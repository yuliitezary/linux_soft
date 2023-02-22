package launcher.request.auth;

import launcher.Launcher.Config;
import launcher.LauncherAPI;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.request.Request;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;

import java.io.IOException;
import java.util.regex.Pattern;

public final class JoinServerRequest extends Request<Boolean>
{
    private static final Pattern SERVERID_PATTERN = Pattern.compile("-?[0-9a-f]{1,40}");

    // Instance
    private final String username;
    private final String accessToken;
    private final String serverID;

    @LauncherAPI
    public JoinServerRequest(Config config, String username, String accessToken, String serverID)
    {
        super(config);
        this.username = VerifyHelper.verifyUsername(username);
        this.accessToken = SecurityHelper.verifyToken(accessToken);
        this.serverID = verifyServerID(serverID);
    }

    @LauncherAPI
    public JoinServerRequest(String username, String accessToken, String serverID)
    {
        this(null, username, accessToken, serverID);
    }

    @LauncherAPI
    public static boolean isValidServerID(CharSequence serverID)
    {
        return SERVERID_PATTERN.matcher(serverID).matches();
    }

    @LauncherAPI
    public static String verifyServerID(String serverID)
    {
        return VerifyHelper.verify(serverID, JoinServerRequest::isValidServerID,
                String.format("Invalid server ID: '%s'", serverID));
    }

    @Override
    public Type getType()
    {
        return Type.JOIN_SERVER;
    }

    @Override
    protected Boolean requestDo(HInput input, HOutput output) throws IOException
    {
        output.writeString(username, 64);
        output.writeInt(accessToken.length());
        output.writeASCII(accessToken, -accessToken.length());
        output.writeASCII(serverID, 41); // 1 char for minus sign
        output.flush();

        // Read response
        readError(input);
        return input.readBoolean();
    }
}
