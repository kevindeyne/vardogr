package com.kevindeyne.datascrambler.integration;

import org.jooq.SQLDialect;
import org.junit.Rule;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresDBTest extends AbstractDBIntegrationTest {

    @Rule
    public PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres"));

    @Override
    protected JdbcDatabaseContainer getDB() {
        return postgres;
    }

    @Override
    protected SQLDialect getDialect() {
        return SQLDialect.POSTGRES;
    }
}
