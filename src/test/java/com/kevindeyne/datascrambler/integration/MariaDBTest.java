package com.kevindeyne.datascrambler.integration;

import com.kevindeyne.datascrambler.dao.SourceConnectionDao;
import com.kevindeyne.datascrambler.domain.distributionmodel.DistributionModel;
import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import com.kevindeyne.datascrambler.service.CharacteristicService;
import com.kevindeyne.datascrambler.service.DistributionModelService;
import org.jooq.SQLDialect;
import org.jooq.tools.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.UUID;

public class MariaDBTest {

    @Rule
    public MariaDBContainer mariaDB = new MariaDBContainer(DockerImageName.parse("mariadb"));

    private DistributionModelService distributionModelService;
    private SourceConnectionDao sourceConnectionDao;

    @Before
    public void init() {
        distributionModelService = new DistributionModelService(new CharacteristicService());
        sourceConnectionDao = new SourceConnectionDao(mariaDB.getJdbcUrl(), mariaDB.getUsername(), mariaDB.getPassword(), SQLDialect.MARIADB);
    }

    @Test
    public void testBuildFromMariaDB() throws Exception {
        createTable("test_123");
        createTable( "person");
        createTable("an_empty_table");

        insertRandomData(5, "person");

        DistributionModel model = distributionModelService.create(sourceConnectionDao, "");
        Assert.assertNotNull(model);

        Assert.assertEquals(3, model.getTables().size());
        Assert.assertEquals(0, findTableInModel(model, "an_empty_table").getTotalCount());
        final TableData person = findTableInModel(model, "person");
        Assert.assertEquals(5, person.getTotalCount());
        Assert.assertEquals(3, person.getFieldData().size());


        for(FieldData data : person.getFieldData()) {
            Assert.assertNotNull(data.getFieldName());
            Assert.assertNotNull(data.getGenerator());
            Assert.assertEquals(1, data.getValueDistribution().getPercentages().size());
            Assert.assertNotNull(data.getValueDistribution().getPercentages().get(20.0D)); //since random and 5 records (=100%), each record represents a diff value, so 20% distribution of values - each record is diff
            System.out.println(data.getFieldName() + ": " + data.getValueDistribution().getPercentages().get(20D) );
        }

        for(TableData table : model.getTables()) {
            System.out.println(table);
        }
    }

    private TableData findTableInModel(DistributionModel model, String searchForTable) {
        return model.getTables().stream()
                .filter(t -> t.getTableName().equals(searchForTable)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(searchForTable + " was not found in model"));
    }

    private void createTable(String tableName) throws Exception {
        Connection connection = DriverManager.getConnection(mariaDB.getJdbcUrl(), mariaDB.getUsername(), mariaDB.getPassword());
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE " + tableName + " (id int not null primary key, value_1 varchar(255), value_2 INT UNSIGNED)");
        stmt.close();
        connection.close();
    }

    private void insertRandomData(int rowsOfData, String tableName) throws Exception {
        Connection connection = DriverManager.getConnection(mariaDB.getJdbcUrl(), mariaDB.getUsername(), mariaDB.getPassword());
        Statement stmt = connection.createStatement();
        for (int i = 0; i < rowsOfData; i++) {
            stmt.executeUpdate("INSERT INTO " + tableName + " VALUES (" + (i+1) + ", '" + randomString(255) + "', " + new Random().nextInt(99999) + ");");
        }
        stmt.close();
        connection.close();
    }

    private String randomString(int maxLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append(UUID.randomUUID().toString());
        }
        return StringUtils.abbreviate(sb.toString(), maxLength);
    }

    @Test
    public void testConnectionToContainer() throws SQLException {
        Assert.assertTrue(sourceConnectionDao.testConnection());
    }

}
