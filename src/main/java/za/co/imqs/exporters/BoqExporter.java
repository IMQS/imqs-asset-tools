package za.co.imqs.exporters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.imqs.database.Database;
import za.co.imqs.exceptions.DuplicateRecordException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

/**
 * Created by gerhardv on 2020-01-28.
 * <p>
 * Description:
 * -----------
 * boq-export-config.json example:
 *  {
 *     "dbParams": {
 *       "driverClass": "com.microsoft.sqlserver.jdbc.SQLServerDriver",
 *       "jdbcUrl": "jdbc:sqlserver://localhost:1433;databaseName=IMQS_Asset_BuffaloCity_20180510",
 *       "username": "sa",
 *       "password": "password"
 *     },
 *     "exportFilename": "D:\\Dump\\IMQS\\Exports\\test\\export_java.csv"
 *  }
 */

public class BoqExporter {
    private final Database db;
    private final ObjectMapper mapper;
    private final String targetFilename;      /* name of the exported file including the path */

    Logger logger = LoggerFactory.getLogger(BoqExporter.class);

    public BoqExporter(String configFilename) throws Exception {
        mapper = new ObjectMapper();

        final JsonNode config = mapper.readTree(new File(configFilename));
        targetFilename = config.get("exportFilename").asText();

        //setup the database connection
        final JsonNode dbParams = config.get("dbParams");
        db = new Database(dbParams.toString(), mapper);
    }

    public void execute() throws SQLException {
        try {
            try (Connection conn = db.getDbConnection()) {
                //if no duplicate records exists continue with the export
                if (noDuplicateRecords(conn)) {
                    exportToCSV(conn);
                } else {
                    throw new DuplicateRecordException("Unable to export BOQ from AssetPolicyVAR because of duplicate records.");
                }
            }
        } catch (Exception e) {
            logger.error("Exception in BoqExporter.Execute. " + e.getMessage(), e);
        }
    }

    /**
     * @return Returns the number of records in the AssetPolicyVAR table
     * @throws SQLException
     */
    private int getRecordCount(Connection connection) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            final String queryString = "SELECT COUNT(*) as NoOfRecords FROM AssetpolicyVAR";
            final ResultSet rs = statement.executeQuery(queryString);

            return rs.getInt("NoOfRecords");
        }
    }

    /**
     * Calls a stored procedure to retrieve the records to export based on the export options.
     *
     * @return: Return a ResultSet that is passed to the CVSWriter.
     */
    private ResultSet getExportResultSet(Connection connection) throws SQLException {
        final CallableStatement cs = connection.prepareCall("{call PolicyVARExportForBOQ}");
        cs.execute();
        // return result set
        return cs.getResultSet();
    }
    
    /**
     * Check if there are no duplicate records in the policy
     *
     * @return Returns true if no duplicate records exists.
     * @throws SQLException
     */
    private boolean noDuplicateRecords(Connection connection) throws SQLException {
        try (final CallableStatement cs = connection.prepareCall("{call AssetPolicyVARDuplicates}")) {
            cs.execute();
            ResultSet rs = cs.getResultSet();
            return !rs.next();
        }
    }

    private void exportToCSV(Connection connection) throws SQLException, IOException {
        logger.info("BoqExporter - Retrieving BOQ records...");

        try (final ResultSet rs = getExportResultSet(connection)) {
            // write CSV file
            logger.info("BoqExporter - Exporting to CSV...");
            writeResultSetToCSV(rs);
            logger.info("BoqExporter - Export complete. Filename = '" + targetFilename + "'");
        }
    }

    /**
     * @param resultSet Result set to be written to file.
     * @throws SQLException
     * @throws IOException
     */
    private void writeResultSetToCSV(final ResultSet resultSet) throws SQLException, IOException {
        try (final CSVPrinter printer = CSVFormat.EXCEL.withHeader(resultSet).print(new FileWriter(targetFilename))) {
            printer.printRecords(resultSet);
            printer.flush();
        }
    }
}
