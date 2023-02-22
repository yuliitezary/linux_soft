package launchserver.response.profile;

import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.response.Response;

import java.io.IOException;
import java.util.UUID;

public final class ProfileByUsernameResponse extends Response
{
    public ProfileByUsernameResponse(LaunchServer server, String ip, HInput input, HOutput output)
    {
        super(server, ip, input, output);
    }

    public static void writeProfile(LaunchServer server, HOutput output, String username) throws IOException
    {
        UUID uuid = server.config.authHandler.usernameToUUID(username);
        if (uuid == null)
        {
            output.writeBoolean(false);
            return;
        }

        // Write profile
        output.writeBoolean(true);
        ProfileByUUIDResponse.getProfile(server, uuid, username).write(output);
    }

    @Override
    public void reply() throws IOException
    {
        String username = VerifyHelper.verifyUsername(input.readString(64));
        debug("Username: " + username);

        // Write response
        writeProfile(server, output, username);
    }
}
