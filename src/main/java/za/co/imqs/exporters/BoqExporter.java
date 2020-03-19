package za.co.imqs.exporters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import za.co.imqs.database.Database;
import za.co.imqs.exceptions.DuplicateRecordException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

import static java.lang.System.out;


/**
 * Created by gerhardv on 2020-01-28.
 * <p>
 * Description:
 * -----------
 */

public class BoqExporter {

    private Database db;
    private String targetFilename;      /* name of the exported file including the path */

    public BoqExporter(String configFilename) throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode config = mapper.readTree(new File(configFilename));
        targetFilename = config.get("exportFilename").asText();

        final JsonNode dbParams = config.get("dbParams");

        db = new Database(dbParams.toString());
    }


    private static void log(String Msg) {

        out.println(Msg);
    }


    /**
     * @return Returns the number of records in the AssetPolicyVAR table
     * @throws SQLException
     */
    private int getRecordCount(Connection connection) throws SQLException {

        final Statement statement = connection.createStatement();
        final String queryString = "SELECT COUNT(*) as NoOfRecords FROM AssetpolicyVAR";
        final ResultSet rs = statement.executeQuery(queryString);

        return rs.getInt("NoOfRecords");
    }


    /**
     * Calls a stored procedure to retrieve the records to export based on the export options.
     *
     * @return: Return a ResultSet that is passed to the CVSWriter.
     */
    private ResultSet getExportResultSet(Connection connection) throws SQLException {

        // prepare stored procedure
        final CallableStatement cs = connection.prepareCall("{call PolicyVARExportForBOQ}");

        cs.execute();

        // return result set
        return cs.getResultSet();
    }


    /**
     * Check if any duplicate records exists in the policy, if so throws an exception.
     *
     * @return Returns true if duplicate records exists.
     * @throws SQLException
     */
    private boolean noDuplicateRecords(Connection connection) throws SQLException {

        // prepare stored procedure                                                                                      .
        final CallableStatement cs = connection.prepareCall("{call AssetPolicyVARDuplicates}");

        cs.execute();
        ResultSet rs = cs.getResultSet();
        final boolean hasResults = !rs.next();
        rs.close();

        return hasResults;
    }


    /**
     * @throws SQLException
     */
    private void exportToCSV(Connection connection) throws SQLException {

        try {
            log("BoqExporter - Retrieving BOQ records...");

            // query database
            final ResultSet rs = getExportResultSet(connection);

            // write CSV file
            log("BoqExporter - Exporting to CSV...");
            writeResultSetToCSV(rs);
            rs.close();

            log("BoqExporter - Export complete. Filename = '" + targetFilename + "'");
        } catch (Exception e) {
            log(e.getMessage());
        }
    }


    /**
     * @param resultSet Result set to be written to file.
     * @throws SQLException
     * @throws IOException
     */
    private void writeResultSetToCSV(final ResultSet resultSet) throws SQLException, IOException {

        final String csvFileName = targetFilename;

        try (final CSVPrinter printer = CSVFormat.EXCEL.withHeader(resultSet).print(new FileWriter(csvFileName))) {
            printer.printRecords(resultSet);
            printer.flush();
        }
    }

    /**
     * @throws SQLException
     */
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
            e.printStackTrace();
            log(e.getMessage());
        }
    }
}
