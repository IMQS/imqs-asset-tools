package za.co.imqs;

import org.apache.commons.cli.*;
import za.co.imqs.exporters.BoqExporter;
import za.co.imqs.importers.BoqImporter;


import static java.lang.System.out;

/**
 * Created by gerhardv on 2020-02-04.
 * <p>
 * A command line utility (Controller class) for routing a request to the responsible handler class.
 * The program expects two arguments:
 * 1. Command : for example exportBoq, importBoq, importClassification etc.
 * 2. Config file  : -f "filename", filename of the configuration file.
 * Each handler class is responsible for parsing its own configuration file. Should a database connection be required,
 * a "dbParams" key is expected, specifying the connection parameters.
 * For example:
 * {
 * "dbParams": {
 * "driverClass": "com.microsoft.sqlserver.jdbc.SQLServerDriver",
 * "jdbcUrl": "jdbc:sqlserver://localhost:1433;databaseName=IMQS_Asset_BuffaloCity_20180510",
 * "username": "myname",
 * "password": "mysecret"
 * },
 * "exportFilename": "D:\\Dump\\IMQS\\Exports\\test\\export_java.csv"
 * }
 */

public class ImqsAssetTools {

    private static void log(String Msg) {

        out.println(Msg);
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


    public static void main(String args[]) throws ParseException {

        final String command;
        final String configFile;

        try {

            // read the first argument for the command
            command = args[0];

            // instantiate Options object for command line arguments
            Options options = new Options();
            // add options
            options.addOption("f", true, "Configuration filename");

            // parse commandline arguments
            CommandLineParser cmdParser = new DefaultParser();
            CommandLine arg = cmdParser.parse(options, args);

            if (arg.hasOption("f")) {
                configFile = arg.getOptionValue("f");
            } else {
                throw new RuntimeException("Missing Configuration file argument");
            }

            //execute command handler
            switch (command) {
                case "exportBoq":
                    exportBoq(configFile);
                    break;
                case "importBoq":
                    importBoQ(configFile);
                    break;
                default:
                    throw new RuntimeException("Missing Commmand argument");
            }

        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }
}


