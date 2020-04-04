package com.kevindeyne.datascrambler.domain;

import com.kevindeyne.datascrambler.domain.distributionmodel.ValueDistribution;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.jooq.*;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import javax.sql.DataSource;
import java.util.List;
import java.sql.*;
import static org.jooq.impl.DSL.*;

@Data
public class ProdConnection {

    private final String url;
    private final String username;
    private final String password;

    public ProdConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public boolean testConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, username, password);
        DSLContext dsl = using(new DefaultConfiguration().derive(connection));
        Integer[] result = dsl.selectOne().fetch().intoArray(0, Integer.class);
        return result.length == 1 && result[0] == 1;
    }

    public List<Table<?>> getAllTables(DataSource dataSource) {
        try (DSLContext dsl = using(new DefaultConfiguration().derive(dataSource))) {
            return dsl.meta().getTables();
        }
    }

    public Long count(String tableName, DSLContext dsl) {
        return dsl
                        .selectCount()
                        .from(DSL.table(tableName))
                        .fetchOne(0, long.class);
    }

    public ValueDistribution determineDistribution(Table<?> table, Field<?> field, long totalCount, DSLContext dsl) {
        Field<Object> f = field(field.getName());
        Result<? extends Record2<?, Integer>> rawDistribution = dsl.select(f, DSL.count())
                .from(DSL.table(table.getName()))
                .groupBy(f)
                .fetch();
        return new ValueDistribution()
                .from(rawDistribution, totalCount);
    }

    public HikariDataSource toDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(this.url);
        ds.setPassword(this.password);
        ds.setUsername(this.username);
        return ds;
    }
}
