package launchserver.command.hash;

import launcher.helper.LogHelper;
import launchserver.LaunchServer;
import launchserver.command.Command;

import java.io.IOException;

public final class SyncProfilesCommand extends Command
{
    public SyncProfilesCommand(LaunchServer server)
    {
        super(server);
    }

    @Override
    public String getArgsDescription()
    {
        return null;
    }

    @Override
    public String getUsageDescription()
    {
        return "Resync profiles dir";
    }

    @Override
    public void invoke(String... args) throws IOException
    {
        server.syncProfilesDir();
        LogHelper.subInfo("Profiles successfully resynced");
    }
}
