package launchserver.response.auth;

import launcher.helper.LogHelper;
import launcher.helper.VerifyHelper;
import launcher.request.auth.JoinServerRequest;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.auth.AuthException;
import launchserver.response.Response;
import launchserver.response.profile.ProfileByUUIDResponse;

import java.io.IOException;
import java.util.UUID;

public final class CheckServerResponse extends Response
{
    public CheckServerResponse(LaunchServer server, String ip, HInput input, HOutput output)
    {
        super(server, ip, input, output);
    }

    @Override
    public void reply() throws IOException
    {
        String username = VerifyHelper.verifyUsername(input.readString(64));
        String serverID = JoinServerRequest.verifyServerID(input.readASCII(41)); // With minus sign
        debug("Username: %s, Server ID: %s", username, serverID);

        // Try check server with auth handler
        UUID uuid;
        try
        {
            uuid = server.config.authHandler.checkServer(username, serverID);
        }
        catch (AuthException e)
        {
            requestError(e.getMessage());
            return;
        }
        catch (Throwable exc)
        {
            LogHelper.error(exc);
            requestError("Internal auth handler error");
            return;
        }
        writeNoError(output);

        // Write profile and UUID
        output.writeBoolean(uuid != null);
        if (uuid != null)
        {
            ProfileByUUIDResponse.getProfile(server, uuid, username).write(output);
        }
    }
}
