package com.kevindeyne.datascrambler.service;

import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.helper.SupportedDBType;
import com.kevindeyne.datascrambler.shell.InputReader;
import com.kevindeyne.datascrambler.shell.ShellHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;

public class ConfigServiceTest {

    private static final int SAMPLE_PORT = 3300;
    private static final String SAMPLE_HOST = "some_server.com";
    private static final String SAMPLE_USERNAME = "some_username";
    private static final String SAMPLE_RAW_PASSWORD = "some_p@ssw0rd";
    private static final String SAMPLE_DB_NAME = "test_PROD";

    private static final String SAMPLE_JSON_CONFIG = "{\"password\":\"o/Xva2CtE6rRP5zgsAjfZA==\",\"port\":3300,\"dbName\":\"test_PROD\",\"host\":\"some_server.com\",\"dbType\":\"mariadb\",\"username\":\"some_username\"}";

    @Mock
    private InputReader input;
    @Mock
    private FileService fileService;
    @Mock
    private EncryptService encryptService;
    @Mock
    private ShellHelper shellHelper;

    @InjectMocks
    private ConfigService service;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConfigSetupHappyFlow_NewFile() {
        Mockito.when(fileService.doesFileExist(anyString(), anyString(), anyString())).thenReturn(false);

        Mockito.when(input.getString("Host", "localhost")).thenReturn(SAMPLE_HOST);
        Mockito.when(input.getInteger("Port", 3306)).thenReturn(SAMPLE_PORT);
        Mockito.when(input.getString("Username")).thenReturn(SAMPLE_USERNAME);
        Mockito.when(input.getPassword("Password")).thenReturn(SAMPLE_RAW_PASSWORD);
        Mockito.when(input.getString("Database name")).thenReturn(SAMPLE_DB_NAME);
        Mockito.when(input.getOption("DB type", SupportedDBType.all())).thenReturn(SupportedDBType.MARIADB.name());

        Mockito.when(encryptService.encrypt(anyString())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(encryptService.decrypt(anyString())).thenReturn(SAMPLE_RAW_PASSWORD);

        Config config = service.loadSourceConfig();

        assertNotNull(config);
        assertEquals(SAMPLE_HOST, config.getHostSource());
        assertEquals(SAMPLE_PORT, config.getPortSource(), 0);
        assertEquals(SAMPLE_DB_NAME, config.getDbNameSource());
        assertEquals(SAMPLE_USERNAME, config.getUsernameSource());
        assertEquals(SAMPLE_RAW_PASSWORD, config.getPasswordSource());
        assertEquals(SupportedDBType.MARIADB, config.getDbTypeSource());
    }

    @Test
    public void testConfigSetupHappyFlow_ExistingFile() throws Exception {
        Mockito.when(fileService.doesFileExist(anyString(), anyString(), anyString())).thenReturn(true);
        Mockito.when(fileService.loadFile(anyString())).thenReturn(SAMPLE_JSON_CONFIG);

        Mockito.when(encryptService.decrypt(anyString())).thenReturn(SAMPLE_RAW_PASSWORD);

        Config config = service.loadSourceConfig();

        assertNotNull(config);
        assertEquals(SAMPLE_HOST, config.getHostSource());
        assertEquals(SAMPLE_PORT, config.getPortSource(), 0);
        assertEquals(SAMPLE_DB_NAME, config.getDbNameSource());
        assertEquals(SAMPLE_USERNAME, config.getUsernameSource());
        assertEquals(SAMPLE_RAW_PASSWORD, config.getPasswordSource());
        assertEquals(SupportedDBType.MARIADB, config.getDbTypeSource());
    }

    @Test
    public void testFileCorrupted() throws Exception {
        Mockito.when(fileService.doesFileExist(anyString(), anyString(), anyString())).thenReturn(true);
        Mockito.when(fileService.loadFile(anyString())).thenReturn("asdljasldkjaslkdjaslkdj");

        Mockito.when(input.getString("Host", "localhost")).thenReturn(SAMPLE_HOST);
        Mockito.when(input.getInteger("Port", 3306)).thenReturn(SAMPLE_PORT);
        Mockito.when(input.getString("Username")).thenReturn(SAMPLE_USERNAME);
        Mockito.when(input.getPassword("Password")).thenReturn(SAMPLE_RAW_PASSWORD);
        Mockito.when(input.getString("Database name")).thenReturn(SAMPLE_DB_NAME);
        Mockito.when(input.getOption("DB type", SupportedDBType.all())).thenReturn(SupportedDBType.MARIADB.name());

        Mockito.when(encryptService.encrypt(anyString())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(encryptService.decrypt(anyString())).thenReturn(SAMPLE_RAW_PASSWORD);

        Config config = service.loadSourceConfig();

        assertNotNull(config);
        assertEquals(SAMPLE_HOST, config.getHostSource());
        assertEquals(SAMPLE_PORT, config.getPortSource(), 0);
        assertEquals(SAMPLE_DB_NAME, config.getDbNameSource());
        assertEquals(SAMPLE_USERNAME, config.getUsernameSource());
        assertEquals(SAMPLE_RAW_PASSWORD, config.getPasswordSource());
        assertEquals(SupportedDBType.MARIADB, config.getDbTypeSource());
    }

    @Test
    public void testFileCorrupted2() throws Exception {
        Mockito.when(fileService.doesFileExist(anyString(), anyString(), anyString())).thenReturn(true);
        Mockito.when(fileService.loadFile(anyString())).thenReturn("{\"glossary\": {\"title\": \"example glossary\"}}\n");

        Mockito.when(input.getString("Host", "localhost")).thenReturn(SAMPLE_HOST);
        Mockito.when(input.getInteger("Port", 3306)).thenReturn(SAMPLE_PORT);
        Mockito.when(input.getString("Username")).thenReturn(SAMPLE_USERNAME);
        Mockito.when(input.getPassword("Password")).thenReturn(SAMPLE_RAW_PASSWORD);
        Mockito.when(input.getString("Database name")).thenReturn(SAMPLE_DB_NAME);
        Mockito.when(input.getOption("DB type", SupportedDBType.all())).thenReturn(SupportedDBType.MARIADB.name());

        Mockito.when(encryptService.encrypt(anyString())).thenReturn(UUID.randomUUID().toString());
        Mockito.when(encryptService.decrypt(anyString())).thenReturn(SAMPLE_RAW_PASSWORD);

        Config config = service.loadSourceConfig();

        assertNotNull(config);
        assertEquals(SAMPLE_HOST, config.getHostSource());
        assertEquals(SAMPLE_PORT, config.getPortSource(), 0);
        assertEquals(SAMPLE_DB_NAME, config.getDbNameSource());
        assertEquals(SAMPLE_USERNAME, config.getUsernameSource());
        assertEquals(SAMPLE_RAW_PASSWORD, config.getPasswordSource());
        assertEquals(SupportedDBType.MARIADB, config.getDbTypeSource());
    }



}
