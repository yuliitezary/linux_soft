package launchserver.response;

import launcher.LauncherAPI;
import launcher.helper.LogHelper;
import launcher.request.RequestException;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;

import java.io.IOException;

public abstract class Response
{
    @LauncherAPI
    protected final LaunchServer server;
    @LauncherAPI
    protected final String ip;
    @LauncherAPI
    protected final HInput input;
    @LauncherAPI
    protected final HOutput output;

    @LauncherAPI
    protected Response(LaunchServer server, String ip, HInput input, HOutput output)
    {
        this.server = server;
        this.ip = ip;
        this.input = input;
        this.output = output;
    }

    @LauncherAPI
    public static void requestError(String message) throws RequestException
    {
        throw new RequestException(message);
    }

    @LauncherAPI
    public abstract void reply() throws Throwable;

    @LauncherAPI
    protected final void debug(String message)
    {
        LogHelper.subDebug("[%s] %s", ip, message);
    }

    @LauncherAPI
    protected final void debug(String message, Object... args)
    {
        debug(String.format(message, args));
    }

    @LauncherAPI
    @SuppressWarnings("MethodMayBeStatic") // Intentionally not static
    protected final void writeNoError(HOutput output) throws IOException
    {
        output.writeString("", 0);
    }

    @FunctionalInterface
    public interface Factory
    {
        @LauncherAPI
        Response newResponse(LaunchServer server, String ip, HInput input, HOutput output);
    }
}
