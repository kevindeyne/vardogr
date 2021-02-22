package com.kevindeyne.datascrambler.commands;

import com.kevindeyne.datascrambler.dao.SourceConnectionDao;
import com.kevindeyne.datascrambler.dao.TargetConnectionDao;
import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.distributionmodel.DistributionModel;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import com.kevindeyne.datascrambler.exceptions.ConfigFileException;
import com.kevindeyne.datascrambler.exceptions.ConnectionFailureException;
import com.kevindeyne.datascrambler.exceptions.ModelCreationException;
import com.kevindeyne.datascrambler.helper.ApplyContext;
import com.kevindeyne.datascrambler.helper.DSLConfiguration;
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

    public CommandController(ConfigService configService,
                             DistributionModelService distributionModelService,
                             FileService fileService,
                             GenerationHelperService generationHelperService,
                             PKDistributionService pkDistributionService,
                             GenerationService generationService) {
        this.configService = configService;
        this.distributionModelService = distributionModelService;
        this.fileService = fileService;
        this.generationService = generationService;
    }

    @PostConstruct
    public void init() {
        printLogo();
        System.out.println();
        System.out.println("# ----------");
        System.out.println("# Use build to get started. This will validate a config file exists and build the distribution model.");
        System.out.println("# Use generate once a model has been constructed.");
        System.out.println("# Use help to see details on parameters.");
        System.out.println("# ----------");

        configService.hasConfig();
    }

    private void printLogo() {
        System.out.println();
        System.out.println(".##..##...####...#####...#####....####....####...#####..");
        System.out.println(".##..##..##..##..##..##..##..##..##..##..##......##..##.");
        System.out.println(".##..##..######..#####...##..##..##..##..##.###..#####..");
        System.out.println("..####...##..##..##..##..##..##..##..##..##..##..##..##.");
        System.out.println("...##....##..##..##..##..#####....####....####...##..##.");
        System.out.println("........................................................");
    }

    @ShellMethod("Builds the model")
    public String build(boolean reset) {
        final SourceConnectionDao sourceConnectionDao;
        String schemaSource;
        try {
            if(reset) configService.clearConfigs();
            Config config = configService.loadSourceConfig();
            sourceConnectionDao = config.setupSourceConnection();
            schemaSource = config.getSchemaSource();
        } catch (Exception e) {
            return "ERROR: Please retry. An error occurred (" + e.getMessage() + ")";
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
    public String generate(@ShellOption(defaultValue="1") @Positive int factor, boolean clean) throws ConfigFileException, ConnectionFailureException {
        if(!fileService.doesFileExist(DISTRIBUTION_MODEL_JSON, MSG_DIST_FOUND, MSG_DIST_NOT_FOUND)) return MSG_DIST_REQUIRED;
        DistributionModel model = fileService.loadModel(DISTRIBUTION_MODEL_JSON);
        Config config = configService.loadTargetConfig();
        generationService.generateFromModel(model, config, new ApplyContext(factor, clean));
        return MSG_GEN_COMPLETED;
    }
}