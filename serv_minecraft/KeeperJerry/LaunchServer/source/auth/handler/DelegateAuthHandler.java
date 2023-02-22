package launchserver.auth.handler;

import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.entry.BlockConfigEntry;
import launchserver.auth.provider.AuthProviderResult;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class DelegateAuthHandler extends AuthHandler
{
    private volatile AuthHandler delegate;

    DelegateAuthHandler(BlockConfigEntry block)
    {
        super(block);
    }

    @Override
    public UUID auth(AuthProviderResult authResult) throws IOException
    {
        return getDelegate().auth(authResult);
    }

    @Override
    public UUID checkServer(String username, String serverID) throws IOException
    {
        return getDelegate().checkServer(username, serverID);
    }

    @Override
    public void close() throws IOException
    {
        AuthHandler delegate = this.delegate;
        if (delegate != null)
        {
            delegate.close();
        }
    }

    @Override
    public boolean joinServer(String username, String accessToken, String serverID) throws IOException
    {
        return getDelegate().joinServer(username, accessToken, serverID);
    }

    @Override
    public UUID usernameToUUID(String username) throws IOException
    {
        return getDelegate().usernameToUUID(username);
    }

    @Override
    public String uuidToUsername(UUID uuid) throws IOException
    {
        return getDelegate().uuidToUsername(uuid);
    }

    private AuthHandler getDelegate()
    {
        return VerifyHelper.verify(delegate, Objects::nonNull, "Delegate auth handler wasn't set");
    }

    @LauncherAPI
    public void setDelegate(AuthHandler delegate)
    {
        this.delegate = delegate;
    }
}
