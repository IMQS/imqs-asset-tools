package za.co.imqs.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.sql.*;

/**
 * Created by gerhardv on 2020-01-28.
 * Database Class to establish a database connection based on the dbParams passed in the constructor.
 */

@Data
public class Database {
    private DatabaseParams params;
    //public Connection connection;

    public Database(String dbParams, ObjectMapper mapper) throws Exception {
        params = new DatabaseParams();
        // map the connection properties
        params = mapper.readValue(dbParams, DatabaseParams.class);

    }
    
    public Connection getDbConnection() throws Exception {
        return DriverManager.getConnection(params.getJdbcUrl(), params.getUsername(), params.getPassword());
    }
}
