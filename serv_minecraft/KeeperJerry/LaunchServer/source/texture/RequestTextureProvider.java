package launchserver.texture;

import launcher.client.ClientLauncher;
import launcher.client.PlayerProfile.Texture;
import launcher.helper.CommonHelper;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

public final class RequestTextureProvider extends TextureProvider
{
    private static final UUID ZERO_UUID = new UUID(0, 0);

    // Instance
    private final String skinURL;
    private final String cloakURL;

    public RequestTextureProvider(BlockConfigEntry block)
    {
        super(block);
        skinURL = block.getEntryValue("skinsURL", StringConfigEntry.class);
        cloakURL = block.getEntryValue("cloaksURL", StringConfigEntry.class);

        // Verify
        IOHelper.verifyURL(getTextureURL(skinURL, ZERO_UUID, "skinUsername"));
        IOHelper.verifyURL(getTextureURL(cloakURL, ZERO_UUID, "cloakUsername"));
    }

    private static Texture getTexture(String url, boolean cloak) throws IOException
    {
        LogHelper.debug("Getting texture: '%s'", url);
        try
        {
            return new Texture(url, cloak);
        }
        catch (FileNotFoundException ignored)
        {
            if (LogHelper.isDebugEnabled()) LogHelper.subDebug("Texture not set or not found! Maybe problem with you proxy!");
            return null; // Simply not found
        }
    }

    private static String getTextureURL(String url, UUID uuid, String username)
    {
        return CommonHelper.replace(url, "username", IOHelper.urlEncode(username),
                "uuid", IOHelper.urlEncode(uuid.toString()), "hash", IOHelper.urlEncode(ClientLauncher.toHash(uuid)));
    }

    @Override
    public void close()
    {
        // Do nothing
    }

    @Override
    public Texture getSkinTexture(UUID uuid, String username) throws IOException
    {
        return getTexture(getTextureURL(skinURL, uuid, username), false);
    }

    @Override
    public Texture getCloakTexture(UUID uuid, String username) throws IOException
    {
        return getTexture(getTextureURL(cloakURL, uuid, username), true);
    }
}
