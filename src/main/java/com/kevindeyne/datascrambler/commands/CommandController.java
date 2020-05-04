package com.kevindeyne.datascrambler.commands;

import com.kevindeyne.datascrambler.dao.SourceConnectionDao;
import com.kevindeyne.datascrambler.dao.TargetConnectionDao;
import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.distributionmodel.DistributionModel;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import com.kevindeyne.datascrambler.exceptions.ConfigFileException;
import com.kevindeyne.datascrambler.exceptions.ModelCreationException;
import com.kevindeyne.datascrambler.helper.ApplyContext;
import com.kevindeyne.datascrambler.helper.DSLConfiguration;
import com.kevindeyne.datascrambler.helper.PrintCmds;
import com.kevindeyne.datascrambler.service.*;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.Named;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Positive;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.kevindeyne.datascrambler.domain.MessageConstants.*;
import static org.jooq.impl.DSL.using;

@ShellComponent
public class CommandController {

    private static final String DISTRIBUTION_MODEL_JSON = "distribution-model.json";

    private final ConfigService configService;
    private final DistributionModelService distributionModelService;
    private final FileService fileService;
    private final GenerationService generationService;
    private final PKDistributionService pkDistributionService;

    public CommandController(ConfigService configService,
                             DistributionModelService distributionModelService,
                             FileService fileService,
                             GenerationService generationService,
                             PKDistributionService pkDistributionService) {
        this.configService = configService;
        this.distributionModelService = distributionModelService;
        this.fileService = fileService;
        this.generationService = generationService;
        this.pkDistributionService = pkDistributionService;
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
    public String generate(@ShellOption(defaultValue="1") @Positive int factor, boolean clean) {
        if(!fileService.doesFileExist(DISTRIBUTION_MODEL_JSON, MSG_DIST_FOUND, MSG_DIST_NOT_FOUND)) return MSG_DIST_REQUIRED;

        HikariDataSource dataSource = null;
        ApplyContext context = new ApplyContext(factor, clean);

        try {
            Config config = configService.loadTargetConfig();
            final TargetConnectionDao dao = config.setupTargetConnection(generationService, pkDistributionService);

            DistributionModel model = fileService.loadModel(DISTRIBUTION_MODEL_JSON);

            dataSource = dao.toDataSource();
            List<String> existingTableNames = dao.getAllTables(dataSource).stream().map(Named::getName).collect(Collectors.toList());

            model.getTables().sort(Comparator.comparing(TableData::getOrderOfExecution));
            context.setTargetConnectionDao(dao);

            try (DSLContext dsl = using(new DSLConfiguration(dataSource, config.getDbTypeTarget()).getDbConfiguration())) {
                context.setDsl(dsl);

                for (TableData table : model.getTables()) {
                    distributionModelService.apply(context.withTable(table, existingTableNames.contains(table.getTableName())));
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