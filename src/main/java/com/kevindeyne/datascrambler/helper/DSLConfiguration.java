package com.kevindeyne.datascrambler.helper;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

public class DSLConfiguration {

    @Getter
    private Configuration dbConfiguration;

    public DSLConfiguration(HikariDataSource dataSource, SupportedDBType dbType) {
        buildConnection(dataSource, dbType.getSQLDialect());
    }

    public DSLConfiguration(HikariDataSource dataSource, SQLDialect dialect) {
        buildConnection(dataSource, dialect);
    }

    private void buildConnection(HikariDataSource dataSource, SQLDialect dialect) {
        dbConfiguration = new DefaultConfiguration().derive(dataSource);
        dbConfiguration.set(dialect);
        dbConfiguration.set(dbConfiguration.settings().withParseDialect(dialect));
    }
}
