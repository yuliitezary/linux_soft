package launchserver.auth.handler;

import launcher.helper.LogHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.auth.MySQLSourceConfig;

import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public final class SQLiteAuthHandler extends CachedAuthHandler
{
    private final Connection sqliteconnection;

    private final String uuidColumn;
    private final String usernameColumn;
    private final String accessTokenColumn;
    private final String serverIDColumn;

    // Prepared SQL queries
    private final String queryByUUIDSQL;
    private final String queryByUsernameSQL;
    private final String updateAuthSQL;
    private final String updateServerIDSQL;

    protected SQLiteAuthHandler(BlockConfigEntry block)
    {
        super(block);
        String table = VerifyHelper.verifyIDName(block.getEntryValue("table", StringConfigEntry.class));

        Connection v_sqliteconnection;
        try
        {
            v_sqliteconnection = DriverManager.getConnection("jdbc:sqlite:" + block.getEntryValue("path", StringConfigEntry.class));
        }
        catch (Exception e)
        {
            LogHelper.error("Error connecting to sqlite: ");
            e.printStackTrace();
            v_sqliteconnection = null;
        }

        this.sqliteconnection = v_sqliteconnection;
        this.uuidColumn = VerifyHelper.verifyIDName(block.getEntryValue("uuidColumn", StringConfigEntry.class));
        this.usernameColumn = VerifyHelper.verifyIDName(block.getEntryValue("usernameColumn", StringConfigEntry.class));
        this.accessTokenColumn = VerifyHelper.verifyIDName(block.getEntryValue("accessTokenColumn", StringConfigEntry.class));
        this.serverIDColumn = VerifyHelper.verifyIDName(block.getEntryValue("serverIDColumn", StringConfigEntry.class));
        this.queryByUUIDSQL = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s=? LIMIT 1", this.uuidColumn, this.usernameColumn, this.accessTokenColumn, this.serverIDColumn, table, this.uuidColumn);
        this.queryByUsernameSQL = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s=? LIMIT 1", this.uuidColumn, this.usernameColumn, this.accessTokenColumn, this.serverIDColumn, table, this.usernameColumn);
        this.updateAuthSQL = String.format("UPDATE %s SET %s=?, %s=?, %s=NULL WHERE %s=? LIMIT 1", table, this.usernameColumn, this.accessTokenColumn, this.serverIDColumn, this.uuidColumn);
        this.updateServerIDSQL = String.format("UPDATE %s SET %s=? WHERE %s=? LIMIT 1", table, this.serverIDColumn, this.uuidColumn);
    }

    @Override
    protected Entry fetchEntry(UUID uuid) throws IOException
    {
        return this.query(this.queryByUUIDSQL, uuid.toString());
    }

    @Override
    protected Entry fetchEntry(String s) throws IOException
    {
        return this.query(this.queryByUsernameSQL, s);
    }

    protected boolean updateAuth(UUID uuid, String username, String accessToken) throws IOException
    {
        try
        {
            Connection c = this.sqliteconnection;
            Throwable var5 = null;

            boolean var8;
            try
            {
                PreparedStatement s = c.prepareStatement(this.updateAuthSQL);
                Throwable var7 = null;

                try
                {
                    s.setString(1, username);
                    s.setString(2, accessToken);
                    s.setString(3, uuid.toString());
                    s.setQueryTimeout(5000);
                    var8 = s.executeUpdate() > 0;
                }
                catch (Throwable var33)
                {
                    var7 = var33;
                    throw var33;
                }
                finally
                {
                    if (s != null)
                    {
                        if (var7 != null)
                        {
                            try
                            {
                                s.close();
                            }
                            catch (Throwable var32)
                            {
                                var7.addSuppressed(var32);
                            }
                        }
                        else
                        {
                            s.close();
                        }
                    }
                }
            }
            catch (Throwable var35)
            {
                var5 = var35;
                throw var35;
            }
            finally
            {
                if (c != null)
                {
                    if (var5 != null)
                    {
                        try
                        {
                            c.close();
                        }
                        catch (Throwable var31)
                        {
                            var5.addSuppressed(var31);
                        }
                    }
                    else
                    {
                        c.close();
                    }
                }
            }
            return var8;
        }
        catch (SQLException e)
        {
            throw new IOException(e);
        }
    }

    protected boolean updateServerID(UUID uuid, String serverID) throws IOException
    {
        try
        {
            Connection c = this.sqliteconnection;
            Throwable var4 = null;

            boolean var7;
            try
            {
                PreparedStatement s = c.prepareStatement(this.updateServerIDSQL);
                Throwable var6 = null;

                try
                {
                    s.setString(1, serverID);
                    s.setString(2, uuid.toString());
                    s.setQueryTimeout(MySQLSourceConfig.TIMEOUT);
                    var7 = s.executeUpdate() > 0;
                }
                catch (Throwable var32)
                {
                    var6 = var32;
                    throw var32;
                }
                finally
                {
                    if (s != null)
                    {
                        if (var6 != null)
                        {
                            try
                            {
                                s.close();
                            }
                            catch (Throwable var31)
                            {
                                var6.addSuppressed(var31);
                            }
                        }
                        else
                        {
                            s.close();
                        }
                    }
                }
            }
            catch (Throwable var34)
            {
                var4 = var34;
                throw var34;
            }
            finally
            {
                if (c != null)
                {
                    if (var4 != null)
                    {
                        try
                        {
                            c.close();
                        }
                        catch (Throwable var30)
                        {
                            var4.addSuppressed(var30);
                        }
                    }
                    else
                    {
                        c.close();
                    }
                }
            }
            return var7;
        }
        catch (SQLException e)
        {
            throw new IOException(e);
        }
    }

    public void close()
    {
        try
        {
            this.sqliteconnection.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private Entry constructEntry(ResultSet set) throws SQLException
    {
        return set.next() ? new Entry(UUID.fromString(set.getString(this.uuidColumn)), set.getString(this.usernameColumn), set.getString(this.accessTokenColumn), set.getString(this.serverIDColumn)) : null;
    }

    private Entry query(String sql, String value) throws IOException
    {
        try
        {
            Connection c = this.sqliteconnection;
            Throwable var4 = null;

            Entry var9;
            try
            {
                PreparedStatement s = c.prepareStatement(sql);
                Throwable var6 = null;

                try
                {
                    s.setString(1, value);
                    s.setQueryTimeout(MySQLSourceConfig.TIMEOUT);
                    ResultSet set = s.executeQuery();
                    Throwable var8 = null;

                    try
                    {
                        var9 = this.constructEntry(set);
                    }
                    catch (Throwable var56)
                    {
                        var8 = var56;
                        throw var56;
                    }
                    finally
                    {
                        if (set != null)
                        {
                            if (var8 != null)
                            {
                                try
                                {
                                    set.close();
                                }
                                catch (Throwable var55)
                                {
                                    var8.addSuppressed(var55);
                                }
                            }
                            else
                            {
                                set.close();
                            }
                        }
                    }
                }
                catch (Throwable var58)
                {
                    var6 = var58;
                    throw var58;
                }
                finally
                {
                    if (s != null)
                    {
                        if (var6 != null)
                        {
                            try
                            {
                                s.close();
                            }
                            catch (Throwable var54)
                            {
                                var6.addSuppressed(var54);
                            }
                        }
                        else
                        {
                            s.close();
                        }
                    }
                }
            }
            catch (Throwable var60)
            {
                var4 = var60;
                throw var60;
            }
            finally
            {
                if (c != null)
                {
                    if (var4 != null)
                    {
                        try
                        {
                            c.close();
                        }
                        catch (Throwable var53)
                        {
                            var4.addSuppressed(var53);
                        }
                    }
                    else
                    {
                        c.close();
                    }
                }
            }
            return var9;
        }
        catch (SQLException e)
        {
            throw new IOException(e);
        }
    }
}
