package launchserver.command.hash;

import launcher.helper.LogHelper;
import launchserver.LaunchServer;
import launchserver.command.Command;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class SyncUpdatesCommand extends Command
{
    public SyncUpdatesCommand(LaunchServer server)
    {
        super(server);
    }

    @Override
    public String getArgsDescription()
    {
        return "[subdirs...]";
    }

    @Override
    public String getUsageDescription()
    {
        return "Resync updates dir";
    }

    @Override
    public void invoke(String... args) throws IOException
    {
        Set<String> dirs = null;
        if (args.length > 0)
        { // Hash all updates dirs
            dirs = new HashSet<>(args.length);
            Collections.addAll(dirs, args);
        }

        // Hash updates dir
        server.syncUpdatesDir(dirs);
        LogHelper.subInfo("Updates dir successfully resynced");
    }
}
