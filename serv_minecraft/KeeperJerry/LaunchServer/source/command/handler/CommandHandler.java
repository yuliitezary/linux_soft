package launchserver.command.handler;

import launcher.LauncherAPI;
import launcher.helper.LogHelper;
import launcher.helper.VerifyHelper;
import launchserver.LaunchServer;
import launchserver.command.Command;
import launchserver.command.CommandException;
import launchserver.command.auth.*;
import launchserver.command.basic.*;
import launchserver.command.hash.*;
import launchserver.command.ip.IPAllowCommand;
import launchserver.command.ip.IPBlockCommand;
import launchserver.command.legacy.DumpBinaryAuthHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CommandHandler implements Runnable
{
    private final Map<String, Command> commands = new ConcurrentHashMap<>(32);

    protected CommandHandler(LaunchServer server)
    {
        // Register basic commands
        registerCommand("help", new HelpCommand(server));
        registerCommand("version", new VersionCommand(server));
        registerCommand("build", new BuildCommand(server));
        registerCommand("stop", new StopCommand(server));
        registerCommand("rebind", new RebindCommand(server));
        registerCommand("debug", new DebugCommand(server));
        registerCommand("clear", new ClearCommand(server));
        registerCommand("eval", new EvalCommand(server));
        registerCommand("gc", new GCCommand(server));
        registerCommand("logConnections", new LogConnectionsCommand(server));

        // Register sync commands
        registerCommand("indexAsset", new IndexAssetCommand(server));
        registerCommand("unindexAsset", new UnindexAssetCommand(server));
        registerCommand("downloadAsset", new DownloadAssetCommand(server));
        registerCommand("downloadClient", new DownloadClientCommand(server));
        registerCommand("syncBinaries", new SyncBinariesCommand(server));
        registerCommand("syncUpdates", new SyncUpdatesCommand(server));
        registerCommand("syncProfiles", new SyncProfilesCommand(server));

        // Register IP commands
        registerCommand("allowIp", new IPAllowCommand(server));
        registerCommand("blockIp", new IPBlockCommand(server));

        // Register custom commands
        registerCommand("syncAll", new SyncAllCommand(server));

        // Register auth commands
        registerCommand("auth", new AuthCommand(server));
        registerCommand("joinServer", new JoinServerCommand(server));
        registerCommand("checkServer", new CheckServerCommand(server));
        registerCommand("usernameToUUID", new UsernameToUUIDCommand(server));
        registerCommand("uuidToUsername", new UUIDToUsernameCommand(server));

        // Register legacy commands
        registerCommand("dumpBinaryAuthHandler", new DumpBinaryAuthHandler(server));
    }

    private static String[] parse(CharSequence line) throws CommandException
    {
        boolean quoted = false;
        boolean wasQuoted = false;

        // Read line char by char
        Collection<String> result = new LinkedList<>();
        StringBuilder builder = new StringBuilder(100);
        for (int i = 0; i <= line.length(); i++)
        {
            boolean end = i >= line.length();
            char ch = end ? '\0' : line.charAt(i);

            // Maybe we should read next argument?
            if (end || !quoted && Character.isWhitespace(ch))
            {
                if (end && quoted)
                { // Quotes should be closed
                    throw new CommandException("Quotes wasn't closed");
                }

                // Empty args are ignored (except if was quoted)
                if (wasQuoted || builder.length() > 0)
                {
                    result.add(builder.toString());
                }

                // Reset string builder
                wasQuoted = false;
                builder.setLength(0);
                continue;
            }

            // Append next char
            switch (ch)
            {
                case '"': // "abc"de, "abc""de" also allowed
                    quoted = !quoted;
                    wasQuoted = true;
                    break;
                case '\\': // All escapes, including spaces etc
                    if (i + 1 >= line.length())
                    {
                        throw new CommandException("Escape character is not specified");
                    }
                    char next = line.charAt(i + 1);
                    builder.append(next);
                    i++;
                    break;
                default: // Default char, simply append
                    builder.append(ch);
                    break;
            }
        }

        // Return result as array
        return result.toArray(new String[result.size()]);
    }

    @Override
    public final void run()
    {
        try
        {
            readLoop();
        }
        catch (IOException e)
        {
            LogHelper.error(e);
        }
    }

    @LauncherAPI
    public abstract void bell() throws IOException;

    @LauncherAPI
    public abstract void clear() throws IOException;

    @LauncherAPI
    public abstract String readLine() throws IOException;

    @LauncherAPI
    public final Map<String, Command> commandsMap()
    {
        return Collections.unmodifiableMap(commands);
    }

    @LauncherAPI
    public final void eval(String line, boolean bell)
    {
        LogHelper.info("Command '%s'", line);

        // Parse line to tokens
        String[] args;
        try
        {
            args = parse(line);
            if(args.length > 0) args[0] = args[0].toLowerCase();
        }
        catch (Throwable exc)
        {
            LogHelper.error(exc);
            return;
        }

        // Evaluate command
        eval(args, bell);
    }

    @LauncherAPI
    public final void eval(String[] args, boolean bell)
    {
        if (args.length == 0)
        {
            return;
        }

        // Measure start time and invoke command
        long start = System.currentTimeMillis();
        try
        {
            lookup(args[0]).invoke(Arrays.copyOfRange(args, 1, args.length));
        }
        catch (Throwable exc)
        {
            LogHelper.error(exc);
        }

        // Bell if invocation took > 1s
        long end = System.currentTimeMillis();
        if (bell && end - start >= 5_000L)
        {
            try
            {
                bell();
            }
            catch (IOException e)
            {
                LogHelper.error(e);
            }
        }
    }

    @LauncherAPI
    public final Command lookup(String name) throws CommandException
    {
        Command command = commands.get(name);
        if (command == null)
        {
            throw new CommandException(String.format("Unknown command: '%s'", name));
        }
        return command;
    }

    @LauncherAPI
    public final void registerCommand(String name, Command command)
    {
        VerifyHelper.verifyIDName(name);
        VerifyHelper.putIfAbsent(commands, name.toLowerCase(), Objects.requireNonNull(command, "command"),
                String.format("Command has been already registered: '%s'", name.toLowerCase()));
    }

    private void readLoop() throws IOException
    {
        for (String line = readLine(); line != null; line = readLine())
        {
            eval(line, true);
        }
    }
}
