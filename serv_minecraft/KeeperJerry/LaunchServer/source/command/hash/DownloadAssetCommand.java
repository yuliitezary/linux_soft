package launchserver.command.hash;

import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.LaunchServer;
import launchserver.command.Command;
import launchserver.helpers.HTTPRequestHelper;
import launchserver.helpers.UnzipHelper;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public final class DownloadAssetCommand extends Command
{
    public DownloadAssetCommand(LaunchServer server)
    {
        super(server);
    }

    @Override
    public String getArgsDescription()
    {
        return "<version> <dir>";
    }

    @Override
    public String getUsageDescription()
    {
        return "Download asset dir";
    }

    @Override
    public void invoke(String... args) throws Throwable
    {
        verifyArgs(args, 2);
        String version = args[0];
        String dirName = IOHelper.verifyFileName(args[1]);
        Path assetDir = server.updatesDir.resolve(dirName);
        String[] mirrors = server.config.mirrors.stream(StringConfigEntry.class).toArray(String[]::new);
        String assetMask = String.format("assets/%s.zip", version);

        for (String mirror : mirrors) {
            URL assetUrl = new URL(mirror + assetMask);

            if (!HTTPRequestHelper.fileExist(assetUrl)) continue;

            // Create asset dir
            LogHelper.subInfo("Asset found. Creating asset dir: '%s'", dirName);
            Files.createDirectory(assetDir);

            // Download required asset
            LogHelper.subInfo("Downloading asset, it may take some time");
            if(!UnzipHelper.downloadZip(assetUrl, assetDir)) return;

            // Finished
            server.syncUpdatesDir(Collections.singleton(dirName));
            LogHelper.subInfo("Asset successfully downloaded: '%s'", dirName);
            return;
        }
        LogHelper.error("Error download %s. All mirrors return error", dirName);
    }
}
