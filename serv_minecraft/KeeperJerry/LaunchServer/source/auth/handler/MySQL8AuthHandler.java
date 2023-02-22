package launchserver.auth.handler;

import launcher.helper.LogHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.BooleanConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.auth.MySQL8SourceConfig;

import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public final class MySQL8AuthHandler extends CachedAuthHandler
{
    private final MySQL8SourceConfig mySQL8Holder;
    private final String uuidColumn;
    private final String usernameColumn;
    private final String accessTokenColumn;
    private final String serverIDColumn;

    // Prepared SQL queries
    private final String queryByUUIDSQL;
    private final String queryByUsernameSQL;
    private final String updateAuthSQL;
    private final String updateServerIDSQL;

    MySQL8AuthHandler(BlockConfigEntry block)
    {
        super(block);
        mySQL8Holder = new MySQL8SourceConfig("authHandlerPool", block);

        // Read query params
        String table = VerifyHelper.verifyIDName(
                block.getEntryValue("table", StringConfigEntry.class));
        uuidColumn = VerifyHelper.verifyIDName(
                block.getEntryValue("uuidColumn", StringConfigEntry.class));
        usernameColumn = VerifyHelper.verifyIDName(
                block.getEntryValue("usernameColumn", StringConfigEntry.class));
        accessTokenColumn = VerifyHelper.verifyIDName(
                block.getEntryValue("accessTokenColumn", StringConfigEntry.class));
        serverIDColumn = VerifyHelper.verifyIDName(
                block.getEntryValue("serverIDColumn", StringConfigEntry.class));

        // Prepare SQL queries
        queryByUUIDSQL = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s=? LIMIT 1",
                uuidColumn, usernameColumn, accessTokenColumn, serverIDColumn, table, uuidColumn);
        queryByUsernameSQL = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s=? LIMIT 1",
                uuidColumn, usernameColumn, accessTokenColumn, serverIDColumn, table, usernameColumn);
        updateAuthSQL = String.format("UPDATE %s SET %s=?, %s=?, %s=NULL WHERE %s=? LIMIT 1",
                table, usernameColumn, accessTokenColumn, serverIDColumn, uuidColumn);
        updateServerIDSQL = String.format("UPDATE %s SET %s=? WHERE %s=? LIMIT 1",
                table, serverIDColumn, uuidColumn);
    }

    @Override
    public void close()
    {
        mySQL8Holder.close();
    }

    @Override
    protected Entry fetchEntry(String username) throws IOException
    {
        return query(queryByUsernameSQL, username);
    }

    @Override
    protected Entry fetchEntry(UUID uuid) throws IOException
    {
        return query(queryByUUIDSQL, uuid.toString());
    }

    @Override
    protected boolean updateAuth(UUID uuid, String username, String accessToken) throws IOException
    {
        try (Connection c = mySQL8Holder.getConnection(); PreparedStatement s = c.prepareStatement(updateAuthSQL))
        {
            s.setString(1, username); // Username case
            s.setString(2, accessToken);
            s.setString(3, uuid.toString());

            // Execute update
            s.setQueryTimeout(MySQL8SourceConfig.TIMEOUT);
            return s.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    protected boolean updateServerID(UUID uuid, String serverID) throws IOException
    {
        try (Connection c = mySQL8Holder.getConnection(); PreparedStatement s = c.prepareStatement(updateServerIDSQL))
        {
            s.setString(1, serverID);
            s.setString(2, uuid.toString());

            // Execute update
            s.setQueryTimeout(MySQL8SourceConfig.TIMEOUT);
            return s.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            throw new IOException(e);
        }
    }

    private Entry constructEntry(ResultSet set) throws SQLException
    {
        return set.next() ? new Entry(UUID.fromString(set.getString(uuidColumn)), set.getString(usernameColumn),
                set.getString(accessTokenColumn), set.getString(serverIDColumn)) : null;
    }

    private Entry query(String sql, String value) throws IOException
    {
        try (Connection c = mySQL8Holder.getConnection(); PreparedStatement s = c.prepareStatement(sql))
        {
            s.setString(1, value);

            // Execute query
            s.setQueryTimeout(MySQL8SourceConfig.TIMEOUT);
            try (ResultSet set = s.executeQuery())
            {
                return constructEntry(set);
            }
        }
        catch (SQLException e)
        {
            throw new IOException(e);
        }
    }
}
