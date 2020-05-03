package com.kevindeyne.datascrambler.service;

import com.kevindeyne.datascrambler.dao.SourceConnectionDao;
import com.kevindeyne.datascrambler.dao.TargetConnectionDao;
import com.kevindeyne.datascrambler.domain.distributionmodel.*;
import com.kevindeyne.datascrambler.exceptions.ModelCreationException;
import com.kevindeyne.datascrambler.helper.ApplyContext;
import com.kevindeyne.datascrambler.helper.DSLConfiguration;
import com.kevindeyne.datascrambler.mapping.DataTypeMapping;
import com.zaxxer.hikari.HikariDataSource;
import me.tongfei.progressbar.ProgressBar;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.using;

@Service
public class DistributionModelService {

    @Autowired
    private CharacteristicService characteristicService;

    private ExecutorService threadPool;

    public DistributionModel create(SourceConnectionDao sourceConnectionDao, String schema) throws ModelCreationException {
        HikariDataSource dataSource = sourceConnectionDao.toDataSource();
        try {
            DistributionModel model = new DistributionModel();

            List<Table<?>> allTables = sourceConnectionDao.getAllTables(dataSource, schema);
            CountDownLatch latch = new CountDownLatch(allTables.size());

            final List<String> orderOfExecutionList = determineOrderOfExecution(allTables);

            try (ProgressBar pb = new ProgressBar("Building model", calculateTotalFieldsForModel(allTables))) {
                threadPool = Executors.newFixedThreadPool(10);
                allTables.forEach(table -> threadPool.execute(() -> {
                    try (DSLContext dsl = using(new DSLConfiguration(dataSource, sourceConnectionDao.getSqlDialect()).getDbConfiguration())) {
                        TableData tableData = new TableData(table.getName());
                        tableData.setTotalCount(sourceConnectionDao.count(tableData.getTableName(), dsl));
                        tableData.setOrderOfExecution(orderOfExecutionList.indexOf(tableData.getTableName()));
                        List<String> primaryKeys = new ArrayList<>();
                        for (UniqueKey<?> key : table.getKeys()) {
                            if (key.isPrimary()) {
                                for (TableField field : key.getFields()) {
                                    primaryKeys.add(field.getName());
                                }
                            }
                        }

                        Arrays.stream(table.fields()).forEach(f -> {
                            FieldData fieldData = new FieldData(f.getName());
                            determineGenerator(sourceConnectionDao, dsl, tableData, f, fieldData);
                            determineCharacteristics(dsl, tableData, f, fieldData);
                            fieldData.setValueDistribution(sourceConnectionDao.determineDistribution(table, f, tableData.getTotalCount(), dsl));
                            if (primaryKeys.contains(f.getName())) fieldData.setPrimaryKey(true);
                            determineFKData(table, f, fieldData);
                            tableData.getFieldData().add(fieldData);
                            pb.step();
                        });

                        for (Index index : table.getIndexes()) {
                            if (notPKOrFK(index, primaryKeys, table.getReferences())) {
                                IndexData indexData = new IndexData();
                                indexData.setName(index.getName());
                                indexData.setUnique(index.getUnique());
                                index.getFields().forEach(f -> indexData.getFields().add(f.getName()));
                                tableData.getIndexData().add(indexData);
                            }
                        }

                        model.getTables().add(tableData);
                    } finally {
                        latch.countDown();
                    }
                }));
                latch.await();
            }
            return model;
        } catch (Exception e) {
            throw new ModelCreationException("Failure while setting up distribution model", e);
        } finally {
            if (null != threadPool) threadPool.shutdown();
            if (null != dataSource) dataSource.close();
        }
    }

    private void determineFKData(Table<?> table, Field<?> f, FieldData fieldData) {
        table.getReferences().stream().filter(fk -> fk.getFields().get(0).getName().equals(f.getName())).forEach(fk ->
                fk.getKey().getFields().forEach(k ->
                        fieldData.setForeignKeyData((new ForeignKeyData(fk.getKey().getTable().getName(), k.getName()))))
        );
    }

    private void determineGenerator(SourceConnectionDao sourceConnectionDao, DSLContext dsl, TableData tableData, Field<?> f, FieldData fieldData) {
        try {
            fieldData.setGenerator(determineGenerator(f));
        } catch (IllegalArgumentException e) {
            fieldData.setGenerator(sourceConnectionDao.manualDetermineGenerator(dsl, tableData.getTableName(), f.getName()));
        }
    }

    private void determineCharacteristics(DSLContext dsl, TableData tableData, Field<?> f, FieldData fieldData) {
        final String type = fieldData.getGenerator().getOriginalType();
        if(tableData.getTotalCount() > 1 && characteristicService.supported(type)) {
            fieldData.setCharacteristics(characteristicService.determineCharacteristics(dsl, tableData.getTableName(), f.getName(), type));
        }
    }

    private boolean notPKOrFK(Index index, List<String> primaryKeys, List<? extends ForeignKey<?, ?>> fks) {
        List<String> indexFields = index.getFields().stream().map(SortField::getName).collect(Collectors.toList());
        if (indexFields.size() == primaryKeys.size()) {
            for (String pk : primaryKeys) {
                if (indexFields.contains(pk)) {
                    return false;
                }
            }
        } else if (indexFields.size() == 1) {
            boolean hasAnyFkMatch = false;
            for (ForeignKey<?, ?> fk : fks) {
                if (fk.getName().contains(indexFields.get(0))) {
                    hasAnyFkMatch = true;
                    break;
                }
            }
            return !hasAnyFkMatch;
        }
        return true;
    }

    public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public List<String> determineOrderOfExecution(List<Table<?>> allTables) {
        LinkedList<String> orderOfExecutionList = new LinkedList<>();
        for (Table<?> allTable : allTables) orderOfExecutionList.add(allTable.getName());
        allTables.forEach(table -> {
            for (ForeignKey<?, ?> fk : table.getReferences()) {
                final String foreignTable = fk.getKey().getTable().getName();
                final String currentTable = table.getName();

                //foreign needs to be BEFORE currentTable
                if (orderOfExecutionList.indexOf(foreignTable) > orderOfExecutionList.indexOf(currentTable)) {
                    //remove foreign key from list
                    orderOfExecutionList.remove(foreignTable);
                    //re-add in correct position (before current table)
                    orderOfExecutionList.add(orderOfExecutionList.indexOf(currentTable), foreignTable);

                }
            }
        });
        return orderOfExecutionList;
    }

    private int calculateTotalFieldsForModel(List<Table<?>> allTables) {
        int totalFieldsToModel = 0;
        for (Table<?> t : allTables) totalFieldsToModel += t.fields().length;
        return totalFieldsToModel;
    }

    private Generator determineGenerator(Field<?> f) {
        DataType<?> dataType = f.getDataType();
        final int length = dataType.length();
        final int precision = dataType.precision();
        final boolean nullable = dataType.nullable();
        final String type = dataType.getType().getTypeName();
        try {
            String key = DataTypeMapping.findByKey(dataType.getTypeName()).getKey();
            return new Generator(length, precision, type, key, nullable);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage() + " for " + f.getName());
        }
    }

    public void apply(ApplyContext context) {
        final DSLContext dsl = context.getDsl();
        final TableData table = context.getTable();
        final TargetConnectionDao dao = context.getTargetConnectionDao();

        if (!context.isTableExists()) {
            dao.createTable(dsl, table);
        } else {
            dao.validateTable(dsl, table);
        }
        dao.truncate(dsl, table.getTableName()); //TODO conditional
        dao.pushData(dsl, table);
        dao.createIndexes(dsl, table);
        table.setFieldData(null);
        System.gc(); //Actually helps keep memory usage relatively low; after every table is handled we can clear a whole chunk of memory - otherwise builds up quite a lot
    }
}
