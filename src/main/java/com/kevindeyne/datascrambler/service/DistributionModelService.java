package com.kevindeyne.datascrambler.service;

import com.google.gson.Gson;
import com.kevindeyne.datascrambler.dao.SourceConnectionDao;
import com.kevindeyne.datascrambler.dao.TargetConnectionDao;
import com.kevindeyne.datascrambler.domain.distributionmodel.DistributionModel;
import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import com.kevindeyne.datascrambler.domain.distributionmodel.Generator;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import com.kevindeyne.datascrambler.exceptions.ModelCreationException;
import com.kevindeyne.datascrambler.mapping.DataTypeMapping;
import com.zaxxer.hikari.HikariDataSource;
import me.tongfei.progressbar.ProgressBar;
import org.jooq.*;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.jooq.impl.DSL.using;

@Service
public class DistributionModelService {

    private ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public DistributionModel create(SourceConnectionDao sourceConnectionDao) throws ModelCreationException {
        HikariDataSource dataSource = sourceConnectionDao.toDataSource();
        try {
            DistributionModel model = new DistributionModel();

            List<Table<?>> allTables = sourceConnectionDao.getAllTables(dataSource);
            CountDownLatch latch = new CountDownLatch(allTables.size());

            try (ProgressBar pb = new ProgressBar("Building model", calculateTotalFieldsForModel(allTables))) {
                allTables.forEach(table -> threadPool.execute(() -> {
                    try (DSLContext dsl = using(new DefaultConfiguration().derive(dataSource))) {
                        TableData tableData = new TableData(table.getName());
                        tableData.setTotalCount(sourceConnectionDao.count(tableData.getTableName(), dsl));

                        List<String> primaryKeys = new ArrayList<>();
                        for(UniqueKey<?> key : table.getKeys()) {
                            if(key.isPrimary()) {
                                for (TableField field : key.getFields()) {
                                    primaryKeys.add(field.getName());
                                }
                            }
                        }

                        Arrays.stream(table.fields()).forEach(f -> {
                            FieldData fieldData = new FieldData(f.getName());
                            fieldData.setGenerator(determineGenerator(f));
                            fieldData.setValueDistribution(sourceConnectionDao.determineDistribution(table, f, tableData.getTotalCount(), dsl));

                            if(primaryKeys.contains(f.getName())) fieldData.setPrimaryKey(true);
                            //TODO determine if field is FK with other table

                            tableData.getFieldData().add(fieldData);
                            pb.step();
                        });

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
        }
    }

    private int calculateTotalFieldsForModel(List<Table<?>> allTables) {
        int totalFieldsToModel = 0;
        for (Table<?> t : allTables) totalFieldsToModel += t.fields().length;
        return totalFieldsToModel;
    }

    private Generator determineGenerator(Field<?> f) {
        DataType<?> dataType = f.getDataType();

        int length = dataType.length();
        int precision = dataType.precision();
        String type = dataType.getType().getTypeName();
        boolean nullable = dataType.nullable();
        String key = DataTypeMapping.findByKey(dataType.getTypeName()).getKey();

        return new Generator(length, precision, type, key, nullable);
    }

    public void apply(DSLContext dsl, TargetConnectionDao targetConnectionDao, TableData table, boolean tableExists) {
        if (!tableExists) {
            targetConnectionDao.createTable(dsl, table);
        } else {
            targetConnectionDao.validateTable(dsl, table);
        }
        targetConnectionDao.truncate(dsl, table.getTableName()); //TODO conditional
        targetConnectionDao.pushData(dsl, table);
    }
}
