package com.kevindeyne.datascrambler.integration;

import com.kevindeyne.datascrambler.dao.SourceConnectionDao;
import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.distributionmodel.*;
import com.kevindeyne.datascrambler.exceptions.ConnectionFailureException;
import com.kevindeyne.datascrambler.helper.ApplyContext;
import com.kevindeyne.datascrambler.helper.SupportedDBType;
import com.kevindeyne.datascrambler.service.CharacteristicService;
import com.kevindeyne.datascrambler.service.DistributionModelService;
import com.kevindeyne.datascrambler.service.GenerationService;
import org.jooq.SQLDialect;
import org.jooq.tools.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public abstract class AbstractDBIntegrationTest {

    protected DistributionModelService distributionModelService;
    protected SourceConnectionDao sourceConnectionDao;
    protected GenerationService generationService;
    private Config config;

    @BeforeEach
    void init() {
        getDB().start();
        distributionModelService = new DistributionModelService(new CharacteristicService());
        sourceConnectionDao = new SourceConnectionDao(getDB().getJdbcUrl(), getDB().getUsername(), getDB().getPassword(), getDialect());
        generationService = new GenerationService(distributionModelService);
        config = new Config(getDB().getUsername(), getDB().getPassword(), getDB().getFirstMappedPort(), getDB().getHost(), getDB().getDatabaseName(), getDBType());
    }

    protected abstract JdbcDatabaseContainer getDB();
    protected abstract SQLDialect getDialect();
    protected abstract SupportedDBType getDBType();

    @Test
    @Disabled
    void testConnectionToContainer() throws SQLException {
        Assertions.assertTrue(sourceConnectionDao.testConnection());
    }

    @Test
    void testBuild() throws Exception {
        createTable("test_123");
        createTable( "person");
        createTable("an_empty_table");

        insertRandomData(5, "person");

        DistributionModel model = distributionModelService.create(sourceConnectionDao, sourceConnectionDao.determineSchemaDynamically());
        Assertions.assertNotNull(model);

        Assertions.assertEquals(3, model.getTables().size());
        Assertions.assertEquals(0, findTableInModel(model, "an_empty_table").getTotalCount());
        final TableData person = findTableInModel(model, "person");
        Assertions.assertEquals(5, person.getTotalCount());
        Assertions.assertEquals(3, person.getFieldData().size());


        for(FieldData data : person.getFieldData()) {
            Assertions.assertNotNull(data.getFieldName());
            Assertions.assertNotNull(data.getGenerator());
            Assertions.assertEquals(1, data.getValueDistribution().getPercentages().size());
            Assertions.assertNotNull(data.getValueDistribution().getPercentages().get(20.0D)); //since random and 5 records (=100%), each record represents a diff value, so 20% distribution of values - each record is diff
            System.out.println(data.getFieldName() + ": " + data.getValueDistribution().getPercentages().get(20D) );
        }

        for(TableData table : model.getTables()) {
            System.out.println(table);
        }

        System.out.println("");
        System.out.println(model);
    }

    @Test
    void testGenerationWithFactor() throws ConnectionFailureException, SQLException {
        DistributionModel model = new DistributionModel();
        TableData tableData = new TableData("person");
        tableData.setTotalCount(10);
        tableData.setOrderOfExecution(1);

        final ValueDistribution valueDistribution = new ValueDistribution();
        valueDistribution.setPercentages(Collections.singletonMap(20D, new ValueDistribution.MutableInt(5)));

        FieldData pk = new FieldData("id");
        pk.setPrimaryKey(true);
        pk.setValueDistribution(valueDistribution);
        pk.setGenerator(new Generator(0, 10, Integer.class.getName(), "integer", false));
        pk.setCharacteristics(Collections.singletonList(Characteristics.CAN_BE_POSITIVE_NUMBER.getShortcutValue()));

        FieldData value = new FieldData("value");
        value.setValueDistribution(valueDistribution);
        value.setGenerator(new Generator(255, 0, String.class.getName(), "varchar", true));

        tableData.setFieldData(Arrays.asList(pk, value));
        model.setTables(Collections.singletonList(tableData));
        generationService.generateFromModel(model, config, new ApplyContext(2, 0,true));

        List<Map<Integer, String>> records = retrieveAllRecords("person");
        Assertions.assertEquals(20, records.size());
    }

    @Test
    void testGenerationWithFill() throws ConnectionFailureException, SQLException {
        DistributionModel model = new DistributionModel();
        TableData tableData = new TableData("person");
        tableData.setTotalCount(10);
        tableData.setOrderOfExecution(1);

        final ValueDistribution valueDistribution = new ValueDistribution();
        valueDistribution.setPercentages(Collections.singletonMap(20D, new ValueDistribution.MutableInt(5)));

        FieldData pk = new FieldData("id");
        pk.setPrimaryKey(true);
        pk.setValueDistribution(valueDistribution);
        pk.setGenerator(new Generator(0, 10, Integer.class.getName(), "integer", false));
        pk.setCharacteristics(Collections.singletonList(Characteristics.CAN_BE_POSITIVE_NUMBER.getShortcutValue()));

        FieldData value = new FieldData("value");
        value.setValueDistribution(valueDistribution);
        value.setGenerator(new Generator(255, 0, String.class.getName(), "varchar", true));

        tableData.setFieldData(Arrays.asList(pk, value));
        model.setTables(Collections.singletonList(tableData));
        generationService.generateFromModel(model, config, new ApplyContext(1, 100,true));

        List<Map<Integer, String>> records = retrieveAllRecords("person");
        Assertions.assertEquals(100, records.size());
    }

    @Test
    void testGenerationWithFillFromEmptyModel() throws ConnectionFailureException, SQLException {
        DistributionModel model = new DistributionModel();
        TableData tableData = new TableData("person");
        tableData.setTotalCount(0);
        tableData.setOrderOfExecution(1);

        FieldData pk = new FieldData("id");
        pk.setPrimaryKey(true);
        pk.setValueDistribution(null);
        pk.setGenerator(new Generator(0, 10, Integer.class.getName(), "integer", false));
        pk.setCharacteristics(Collections.singletonList(Characteristics.CAN_BE_POSITIVE_NUMBER.getShortcutValue()));

        FieldData value = new FieldData("value");
        value.setValueDistribution(null);
        value.setGenerator(new Generator(255, 0, String.class.getName(), "varchar", true));

        tableData.setFieldData(Arrays.asList(pk, value));
        model.setTables(Collections.singletonList(tableData));
        generationService.generateFromModel(model, config, new ApplyContext(1, 150,true));

        List<Map<Integer, String>> records = retrieveAllRecords("person");
        Assertions.assertEquals(150, records.size());

        List<Integer> uniqueValues = ThreadLocalRandom.current()
                .ints(0, 100)
                .distinct()
                .limit(5)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        String randomValue1 = records.get(uniqueValues.get(0)).values().toArray()[0].toString();
        String randomValue2 = records.get(uniqueValues.get(1)).values().toArray()[0].toString();
        String randomValue3 = records.get(uniqueValues.get(2)).values().toArray()[0].toString();
        String randomValue4 = records.get(uniqueValues.get(3)).values().toArray()[0].toString();
        String randomValue5 = records.get(uniqueValues.get(4)).values().toArray()[0].toString();

        Assertions.assertNotEquals(randomValue1, randomValue2);
        Assertions.assertNotEquals(randomValue1, randomValue3);
        Assertions.assertNotEquals(randomValue1, randomValue4);
        Assertions.assertNotEquals(randomValue1, randomValue5);

        Assertions.assertNotEquals(randomValue2, randomValue3);
        Assertions.assertNotEquals(randomValue2, randomValue4);
        Assertions.assertNotEquals(randomValue2, randomValue5);

        Assertions.assertNotEquals(randomValue3, randomValue4);
        Assertions.assertNotEquals(randomValue3, randomValue5);

        Assertions.assertNotEquals(randomValue4, randomValue5);
    }

    protected List<Map<Integer, String>> retrieveAllRecords(String tableName) throws SQLException {
        List<Map<Integer, String>> records = new ArrayList<>();
        Connection connection = DriverManager.getConnection(getDB().getJdbcUrl(), getDB().getUsername(), getDB().getPassword());
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("SELECT * FROM " + tableName);
        while (resultSet.next()) {
            records.add(Collections.singletonMap(resultSet.getInt("id"), resultSet.getString("value")));
        }
        stmt.close();
        connection.close();
        return records;
    }

    private TableData findTableInModel(DistributionModel model, String searchForTable) {
        return model.getTables().stream()
                .filter(t -> t.getTableName().equals(searchForTable)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(searchForTable + " was not found in model"));
    }

    private void createTable(String tableName) throws Exception {
        Connection connection = DriverManager.getConnection(getDB().getJdbcUrl(), getDB().getUsername(), getDB().getPassword());
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE " + tableName + " (id int not null primary key, value_1 varchar(255), value_2 INT)");
        stmt.close();
        connection.close();
    }

    private void insertRandomData(int rowsOfData, String tableName) throws Exception {
        Connection connection = DriverManager.getConnection(getDB().getJdbcUrl(), getDB().getUsername(), getDB().getPassword());
        Statement stmt = connection.createStatement();
        for (int i = 0; i < rowsOfData; i++) {
            stmt.executeUpdate("INSERT INTO " + tableName + " VALUES (" + (i+1) + ", '" + randomString(255) + "', " + new Random().nextInt(99999) + ");");
        }
        stmt.close();
        connection.close();
    }

    protected String randomString(int maxLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append(UUID.randomUUID().toString());
        }
        return StringUtils.abbreviate(sb.toString(), maxLength);
    }

}
