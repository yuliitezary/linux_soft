package launchserver.texture;

import launcher.client.PlayerProfile.Texture;
import launcher.serialize.config.entry.BlockConfigEntry;

import java.util.UUID;

public class ElyByTextureProvider extends TextureProvider
{
    protected CacheTextureProvider cacheTextureProvider = new CacheTextureProvider();
    public ElyByTextureProvider(BlockConfigEntry block)
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
        return cacheTextureProvider.getCached(uuid, "https://authserver.ely.by/api/users/profiles/minecraft/", "http://skinsystem.ely.by/profile/", "ElyBy").skin;
    }

    @Override
    public synchronized Texture getCloakTexture(UUID uuid, String username)
    {
        return cacheTextureProvider.getCached(uuid, "https://authserver.ely.by/api/users/profiles/minecraft/", "http://skinsystem.ely.by/profile/", "ElyBy").cloak;
    }
}
