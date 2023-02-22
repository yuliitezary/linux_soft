package launchserver.response.profile;

import launcher.client.PlayerProfile;
import launcher.client.PlayerProfile.Texture;
import launcher.helper.LogHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.response.Response;

import java.io.IOException;
import java.util.UUID;

public final class ProfileByUUIDResponse extends Response
{
    public ProfileByUUIDResponse(LaunchServer server, String ip, HInput input, HOutput output)
    {
        super(server, ip, input, output);
    }

    public static PlayerProfile getProfile(LaunchServer server, UUID uuid, String username)
    {
        // Get skin texture
        Texture skin;
        try
        {
            skin = server.config.textureProvider.getSkinTexture(uuid, username);
        }
        catch (Throwable exc)
        {
            LogHelper.error(new IOException(String.format("Can't get skin texture: '%s'", username), exc));
            skin = null;
        }

        // Get cloak texture
        Texture cloak;
        try
        {
            cloak = server.config.textureProvider.getCloakTexture(uuid, username);
        }
        catch (Throwable exc)
        {
            LogHelper.error(new IOException(String.format("Can't get cloak texture: '%s'", username), exc));
            cloak = null;
        }

        // Return combined profile
        return new PlayerProfile(uuid, username, skin, cloak);
    }

    @Override
    public void reply() throws IOException
    {
        UUID uuid = input.readUUID();
        debug("UUID: " + uuid);

        // Verify has such profile
        String username = server.config.authHandler.uuidToUsername(uuid);
        if (username == null)
        {
            output.writeBoolean(false);
            return;
        }

        // Write profile
        output.writeBoolean(true);
        getProfile(server, uuid, username).write(output);
    }
}
