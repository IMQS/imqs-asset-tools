package za.co.imqs;

import org.apache.commons.cli.*;
import org.apache.log4j.PropertyConfigurator;
import za.co.imqs.exporters.BoqExporter;
import za.co.imqs.importers.BoqImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by gerhardv on 2020-02-04.
 * <p>
 * A command line utility (Controller class) for routing a request to the responsible handler class.
 * The program expects 3 arguments:
 * 1. Command : -c for example exportBoq, importBoq, importClassification etc.
 * 2. Config file : -f "filename", filename of the configuration file.
 * 3. Log file : -l filename of the properties file for log4j
 *
 * Each handler class is responsible for parsing its own configuration file. See handler class for more details.
 *
 */

public class ImqsAssetTools {
    public static void main(String args[]) throws ParseException {
        Logger logger = LoggerFactory.getLogger(ImqsAssetTools.class);
        
        try {
            // instantiate Options object for command line arguments
            Options options = new Options();
            // add all the arguments
            options.addOption("c", true, "Command : (exportBoq, importBoq)");
            options.getOption("c").setRequired(true);
            options.addOption("f", true, "Path for the Configuration filename");
            options.getOption("f").setRequired(true);
            options.addOption("l", true, "Path for the Log4j properties filename");
            options.getOption("l").setRequired(true);

            // parse commandline arguments
            final String command;
            final String configFile;
            final String logConfigFile;
            CommandLineParser cmdParser = new DefaultParser();
            CommandLine arg = cmdParser.parse(options, args);
            command = arg.getOptionValue("c");
            configFile = arg.getOptionValue("f");
            logConfigFile = arg.getOptionValue("l");
            
            PropertyConfigurator.configure(logConfigFile);

            //execute command handler
            logger.info("Command line arguments: {}", Arrays.toString(args));
            switch (command) {
                case "exportBoq":
                    exportBoq(configFile);
                    break;
                case "importBoq":
                    importBoQ(configFile);
                    break;
            }

        } catch (Exception e) {
            logger.error("Exception thrown. " + e.getMessage(), e);
        }
        logger.info("Done. Exit Applciation.");
    }

        /**
     * @param configFilename = Configuration file for the BOQ export
     * @throws Exception
     */
    private static void exportBoq(String configFilename) throws Exception {
        final BoqExporter exporter = new BoqExporter(configFilename);
        exporter.execute();
    }

    /**
     * @param configFilename = Configuration file for the BOQ import
     * @throws Exception
     */
    private static void importBoQ(String configFilename) throws Exception {
        final BoqImporter importer = new BoqImporter(configFilename);
        importer.execute();
    }
}


