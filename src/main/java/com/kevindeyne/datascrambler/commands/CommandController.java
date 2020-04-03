package com.kevindeyne.datascrambler.commands;

import com.kevindeyne.datascrambler.domain.Config;
import com.kevindeyne.datascrambler.domain.Distribution;
import com.kevindeyne.datascrambler.domain.Generator;
import com.kevindeyne.datascrambler.domain.ProdConnection;
import com.kevindeyne.datascrambler.helper.PrintCmds;
import com.kevindeyne.datascrambler.service.ConfigService;
import org.jooq.Field;
import org.jooq.Table;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import javax.annotation.PostConstruct;
import java.util.List;

@ShellComponent
public class CommandController {

    private ConfigService configService;

    public CommandController(ConfigService configService) {
        this.configService = configService;
    }

    @PostConstruct
    public void init() {
        log("Use build");
    }

    @ShellMethod("Builds the model")
    public String build() {
        ProdConnection prodConnection = null;

        try {
            Config config = configService.loadConfig();
            prodConnection = config.setupProdConnection();
        } catch (Exception e) {
            configService.loadConfig(true);
            return build();
        }

        try {
            List<Table<?>> tables = prodConnection.getAllTables();
            for(Table<?> table : tables) {
                log();
                Double count = Double.valueOf(prodConnection.count(table.getName()));
                log("Count ("+ table.getName() +"): " + count);

                for (Field<?> f : table.fields()) {
                    Generator generator = Generator.determineGenerator(f);
                    Distribution distribution = prodConnection.determineDistribution(table, f, count);

                    //TODO determine if field is FK with other table
                    log(f.getName() + " - " + generator + " - " + distribution);
                }
            }

            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
            if(prodConnection != null) {
                prodConnection.clearConnection();
            }
        }
    }

    private void log() {
        log("");
    }

    private void log(String string) {
        System.out.println(PrintCmds.green(string));
    }

}