package com.kevindeyne.datascrambler.dao;

import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import com.kevindeyne.datascrambler.domain.distributionmodel.Generator;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.using;

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
            }

            createStep.execute();
        } finally {
            if (createStep != null) createStep.close();
        }
    }

    public void validateTable(DSLContext dsl, TableData table) {
        //TODO
    }

    private ExecutorService threadPool = Executors.newFixedThreadPool(50);

    public void pushData(DSLContext dsl, TableData table) {
        List<Field<?>> fields = table.getFieldData().stream().map(f -> DSL.field(f.getFieldName())).collect(Collectors.toCollection(LinkedList::new));

        final long total = table.getTotalCount();
        try (ProgressBar pb = new ProgressBar("Generating data for " + table.getTableName(), total)) {
            CountDownLatch latch = new CountDownLatch((int) total);

            for (int i = 0; i < total; i++) {
                threadPool.execute(() -> {
                    List<Object> data = new LinkedList<>();
                    table.getFieldData().forEach(f -> {
                        final Generator g = f.getGenerator();
                        data.add(generationService.generate(g.getOriginalType(), g.getLength(), f.getFieldName()));
                    });
                    dsl.insertInto(DSL.table(table.getTableName()), fields)
                            .values(data)
                            .execute();
                    pb.step();
                    latch.countDown();
                });
            }

            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
