package com.kevindeyne.datascrambler.dao;

import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import com.kevindeyne.datascrambler.domain.distributionmodel.Generator;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import com.kevindeyne.datascrambler.domain.distributionmodel.ValueDistribution;
import com.kevindeyne.datascrambler.mapping.DataTypeMapping;
import com.kevindeyne.datascrambler.service.GenerationService;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import me.tongfei.progressbar.ProgressBar;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

@Data
public class TargetConnectionDao {

    private final String url;
    private final String username;
    private final String password;
    private final GenerationService generationService;

    public TargetConnectionDao(String url, String username, String password, GenerationService generationService) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.generationService = generationService;
    }

    public boolean testConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, username, password);
        DSLContext dsl = using(new DefaultConfiguration().derive(connection));
        Integer[] result = dsl.selectOne().fetch().intoArray(0, Integer.class);
        return result.length == 1 && result[0] == 1;
    }

    public HikariDataSource toDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(this.url);
        ds.setPassword(this.password);
        ds.setUsername(this.username);
        return ds;
    }

    public List<Table<?>> getAllTables(DataSource dataSource) {
        try (DSLContext dsl = using(new DefaultConfiguration().derive(dataSource))) {
            return dsl.meta().getTables();
        }
    }

    public void truncate(DSLContext dsl, String tableName) {
        dsl.truncate(DSL.table(tableName)).execute();
    }

    public void createTable(DSLContext dsl, TableData table) {
        List<Field<?>> primaryKeys = new ArrayList<>();
        CreateTableColumnStep createStep = null;
        try {
            createStep = dsl.createTable(DSL.table(table.getTableName()));

            for (FieldData fieldData : table.getFieldData()) {
                final Generator generator = fieldData.getGenerator();
                DataType<?> dataType = DataTypeMapping.findByKey(generator.getDataTypeKey()).getDataType();
                dataType = dataType.nullable(generator.isNullable());
                dataType = dataType.precision(generator.getPrecision());
                dataType = dataType.length(generator.getLength());

                final Field<?> field = DSL.field(fieldData.getFieldName(), dataType);
                createStep = createStep.column(field);

                if (fieldData.isPrimaryKey()) primaryKeys.add(field(fieldData.getFieldName()));
            }
            createStep.execute();
        } finally {
            if (createStep != null) createStep.close();
        }

        if (!primaryKeys.isEmpty()) {
            dsl.alterTable(table(table.getTableName())).add(constraint().primaryKey(primaryKeys.toArray(new Field<?>[0]))).execute();
        }
    }

    public void validateTable(DSLContext dsl, TableData table) {
        //TODO
    }

    private ExecutorService threadPool = Executors.newFixedThreadPool(50);

    public void pushData(DSLContext dsl, TableData table) {
        List<Field<?>> fields = table.getFieldData().stream().map(f -> DSL.field(f.getFieldName())).collect(Collectors.toCollection(LinkedList::new));

        final long total = table.getTotalCount();

        Map<String, Long> skipList = new HashMap<>();
        Map<String, Object> skipListData = new HashMap<>();

        Map<String, Map<Double, ValueDistribution.MutableInt>> percentagesHandled = new HashMap<>();

        try (ProgressBar pb = new ProgressBar("Generating data for " + table.getTableName(), total)) {
            for (long i = 0; i < total; i++) {
                List<Object> data = new LinkedList<>();

                for (FieldData field : table.getFieldData()) {
                    final String fieldName = field.getFieldName();
                    Long skipListValue = skipList.get(fieldName);

                    if (skipListValue == null) {
                        Double percentage = determineActivePercentage(percentagesHandled, field);

                        long skipTo = calculateSkipTo(total, i, percentage);

                        skipList.put(fieldName, skipTo);
                        skipListData.put(fieldName, generateNewDataField(field));
                        skipListValue = skipTo;
                    }

                    data.add(skipListData.get(fieldName));

                    if (i + 1 == skipListValue) {
                        skipList.put(fieldName, null);
                        skipListData.put(fieldName, null);
                    }
                }

                dsl.insertInto(DSL.table(table.getTableName()), fields)
                        .values(data)
                        .execute();
                pb.step();
            }
        }
    }

    private long calculateSkipTo(long total, long i, Double percentage) {
        long skipTo = Math.round(i + (((double) total) / 100 * percentage));
        if (skipTo > total) skipTo = total;
        return skipTo;
    }

    private Double determineActivePercentage(Map<String, Map<Double, ValueDistribution.MutableInt>> percentagesHandledPerField, FieldData field) {
        percentagesHandledPerField.computeIfAbsent(field.getFieldName(), k -> new HashMap<>());
        Map<Double, ValueDistribution.MutableInt> percentagesHandled = percentagesHandledPerField.get(field.getFieldName());
        final Map<Double, ValueDistribution.MutableInt> percentages = field.getValueDistribution().getPercentages();
        Double percentage = null;
        for (Map.Entry<Double, ValueDistribution.MutableInt> percentageToPossiblyHandle : percentages.entrySet()) {
            if (!percentagesHandled.containsKey(percentageToPossiblyHandle.getKey())) {
                percentagesHandled.put(percentageToPossiblyHandle.getKey(), new ValueDistribution.MutableInt());
                percentage = percentageToPossiblyHandle.getKey();
                break;
            } else if (percentagesHandled.get(percentageToPossiblyHandle.getKey()).get() < percentageToPossiblyHandle.getValue().get()) {
                percentagesHandled.put(percentageToPossiblyHandle.getKey(), percentagesHandled.get(percentageToPossiblyHandle.getKey()).increment());
                percentage = percentageToPossiblyHandle.getKey();
                break;
            }
        }
        if (null == percentage) {
            throw new RuntimeException("Could not find percentage");
        }
        return percentage;
    }

    private Object generateNewDataField(FieldData field) {
        final Generator g = field.getGenerator();
        return generationService.generate(g.getOriginalType(), g.getLength(), field.getFieldName());
    }
}
