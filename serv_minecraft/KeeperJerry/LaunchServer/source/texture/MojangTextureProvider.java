package launchserver.texture;

import launcher.client.PlayerProfile.Texture;
import launcher.serialize.config.entry.BlockConfigEntry;

import java.util.UUID;

public final class MojangTextureProvider extends TextureProvider
{
    public MojangTextureProvider(BlockConfigEntry block)
    {
        super(block);
    }

    protected CacheTextureProvider cacheTextureProvider = new CacheTextureProvider();

    @Override
    public void close()
    {
        // Do nothing
    }

    @Override
    public synchronized Texture getSkinTexture(UUID uuid, String username)
    {
        return cacheTextureProvider.getCached(uuid, username, "https://sessionserver.mojang.com/session/minecraft/profile/", "Mojang").skin;
    }

    @Override
    public synchronized Texture getCloakTexture(UUID uuid, String username)
    {
        return cacheTextureProvider.getCached(uuid, username, "https://sessionserver.mojang.com/session/minecraft/profile/", "Mojang").cloak;
    }
}
