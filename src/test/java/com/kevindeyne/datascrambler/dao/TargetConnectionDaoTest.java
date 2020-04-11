package com.kevindeyne.datascrambler.dao;

import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import com.kevindeyne.datascrambler.domain.distributionmodel.Generator;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import com.kevindeyne.datascrambler.domain.distributionmodel.ValueDistribution;
import com.kevindeyne.datascrambler.mapping.DataTypeMapping;
import com.kevindeyne.datascrambler.service.GenerationService;
import org.jooq.*;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;

public class TargetConnectionDaoTest {

    private TargetConnectionDao dao;

    @Mock
    private GenerationService generationService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        dao = new TargetConnectionDao("", "", "", generationService);
    }

    @Test
    public void testWithOneField50Distribution() {
        MockDataProvider provider = new MockProvider();
        MockConnection connection = new MockConnection(provider);
        Settings settings = new Settings();
        settings.setExecuteLogging(false);
        DSLContext dsl = DSL.using(connection, SQLDialect.MARIADB, settings);

        final TableData table = new TableData("exampleTable");
        table.setTotalCount(12345);
        final FieldData fieldData = new FieldData("exampleField");
        fieldData.setGenerator(new Generator(5, 5, String.class.getName(), DataTypeMapping.VARCHAR.getKey(), false));
        final ValueDistribution valueDistribution = new ValueDistribution();
        final HashMap<Double, ValueDistribution.MutableInt> percentages = new HashMap<>();
        percentages.put(25D, new ValueDistribution.MutableInt(2));
        percentages.put(50D, new ValueDistribution.MutableInt(1));
        valueDistribution.setPercentages(percentages);
        fieldData.setValueDistribution(valueDistribution);
        table.setFieldData(Collections.singletonList(fieldData));

        Mockito.when(generationService.generate(anyString(), anyInt(), anyString())).thenReturn(UUID.randomUUID());
        dao.pushData(dsl, table);
        Mockito.verify(generationService, Mockito.times(3)).generate(anyString(), anyInt(), anyString());
    }

}