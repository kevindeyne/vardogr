package com.kevindeyne.datascrambler.service;

import com.kevindeyne.datascrambler.domain.Generator;
import com.kevindeyne.datascrambler.domain.ProdConnection;
import com.kevindeyne.datascrambler.domain.distributionmodel.DistributionModel;
import com.kevindeyne.datascrambler.domain.distributionmodel.FieldData;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import com.kevindeyne.datascrambler.exceptions.ModelCreationException;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.jooq.impl.DSL.using;

@Service
public class DistributionModelService {

    private ExecutorService threadPool = Executors.newFixedThreadPool(100);

    public DistributionModel create(ProdConnection prodConnection) throws ModelCreationException {
        HikariDataSource dataSource = prodConnection.toDatasource();
        try {
            DistributionModel model = new DistributionModel();
            prodConnection.getAllTables(dataSource).forEach(table -> threadPool.execute(() -> {
                try (DSLContext dsl = using(new DefaultConfiguration().derive(dataSource))) {
                    TableData tableData = new TableData(table.getName());
                    tableData.setTotalCount(prodConnection.count(tableData.getTableName(), dsl));

                    Arrays.stream(table.fields()).forEach(f -> {
                        FieldData fieldData = new FieldData(f.getName());
                        fieldData.setGenerator(determineGenerator(f));
                        fieldData.setValueDistribution(prodConnection.determineDistribution(table, f, tableData.getTotalCount(), dsl));

                        //TODO determine if field is FK with other table

                        tableData.getFieldData().add(fieldData);
                    });
                    model.getTables().add(tableData);
                }
            }));
            return model;
        } catch (Exception e) {
            throw new ModelCreationException("Failure while setting up distribution model", e);
        }
    }

    private Generator determineGenerator(Field<?> f) {
        DataType<?> dataType = f.getDataType();
        int length = dataType.length();
        int precision = dataType.precision();
        String type = dataType.getType().getTypeName();

        return new Generator(length, precision, type);
    }
}
