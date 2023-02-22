package launchserver.texture;

import launcher.LauncherAPI;
import launcher.client.PlayerProfile.Texture;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.ConfigObject;
import launcher.serialize.config.entry.BlockConfigEntry;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TextureProvider extends ConfigObject implements AutoCloseable
{
    private static final Map<String, Adapter<TextureProvider>> TEXTURE_PROVIDERS = new ConcurrentHashMap<>(2);

    static
    {
        // Default TextureProviders
        registerProvider("void", VoidTextureProvider::new);
        registerProvider("delegate", DelegateTextureProvider::new);
        registerProvider("request", RequestTextureProvider::new);

        // Authlib TextureProviders
        registerProvider("authlib", AuthlibTextureProvider::new);
        registerProvider("authlib-injector", AuthlibInjectorTextureProvider::new);
        registerProvider("minesocial", MineSocialTextureProvider::new);
        registerProvider("elyby", ElyByTextureProvider::new);
        registerProvider("mojang", MojangTextureProvider::new);

    }

    @LauncherAPI
    protected TextureProvider(BlockConfigEntry block)
    {
        super(block);
    }

    @LauncherAPI
    public static TextureProvider newProvider(String name, BlockConfigEntry block)
    {
        VerifyHelper.verifyIDName(name);
        Adapter<TextureProvider> authHandlerAdapter = VerifyHelper.getMapValue(TEXTURE_PROVIDERS, name,
                String.format("Unknown texture provider: '%s'", name));
        return authHandlerAdapter.convert(block);
    }

    @LauncherAPI
    public static void registerProvider(String name, Adapter<TextureProvider> adapter)
    {
        VerifyHelper.putIfAbsent(TEXTURE_PROVIDERS, name, Objects.requireNonNull(adapter, "adapter"),
                String.format("Texture provider has been already registered: '%s'", name));
    }

    @Override
    public abstract void close() throws IOException;

    @LauncherAPI
    public abstract Texture getCloakTexture(UUID uuid, String username) throws IOException;

    @LauncherAPI
    public abstract Texture getSkinTexture(UUID uuid, String username) throws IOException;
}
