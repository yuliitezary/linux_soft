package launchserver.texture;

import launcher.client.PlayerProfile.Texture;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;

import java.util.UUID;

public class AuthlibTextureProvider extends TextureProvider
{
    // Instance
    private final String setProfileURL;
    protected CacheTextureProvider cacheTextureProvider = new CacheTextureProvider();

    public AuthlibTextureProvider(BlockConfigEntry block)
    {
        super(block);
        setProfileURL = block.getEntryValue("profileURL", StringConfigEntry.class);
    }

    @Override
    public void close()
    {
        // Do nothing
    }

    @Override
    public synchronized Texture getSkinTexture(UUID uuid, String username)
    {
        return cacheTextureProvider.getCached(uuid, username, setProfileURL, "Authlib").skin;
    }

    @Override
    public synchronized Texture getCloakTexture(UUID uuid, String username)
    {
        return cacheTextureProvider.getCached(uuid, username, setProfileURL, "Authlib").cloak;
    }
}
