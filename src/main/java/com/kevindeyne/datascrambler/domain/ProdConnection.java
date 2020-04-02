package com.kevindeyne.datascrambler.domain;

import org.jooq.*;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import java.util.List;
import java.sql.*;
import static org.jooq.impl.DSL.*;

public class ProdConnection {

    private Connection connection;
    private DSLContext dsl;

    public void setupConnection(String url, String userName, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, userName, password);
        this.dsl = using(new DefaultConfiguration().derive(connection));
    }

    public boolean testConnection() {
        Integer[] result = dsl.selectOne().fetch().intoArray(0, Integer.class);
        return result.length == 1 && result[0] == 1;
    }

    public List<Table<?>> getAllTables() {
        return dsl.meta().getTables();
    }

    public Long count(String tableName) {
        return dsl
                        .selectCount()
                        .from(DSL.table(tableName))
                        .fetchOne(0, long.class);
    }

    public void clearConnection() {
        try {
        this.connection.close();
        } catch (Exception e) {
            return;
        }
    }

    public Distribution determineDistribution(Table<?> table, Field<?> field, Double totalCount) {
        Field<Object> f = field(field.getName());
        Result<? extends Record2<?, Integer>> rawDistribution = dsl.select(f, DSL.count())
                .from(DSL.table(table.getName()))
                .groupBy(f)
                .fetch();
        return Distribution.from(rawDistribution, totalCount);
    }
}
