package com.kevindeyne.datascrambler.dao;

import com.kevindeyne.datascrambler.domain.distributionmodel.*;
import com.kevindeyne.datascrambler.mapping.DataTypeMapping;
import com.kevindeyne.datascrambler.service.GenerationService;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import me.tongfei.progressbar.ProgressBar;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
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
        dsl.truncate(DSL.table(tableName)).cascade().execute();
    }

    public void createTable(DSLContext dsl, TableData table) {
        List<Field<?>> primaryKeys = new ArrayList<>();
        CreateTableColumnStep createStep = null;
        try {
            createStep = dsl.createTable(table(quotedName(table.getTableName()))); //cannot be a final variable, so no try-resources - manually close
            for (FieldData fieldData : table.getFieldData()) {
                final Generator generator = fieldData.getGenerator();
                DataType<?> dataType = DataTypeMapping.findByKey(generator.getDataTypeKey()).getDataType();
                dataType = dataType.nullable(generator.isNullable());
                dataType = dataType.precision((generator.getPrecision() > Short.MAX_VALUE) ? Short.MAX_VALUE : generator.getPrecision());
                dataType = dataType.length((generator.getLength() > Short.MAX_VALUE) ? Short.MAX_VALUE : generator.getLength());

                final Field<?> field = field(quotedName(fieldData.getFieldName()), dataType);
                createStep = createStep.column(field);

                if (fieldData.isPrimaryKey()) primaryKeys.add(field(quotedName(fieldData.getFieldName())));
            }
            createStep.execute();
        } finally {
            if (createStep != null) createStep.close();
        }

        if (!primaryKeys.isEmpty()) {
            dsl.alterTable(table(quotedName(table.getTableName()))).add(constraint().primaryKey(primaryKeys.toArray(new Field<?>[0]))).execute();
        }

        table.getFieldData().forEach(fieldData ->
            fieldData.getForeignKeyData().forEach(fk ->
                dsl.alterTable(table(quotedName(table.getTableName())))
                        .add(foreignKey(field(quotedName(fieldData.getFieldName()))).references(quotedName(fk.getTable()), quotedName(fk.getKey())))
                        .execute()
            )
        );
    }

    public void validateTable(DSLContext dsl, TableData table) {
        //TODO
    }

    public void pushData(DSLContext dsl, TableData table) {
        List<Field<?>> fields = table.getFieldData().stream().map(f -> field(quotedName(f.getFieldName()))).collect(Collectors.toCollection(LinkedList::new));

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
                    if (skipListValue == null || field.isPrimaryKey()) {
                        Double percentage = determineActivePercentage(percentagesHandled, field);

                        long skipTo = calculateSkipTo(total, i, percentage);

                        skipList.put(fieldName, skipTo);
                        Object gen;

                        do {
                            gen = generateNewDataField(dsl, field);
                        } while (skipListData.containsValue(gen));

                        skipListData.put(fieldName, gen);
                        skipListValue = skipTo;
                    }

                    data.add(skipListData.get(fieldName));

                    if (i + 1 == skipListValue) {
                        skipList.put(fieldName, null);
                        skipListData.put(fieldName, null);
                    }
                }

                try {
                    dsl.insertInto(table(quotedName(table.getTableName())), fields)
                            .values(data)
                            .execute();
                } catch (DataAccessException e) {
                    if(e.getMessage().contains("duplicate") || e.getMessage().contains(" violates foreign key")) {
                        //System.err.println(e.getMessage() + " for table: " + table.getTableName());
                        pb.step();
                        continue;
                    }

                    throw e;
                }
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
        for (Double percentageToPossiblyHandle : percentages.keySet()) {
            if (!percentagesHandled.containsKey(percentageToPossiblyHandle)) {
                percentagesHandled.put(percentageToPossiblyHandle, new ValueDistribution.MutableInt());
                percentage = percentageToPossiblyHandle;
                break;
            } else if (percentagesHandled.get(percentageToPossiblyHandle).get() < percentages.get(percentageToPossiblyHandle).get()) {
                percentagesHandled.put(percentageToPossiblyHandle, percentagesHandled.get(percentageToPossiblyHandle).increment());
                percentage = percentageToPossiblyHandle;
                break;
            }
        }
        if (null == percentage) { //this is, essentially, a rounding error. If percentages don't count up to 100% but go to 99.999%, that 0.0001% needs to be accounted for
            return 0.0001D; //could probably introduce a percentage cleanup round after determining them too; but this will do for now
        }
        return percentage;
    }

    private Object generateNewDataField(DSLContext dsl, FieldData field) {
        if(field.getForeignKeyData().isEmpty()) {
            final Generator g = field.getGenerator();
            return generationService.generate(g.getOriginalType(), g.getLength(), field);
        } else {
            ForeignKeyData fk = field.getForeignKeyData().get(0); //TODO not sure how multiple FKS works

            final Record1<Object> results = dsl.select(field(quotedName(fk.getKey()))).from(table(quotedName(fk.getTable()))).where().limit(1).offset(field.getOffset()).fetchOne();
            if (results == null) {
                final Generator g = field.getGenerator();
                return generationService.generate(g.getOriginalType(), g.getLength(), field);
            }
            final Object result = results.component1();

            field.setOffset(field.getOffset()+1);
            return result;
        }
    }
}
