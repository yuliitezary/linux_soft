package launchserver.auth.provider;

import launcher.helper.CommonHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.ListConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.auth.AuthException;
import launchserver.auth.MariaDBSourceConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class MariaDBBcryptAuthProvider extends AuthProvider
{
    private final MariaDBSourceConfig mySQLHolder;
    private final String query;
    private final String[] queryParams;

    MariaDBBcryptAuthProvider(BlockConfigEntry block)
    {
        super(block);
        mySQLHolder = new MariaDBSourceConfig("authProviderPool", block);

        query = VerifyHelper.verify(block.getEntryValue("query", StringConfigEntry.class),
                VerifyHelper.NOT_EMPTY, "MySQL query can't be empty");
        queryParams = block.getEntry("queryParams", ListConfigEntry.class).
                stream(StringConfigEntry.class).toArray(String[]::new);
    }

    @Override
    public AuthProviderResult auth(String login, String password, String ip) throws SQLException, AuthException
    {
        try (Connection c = mySQLHolder.getConnection(); PreparedStatement s = c.prepareStatement(query))
        {
            String[] replaceParams = {"login", login, "password", password, "ip", ip};
            for (int i = 0; i < queryParams.length; i++)
            {
                s.setString(i + 1, CommonHelper.replace(queryParams[i], replaceParams));
            }

            // Execute SQL query
            s.setQueryTimeout(MariaDBSourceConfig.TIMEOUT);
            try (ResultSet set = s.executeQuery())
            {
                return set.next() ? BCrypt.checkpw(password, "$2a" + set.getString(1).substring(3)) ? new AuthProviderResult(set.getString(2), SecurityHelper.randomStringToken()) : authError("Incorrect username or password") : authError("Incorrect username or password");
            }
        }
    }

    @Override
    public void close()
    {
        mySQLHolder.close();
    }
}
