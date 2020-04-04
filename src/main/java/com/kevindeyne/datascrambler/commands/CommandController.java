package com.kevindeyne.datascrambler.commands;

import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.ProdConnection;
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
        final ProdConnection prodConnection;

        try {
            Config config = configService.loadConfig();
            prodConnection = config.setupProdConnection();
        } catch (Exception e) {
            configService.loadConfig(true);
            return build();
        }

        try {
            DistributionModel model = distributionModelService.create(prodConnection);
            fileService.writeToFile(model.toJsonFile(), DISTRIBUTION_MODEL_JSON);
            return "Model created. You can now use this model to generate data. Do this by calling generate";
        } catch (ModelCreationException | ConfigFileException e) {
            e.getWrappedException().printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
          //
        }
    }
}