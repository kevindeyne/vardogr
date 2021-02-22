package com.kevindeyne.datascrambler.integration;

import com.kevindeyne.datascrambler.helper.SupportedDBType;
import org.jooq.SQLDialect;
import org.junit.Rule;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MariaDBTest extends AbstractDBIntegrationTest {

    @Rule
    public MariaDBContainer mariaDB = new MariaDBContainer(DockerImageName.parse("mariadb"));

    @Override
    protected JdbcDatabaseContainer getDB() {
        return mariaDB;
    }

    @Override
    protected SQLDialect getDialect() {
        return SQLDialect.MARIADB;
    }

    @Override
    protected SupportedDBType getDBType() {
        return SupportedDBType.MARIADB;
    }
}
