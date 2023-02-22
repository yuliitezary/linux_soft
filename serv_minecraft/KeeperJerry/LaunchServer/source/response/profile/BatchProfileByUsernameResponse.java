package launchserver.response.profile;

import launcher.helper.VerifyHelper;
import launcher.request.uuid.BatchProfileByUsernameRequest;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.response.Response;

import java.io.IOException;
import java.util.Arrays;

public final class BatchProfileByUsernameResponse extends Response
{
    public BatchProfileByUsernameResponse(LaunchServer server, String ip, HInput input, HOutput output)
    {
        super(server, ip, input, output);
    }

    @Override
    public void reply() throws IOException
    {
        String[] usernames = new String[input.readLength(BatchProfileByUsernameRequest.MAX_BATCH_SIZE)];
        for (int i = 0; i < usernames.length; i++)
        {
            usernames[i] = VerifyHelper.verifyUsername(input.readString(64));
        }
        debug("Usernames: " + Arrays.toString(usernames));

        // Respond with profiles array
        for (String username : usernames)
        {
            ProfileByUsernameResponse.writeProfile(server, output, username);
        }
    }
}
