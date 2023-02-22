package launchserver.texture;

import launcher.client.PlayerProfile.Texture;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;

import java.util.UUID;

public class AuthlibInjectorTextureProvider extends TextureProvider
{
    private final String urlApiInjector;
    protected CacheTextureProvider cacheTextureProvider = new CacheTextureProvider();

    public AuthlibInjectorTextureProvider(BlockConfigEntry block)
    {
        super(block);
        urlApiInjector = block.getEntryValue("urlApiInjector", StringConfigEntry.class);
    }

    @Override
    public void close()
    {
        // Do nothing
    }

    @Override
    public synchronized Texture getSkinTexture(UUID uuid, String username)
    {
        return cacheTextureProvider.getCached(uuid, username, urlApiInjector + "/sessionserver/session/minecraft/profile/", "Authlib-Injector").skin;
    }

    @Override
    public synchronized Texture getCloakTexture(UUID uuid, String username)
    {
        return cacheTextureProvider.getCached(uuid, username, urlApiInjector + "/sessionserver/session/minecraft/profile/", "Authlib-Injector").cloak;
    }
}
