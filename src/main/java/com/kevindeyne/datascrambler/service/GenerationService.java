package com.kevindeyne.datascrambler.service;

import com.kevindeyne.datascrambler.dao.TargetConnectionDao;
import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.distributionmodel.DistributionModel;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import com.kevindeyne.datascrambler.exceptions.ConnectionFailureException;
import com.kevindeyne.datascrambler.helper.ApplyContext;
import com.kevindeyne.datascrambler.helper.DSLConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.Named;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.using;

@Service
public class GenerationService {

    private final DistributionModelService distributionModelService;

    public GenerationService(DistributionModelService distributionModelService) {
        this.distributionModelService = distributionModelService;
    }

    public void generateFromModel(DistributionModel model, Config config, ApplyContext context) throws ConnectionFailureException {
        final TargetConnectionDao targetConnectionDao = config.setupTargetConnection(new GenerationHelperService(), new PKDistributionService());

        try (HikariDataSource dataSource = targetConnectionDao.toDataSource()) {
            List<String> existingTableNames = targetConnectionDao.getAllTables(dataSource).stream().map(Named::getName).collect(Collectors.toList());

            model.getTables().sort(Comparator.comparing(TableData::getOrderOfExecution));
            context.setTargetConnectionDao(targetConnectionDao);

            DSLContext dsl = using(new DSLConfiguration(dataSource, config.getDbTypeTarget()).getDbConfiguration());
            context.setDsl(dsl);

            for (TableData table : model.getTables()) {
                distributionModelService.apply(context.withTable(table, existingTableNames.contains(table.getTableName())));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
