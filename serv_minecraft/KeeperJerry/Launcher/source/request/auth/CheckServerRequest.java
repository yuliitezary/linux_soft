package launcher.request.auth;

import launcher.Launcher.Config;
import launcher.LauncherAPI;
import launcher.client.PlayerProfile;
import launcher.helper.VerifyHelper;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

import java.io.IOException;

public final class CheckServerRequest extends Request<PlayerProfile>
{
    private final String username;
    private final String serverID;

    @LauncherAPI
    public CheckServerRequest(Config config, String username, String serverID)
    {
        super(config);
        this.username = VerifyHelper.verifyUsername(username);
        this.serverID = JoinServerRequest.verifyServerID(serverID);
    }

    @LauncherAPI
    public CheckServerRequest(String username, String serverID)
    {
        this(null, username, serverID);
    }

    @Override
    public Type getType()
    {
        return Type.CHECK_SERVER;
    }

    @Override
    protected PlayerProfile requestDo(HInput input, HOutput output) throws IOException
    {
        output.writeString(username, 64);
        output.writeASCII(serverID, 41); // 1 char for minus sign
        output.flush();

        // Read response
        readError(input);
        return input.readBoolean() ? new PlayerProfile(input) : null;
    }
}
