package za.co.imqs.database;

import lombok.Data;

/**
 * Created by gerhardv on 2020-01-30.
 */

@Data
public class DatabaseParams {
    private String driverClass;
    private String jdbcUrl;
    private String username;
    private String password;
}
