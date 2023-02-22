package launchserver.auth;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLSourceConfig
{
    void close();
    Connection getConnection() throws SQLException;
}
