package com.kevindeyne.datascrambler.commands;

import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.dao.SourceConnectionDao;
import com.kevindeyne.datascrambler.domain.distributionmodel.DistributionModel;
import com.kevindeyne.datascrambler.exceptions.ConfigFileException;
import com.kevindeyne.datascrambler.exceptions.ModelCreationException;
import com.kevindeyne.datascrambler.helper.PrintCmds;
import com.kevindeyne.datascrambler.service.ConfigService;
import com.kevindeyne.datascrambler.service.DistributionModelService;
import com.kevindeyne.datascrambler.service.FileService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import javax.annotation.PostConstruct;

import static com.kevindeyne.datascrambler.domain.MessageConstants.*;

@ShellComponent
public class CommandController {

    private static final String DISTRIBUTION_MODEL_JSON = "distribution-model.json";

    private ConfigService configService;
    private DistributionModelService distributionModelService;
    private FileService fileService;

    public CommandController(ConfigService configService,
                             DistributionModelService distributionModelService,
                             FileService fileService) {
        this.configService = configService;
        this.distributionModelService = distributionModelService;
        this.fileService = fileService;
    }

    @PostConstruct
    public void init() {
        System.out.println(PrintCmds.green("Use build"));
    }

    @ShellMethod("Builds the model")
    public String build() {
        final SourceConnectionDao sourceConnectionDao;

        try {
            Config config = configService.loadConfig();
            sourceConnectionDao = config.setupSourceConnection();
        } catch (Exception e) {
            configService.loadConfig(true);
            return build();
        }

        try {
            DistributionModel model = distributionModelService.create(sourceConnectionDao);
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



        return MSG_GEN_COMPLETED;
    }
}