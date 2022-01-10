package com.kevindeyne.datascrambler.integration;

import com.kevindeyne.datascrambler.helper.SupportedDBType;
import org.jooq.SQLDialect;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class MySQLDBTest extends AbstractDBIntegrationTest {

    public MySQLContainer mysql = new MySQLContainer<>(DockerImageName.parse("mysql"));

    @Override
    protected JdbcDatabaseContainer getDB() {
        return mysql;
    }

    @Override
    protected SQLDialect getDialect() {
        return SQLDialect.MYSQL;
    }

    @Override
    protected SupportedDBType getDBType() {
        return SupportedDBType.MYSQL;
    }
}
