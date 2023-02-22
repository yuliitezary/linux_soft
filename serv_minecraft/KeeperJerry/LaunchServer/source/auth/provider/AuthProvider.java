package launchserver.auth.provider;

import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.ConfigObject;
import launcher.serialize.config.entry.BlockConfigEntry;
import launchserver.auth.AuthException;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AuthProvider extends ConfigObject implements AutoCloseable
{
    private static final Map<String, Adapter<AuthProvider>> AUTH_PROVIDERS = new ConcurrentHashMap<>(15);

    static
    {
        // Default Providers
        registerProvider("accept", AcceptAuthProvider::new);
        registerProvider("reject", RejectAuthProvider::new);
        registerProvider("delegate", DelegateAuthProvider::new);
        registerProvider("file", FileAuthProvider::new);
        registerProvider("json", JsonAuthProvider::new);
        registerProvider("request", RequestAuthProvider::new);

        // SQL and NoSQL Providers
        registerProvider("mysql", MySQLAuthProvider::new);
        registerProvider("mysql-bcrypt", MySQLBcryptAuthProvider::new);
        registerProvider("mysql-8", MySQL8AuthProvider::new);
        registerProvider("mysql-8-bcrypt", MySQL8BcryptAuthProvider::new);
        registerProvider("mariadb", MariaDBAuthProvider::new);
        registerProvider("mariadb-bcrypt", MariaDBBcryptAuthProvider::new);
        registerProvider("postgresql", PostgreSQLAuthProvider::new);
        registerProvider("postgresql-bcrypt", PostgreSQLBcryptAuthProvider::new);
        registerProvider("sqlite", SQLiteAuthProvider::new);

        // Authlib Providers
        registerProvider("authlib", AuthlibAuthProvider::new);
        registerProvider("authlib-injector", AuthlibInjectorAuthProvider::new);
        registerProvider("mojang", MojangAuthProvider::new);
        registerProvider("minesocial", MineSocialAuthProvider::new);
        registerProvider("elyby", ElyByAuthProvider::new);
    }

    @LauncherAPI
    protected AuthProvider(BlockConfigEntry block)
    {
        super(block);
    }

    @LauncherAPI
    public static AuthProviderResult authError(String message) throws AuthException
    {
        throw new AuthException(message);
    }

    @LauncherAPI
    public static AuthProvider newProvider(String name, BlockConfigEntry block)
    {
        VerifyHelper.verifyIDName(name);
        Adapter<AuthProvider> authHandlerAdapter = VerifyHelper.getMapValue(AUTH_PROVIDERS, name,
                String.format("Unknown auth provider: '%s'", name));
        return authHandlerAdapter.convert(block);
    }

    @LauncherAPI
    public static void registerProvider(String name, Adapter<AuthProvider> adapter)
    {
        VerifyHelper.putIfAbsent(AUTH_PROVIDERS, name, Objects.requireNonNull(adapter, "adapter"),
                String.format("Auth provider has been already registered: '%s'", name));
    }

    @Override
    public abstract void close() throws IOException;

    @LauncherAPI
    public abstract AuthProviderResult auth(String login, String password, String ip) throws Throwable;
}
