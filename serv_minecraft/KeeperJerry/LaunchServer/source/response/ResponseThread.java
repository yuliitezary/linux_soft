package launchserver.response;

import launcher.Launcher;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.helper.SecurityHelper;
import launcher.helper.VerifyHelper;
import launcher.request.Request.Type;
import launcher.request.RequestException;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.LaunchServer;
import launchserver.auth.limiter.AuthLimiterIPConfig;
import launchserver.response.auth.AuthResponse;
import launchserver.response.auth.CheckServerResponse;
import launchserver.response.auth.JoinServerResponse;
import launchserver.response.profile.BatchProfileByUsernameResponse;
import launchserver.response.profile.ProfileByUUIDResponse;
import launchserver.response.profile.ProfileByUsernameResponse;
import launchserver.response.update.LauncherResponse;
import launchserver.response.update.UpdateListResponse;
import launchserver.response.update.UpdateResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;

public final class ResponseThread implements Runnable
{
    private final LaunchServer server;
    private final String ip;
    private final Socket socket;

    public ResponseThread(LaunchServer server, String ip, Socket socket) throws SocketException
    {
        this.server = server;
        this.ip = ip;
        this.socket = socket;

        // Fix socket flags
        IOHelper.setSocketFlags(socket);
    }

    @Override
    public void run()
    {
        if (server.config.authLimit && !server.config.authLimitConfig.blockOnConnect)
        {
            if (AuthLimiterIPConfig.Instance.getAllowIp().stream().noneMatch(s -> s.equals(ip)) && server.config.authLimitConfig.onlyAllowIp)
            {
                if (!server.serverSocketHandler.logConnections) LogHelper.debug("Blocked connection from %s [Not found in Allow List]", ip);
                return;
            }

            if (AuthLimiterIPConfig.Instance.getBlockIp().stream().anyMatch(s -> s.equals(ip)) && server.config.authLimitConfig.useBlockIp)
            {
                if (!server.serverSocketHandler.logConnections) LogHelper.debug("Blocked connection from %s [Found in Block List]", ip);
                return;
            }
        }

        if (!server.serverSocketHandler.logConnections) LogHelper.debug("Connection from %s", ip);

        // Process connection
        boolean cancelled = false;
        Throwable savedError = null;
        try (HInput input = new HInput(IOHelper.newBufferedInputStream(socket.getInputStream()));
             HOutput output = new HOutput(IOHelper.newBufferedOutStream(socket.getOutputStream())))
        {
            Type type = readHandshake(ip, input, output);
            if (type == null)
            { // Not accepted
                cancelled = true;
                return;
            }

            // Start response
            try
            {
                respond(type, input, output);
            }
            catch (RequestException e)
            {
                LogHelper.subDebug(String.format("#%s Request error: %s", IOHelper.getIP(socket.getRemoteSocketAddress()), e.getMessage()));
                output.writeString(e.getMessage(), 0);
            }
        }
        catch (Throwable exc)
        {
            savedError = exc;
            if (LogHelper.isDebugEnabled()) LogHelper.error(exc);
        }
        finally
        {
            IOHelper.close(socket);
            if (!cancelled)
            {
                server.serverSocketHandler.onDisconnect(IOHelper.getIP(socket.getRemoteSocketAddress()), savedError);
            }
        }
    }

    private Type readHandshake(String ip, HInput input, HOutput output) throws IOException
    {
        boolean legacy = false;

        // Verify magic number
        int magicNumber = input.readInt();
        if (magicNumber != Launcher.PROTOCOL_MAGIC)
        {
            if (magicNumber != Launcher.PROTOCOL_MAGIC - 1)
            { // Previous launcher protocol
                output.writeBoolean(false);
                if (LogHelper.isDebugEnabled()) throw new IOException(String.format("[%s] Protocol magic mismatch", ip));
                return null;
            }
            legacy = true;
        }

        // Verify key modulus
        BigInteger keyModulus = input.readBigInteger(SecurityHelper.RSA_KEY_LENGTH + 1);
        if (!keyModulus.equals(server.privateKey.getModulus()))
        {
            output.writeBoolean(false);
            if (LogHelper.isDebugEnabled()) throw new IOException(String.format("[%s] Key modulus mismatch", ip));
            return null;
        }

        // Read request type
        Type type = Type.read(input);
        if (legacy && type != Type.LAUNCHER)
        {
            output.writeBoolean(false);
            if (LogHelper.isDebugEnabled()) throw new IOException(String.format("[%s] Not LAUNCHER request on legacy protocol", ip));
            return null;
        }
        if (!server.serverSocketHandler.onHandshake(ip, type))
        {
            output.writeBoolean(false);
            return null;
        }

        // Protocol successfully verified
        output.writeBoolean(true);
        output.flush();
        return type;
    }

    private void respond(Type type, HInput input, HOutput output) throws Throwable
    {
        if (server.serverSocketHandler.logConnections)
        {
            LogHelper.info("Connection from %s: %s", ip, type.name());
        }
        else
        {
            LogHelper.subDebug("[%s] Type: %s", ip, type.name());
        }

        // Choose response based on type
        Response response;
        switch (type)
        {
            case PING:
                response = new PingResponse(server, ip, input, output);
                break;
            case AUTH:
                response = new AuthResponse(server, input, output, IOHelper.getIP(socket.getRemoteSocketAddress()));
                break;
            case JOIN_SERVER:
                response = new JoinServerResponse(server, ip, input, output);
                break;
            case CHECK_SERVER:
                response = new CheckServerResponse(server, ip, input, output);
                break;
            case LAUNCHER:
                response = new LauncherResponse(server, ip, input, output);
                break;
            case UPDATE:
                response = new UpdateResponse(server, ip, input, output);
                break;
            case UPDATE_LIST:
                response = new UpdateListResponse(server, ip, input, output);
                break;
            case PROFILE_BY_USERNAME:
                response = new ProfileByUsernameResponse(server, ip, input, output);
                break;
            case PROFILE_BY_UUID:
                response = new ProfileByUUIDResponse(server, ip, input, output);
                break;
            case BATCH_PROFILE_BY_USERNAME:
                response = new BatchProfileByUsernameResponse(server, ip, input, output);
                break;
            case CUSTOM:
                String name = VerifyHelper.verifyIDName(input.readASCII(255));
                response = server.serverSocketHandler.newCustomResponse(name, ip, input, output);
                break;
            default:
                throw new AssertionError("Unsupported request type: " + type.name());
        }

        // Reply
        response.reply();
        LogHelper.subDebug("[%s] Replied", ip);
    }
}
