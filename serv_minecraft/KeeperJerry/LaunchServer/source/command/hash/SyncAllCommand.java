package launchserver.command.hash;

import launcher.helper.LogHelper;
import launchserver.LaunchServer;
import launchserver.command.Command;

import java.io.IOException;

// Source: https://github.com/GravitLauncher/Launcher/commit/3e6384cad9c4bdc2fdc1a614bdcafe9cbc1df4bb
// By Will0376
public class SyncAllCommand extends Command
{
    public SyncAllCommand(LaunchServer server)
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
        return "Resync profiles & updates dirs";
    }

    @Override
    public void invoke(String... args) throws IOException
    {
        server.syncProfilesDir();
        LogHelper.subInfo("Profiles successfully resynced");

        server.syncUpdatesDir(null);
        LogHelper.subInfo("Updates dir successfully resynced");

        server.syncLauncherBinaries();
        LogHelper.subInfo("Binaries successfully resynced");
        LogHelper.subInfo("All services resynced!");
    }
}