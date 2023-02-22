package launchserver.response.auth;

import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.request.auth.JoinServerRequest;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.auth.AuthException;
import launchserver.response.Response;

import java.io.IOException;

public final class JoinServerResponse extends Response
{
    public JoinServerResponse(LaunchServer server, String ip, HInput input, HOutput output)
    {
        super(server, ip, input, output);
    }

    @Override
    public void reply() throws IOException
    {
        String username = VerifyHelper.verifyUsername(input.readString(64));
        int length = input.readInt();
        String accessToken = SecurityHelper.verifyToken(input.readASCII(-length));
        String serverID = JoinServerRequest.verifyServerID(input.readASCII(41)); // With minus sign

        // Try join server with auth handler
        debug("Username: '%s', Access token: %s, Server ID: %s", username, accessToken, serverID);
        boolean success;
        try
        {
            success = server.config.authHandler.joinServer(username, accessToken, serverID);
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

        // Write response
        output.writeBoolean(success);
    }
}
