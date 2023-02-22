package launchserver.binary;

import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launchserver.LaunchServer;

import java.io.IOException;
import java.nio.file.Files;

public final class EXELauncherBinary extends LauncherBinary
{
    public EXELauncherBinary(LaunchServer server)
    {
        super(server, server.dir.resolve(server.config.binaryName + ".exe"));
    }

    @Override
    public void build() throws IOException
    {
        if (IOHelper.isFile(binaryFile))
        {
            LogHelper.subWarning("Deleting obsolete launcher EXE binary file");
            Files.delete(binaryFile);
        }
    }
}
