package com.kevindeyne.datascrambler.commands;

import com.kevindeyne.datascrambler.dao.SourceConnectionDao;
import com.kevindeyne.datascrambler.dao.TargetConnectionDao;
import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.distributionmodel.DistributionModel;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import com.kevindeyne.datascrambler.exceptions.ConfigFileException;
import com.kevindeyne.datascrambler.exceptions.ModelCreationException;
import com.kevindeyne.datascrambler.helper.PrintCmds;
import com.kevindeyne.datascrambler.service.ConfigService;
import com.kevindeyne.datascrambler.service.DistributionModelService;
import com.kevindeyne.datascrambler.service.FileService;
import com.kevindeyne.datascrambler.service.GenerationService;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.Named;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.kevindeyne.datascrambler.domain.MessageConstants.*;
import static org.jooq.impl.DSL.using;

@ShellComponent
public class CommandController {

    private static final String DISTRIBUTION_MODEL_JSON = "distribution-model.json";

    private ConfigService configService;
    private DistributionModelService distributionModelService;
    private FileService fileService;
    private GenerationService generationService;

    public CommandController(ConfigService configService,
                             DistributionModelService distributionModelService,
                             FileService fileService,
                             GenerationService generationService) {
        this.configService = configService;
        this.distributionModelService = distributionModelService;
        this.fileService = fileService;
        this.generationService = generationService;
    }

    @PostConstruct
    public void init() {
        System.out.println(PrintCmds.green("Use build"));
    }

    @ShellMethod("Builds the model")
    public String build() {
        final SourceConnectionDao sourceConnectionDao;
        String schemaSource;
        try {
            Config config = configService.loadSourceConfig();
            sourceConnectionDao = config.setupSourceConnection();
            schemaSource = config.getSchemaSource();
        } catch (Exception e) {
            configService.loadSourceConfig(true);
            return build();
        }

        try {
            DistributionModel model = distributionModelService.create(sourceConnectionDao, schemaSource);
            fileService.writeToFile(model.toJsonFile(), DISTRIBUTION_MODEL_JSON);
            return MSG_BUILD_COMPLETED;
        } catch (ModelCreationException | ConfigFileException e) {
            e.getWrappedException().printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @ShellMethod("Generates data based on the model")
    public String generate() {
        if(!fileService.doesFileExist(DISTRIBUTION_MODEL_JSON, MSG_DIST_FOUND, MSG_DIST_NOT_FOUND)) return MSG_DIST_REQUIRED;
        HikariDataSource dataSource = null;
        final TargetConnectionDao targetConnectionDao;
        try {
            Config config = configService.loadTargetConfig();
            targetConnectionDao = config.setupTargetConnection(generationService);

            DistributionModel model = fileService.loadModel(DISTRIBUTION_MODEL_JSON);

            dataSource = targetConnectionDao.toDataSource();
            List<String> existingTableNames = targetConnectionDao.getAllTables(dataSource).stream().map(Named::getName).collect(Collectors.toList());

            model.getTables().sort(Comparator.comparing(TableData::getOrderOfExecution));

            try (DSLContext dsl = using(new DefaultConfiguration().derive(dataSource))) {
                for (TableData table : model.getTables()) {
                    boolean tableExists = existingTableNames.contains(table.getTableName());
                    distributionModelService.apply(dsl, targetConnectionDao, table, tableExists);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            //configService.loadTargetConfig(true);
            //return generate();
            throw new RuntimeException(e);
        } finally {
            if(dataSource != null) dataSource.close();
        }

        return MSG_GEN_COMPLETED;
    }
}