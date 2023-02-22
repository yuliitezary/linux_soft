package launchserver.texture;

import launcher.client.PlayerProfile.Texture;
import launcher.serialize.config.entry.BlockConfigEntry;

import java.util.UUID;

public class MineSocialTextureProvider extends TextureProvider
{
    protected CacheTextureProvider cacheTextureProvider = new CacheTextureProvider();
    public MineSocialTextureProvider(BlockConfigEntry block)
    {
        super(block);
    }

    @Override
    public void close()
    {
        // Do nothing
    }

    @Override
    public synchronized Texture getSkinTexture(UUID uuid, String username)
    {
        return cacheTextureProvider.getCached(uuid, username, "https://sessionserver.minesocial.net/session/minecraft/profile/", "MineSocial").skin;
    }

    @Override
    public synchronized Texture getCloakTexture(UUID uuid, String username)
    {
        return cacheTextureProvider.getCached(uuid, username, "https://sessionserver.minesocial.net/session/minecraft/profile/", "MineSocial").cloak;
    }
}
