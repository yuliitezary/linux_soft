package launchserver.response;

import launcher.LauncherAPI;
import launcher.helper.CommonHelper;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.helper.VerifyHelper;
import launcher.request.Request.Type;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;
import launchserver.response.Response.Factory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class ServerSocketHandler implements Runnable, AutoCloseable
{
    private static final ThreadFactory THREAD_FACTORY = r -> CommonHelper.newThread("Network Thread", true, r);
    // Instance
    private final LaunchServer server;
    private final AtomicReference<ServerSocket> serverSocket = new AtomicReference<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool(THREAD_FACTORY);
    // API
    private final Map<String, Factory> customResponses = new ConcurrentHashMap<>(2);
    private final AtomicLong idCounter = new AtomicLong(0L);
    @LauncherAPI
    public volatile boolean logConnections = Boolean.getBoolean("launcher.logConnections");
    private volatile Listener listener;

    public ServerSocketHandler(LaunchServer server)
    {
        this.server = server;
    }

    @Override
    public void close()
    {
        ServerSocket socket = serverSocket.getAndSet(null);
        if (socket != null)
        {
            LogHelper.info("Closing server socket listener");
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                LogHelper.error(e);
            }
        }
    }

    @Override
    public void run()
    {
        LogHelper.info("Starting server socket thread");
        try (ServerSocket serverSocket = new ServerSocket())
        {
            if (!this.serverSocket.compareAndSet(null, serverSocket))
            {
                throw new IllegalStateException("Previous socket wasn't closed");
            }

            // Set socket params
            serverSocket.setReuseAddress(true);
            serverSocket.setPerformancePreferences(1, 0, 2);
            //serverSocket.setReceiveBufferSize(0x10000);
            serverSocket.bind(server.config.getSocketAddress());
            LogHelper.info("Server socket thread successfully started");

            // Listen for incoming connections
            while (serverSocket.isBound())
            {
                Socket socket = serverSocket.accept();

                String ip = IOHelper.getIP(socket.getRemoteSocketAddress());

                // Invoke pre-connect listener
                if (listener != null && !listener.onConnect(ip, socket.getInetAddress()))
                {
                    continue; // Listener didn't accepted this connection
                }

                // Reply in separate thread
                threadPool.execute(new ResponseThread(server, ip, socket));
            }
        }
        catch (IOException e)
        {
            // Ignore error after close/rebind
            if (serverSocket.get() != null)
            {
                LogHelper.error(e);
            }
        }
    }

    @LauncherAPI
    public Response newCustomResponse(String name, String ip, HInput input, HOutput output)
    {
        Factory factory = VerifyHelper.getMapValue(customResponses, name,
                String.format("Unknown custom response: '%s'", name));
        return factory.newResponse(server, ip, input, output);
    }

    @LauncherAPI
    public void registerCustomResponse(String name, Factory factory)
    {
        VerifyHelper.verifyIDName(name);
        VerifyHelper.putIfAbsent(customResponses, name, Objects.requireNonNull(factory, "factory"),
                String.format("Custom response has been already registered: '%s'", name));
    }

    @LauncherAPI
    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    /*package*/ void onDisconnect(String ip, Throwable exc)
    {
        if (listener != null)
        {
            listener.onDisconnect(ip, exc);
        }
    }

    /*package*/ boolean onHandshake(String ip, Type type)
    {
        return listener == null || listener.onHandshake(ip, type);
    }

    public interface Listener
    {
        @LauncherAPI
        boolean onConnect(String ip, InetAddress address);

        @LauncherAPI
        void onDisconnect(String ip, Throwable exc);

        @LauncherAPI
        boolean onHandshake(String ip, Type type);
    }
}
