package launchserver.auth.provider;

import java.util.UUID;

public class AuthlibAuthProviderResult extends AuthProviderResult
{
    public final UUID uuid;
    public final String launcherToken;

    AuthlibAuthProviderResult(String username, String accessToken, UUID uuid, String launcherToken)
    {
        super(username, accessToken);
        this.uuid = uuid;
        this.launcherToken = launcherToken;
    }
}
