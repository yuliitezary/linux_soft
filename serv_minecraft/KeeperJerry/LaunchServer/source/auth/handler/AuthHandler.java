package launchserver.auth.handler;

import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.ConfigObject;
import launcher.serialize.config.entry.BlockConfigEntry;
import launchserver.auth.AuthException;
import launchserver.auth.provider.AuthProviderResult;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AuthHandler extends ConfigObject implements AutoCloseable
{
    private static final Map<String, Adapter<AuthHandler>> AUTH_HANDLERS = new ConcurrentHashMap<>(10);

    static
    {
        // Default Handlers
        registerHandler("memory", MemoryAuthHandler::new);
        registerHandler("delegate", DelegateAuthHandler::new);
        registerHandler("binaryFile", BinaryFileAuthHandler::new);
        registerHandler("textFile", TextFileAuthHandler::new);
        registerHandler("json", JsonAuthHandler::new);

        // SQL and NoSQL Handlers
        registerHandler("mysql", MySQLAuthHandler::new);
        registerHandler("mysql-8", MySQL8AuthHandler::new);
        registerHandler("mariadb", MariaDBAuthHandler::new);
        registerHandler("postgresql", PostgreSQLAuthHandler::new);
        registerHandler("sqlite", SQLiteAuthHandler::new);

        // Authlib Handlers
        registerHandler("authlib", AuthlibAuthHandler::new);
        registerHandler("authlib-injector", AuthlibInjectorAuthHandler::new);
        registerHandler("mojang", MojangAuthHandler::new);
        registerHandler("minesocial", MineSocialAuthHandler::new);
        registerHandler("elyby", ElyByAuthHandler::new);
    }

    @LauncherAPI
    protected AuthHandler(BlockConfigEntry block)
    {
        super(block);
    }

    @LauncherAPI
    public static UUID authError(String message) throws AuthException
    {
        throw new AuthException(message);
    }

    @LauncherAPI
    public static AuthHandler newHandler(String name, BlockConfigEntry block)
    {
        Adapter<AuthHandler> authHandlerAdapter = VerifyHelper.getMapValue(AUTH_HANDLERS, name,
                String.format("Unknown auth handler: '%s'", name));
        return authHandlerAdapter.convert(block);
    }

    @LauncherAPI
    public static void registerHandler(String name, Adapter<AuthHandler> adapter)
    {
        VerifyHelper.verifyIDName(name);
        VerifyHelper.putIfAbsent(AUTH_HANDLERS, name, Objects.requireNonNull(adapter, "adapter"),
                String.format("Auth handler has been already registered: '%s'", name));
    }

    @Override
    public abstract void close() throws IOException;

    @LauncherAPI
    public abstract UUID auth(AuthProviderResult authResult) throws IOException;

    @LauncherAPI
    public abstract UUID checkServer(String username, String serverID) throws IOException;

    @LauncherAPI
    public abstract boolean joinServer(String username, String accessToken, String serverID) throws IOException;

    @LauncherAPI
    public abstract UUID usernameToUUID(String username) throws IOException;

    @LauncherAPI
    public abstract String uuidToUsername(UUID uuid) throws IOException;
}
