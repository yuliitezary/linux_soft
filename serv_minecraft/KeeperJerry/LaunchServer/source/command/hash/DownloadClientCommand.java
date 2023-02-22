package launchserver.command.hash;

import launcher.client.ClientProfile;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;
import launcher.serialize.config.TextConfigReader;
import launcher.serialize.config.TextConfigWriter;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.LaunchServer;
import launchserver.command.Command;
import launchserver.helpers.HTTPRequestHelper;
import launchserver.helpers.UnzipHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public final class DownloadClientCommand extends Command
{
    public DownloadClientCommand(LaunchServer server)
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
        return "Download client dir";
    }

    @Override
    public void invoke(String... args) throws Throwable
    {
        verifyArgs(args, 2);
        String version = args[0];
        if (version.contains("-")) version = version.split("-")[0];
        String dirName = IOHelper.verifyFileName(args[1]);
        Path clientDir = server.updatesDir.resolve(args[1]);
        String[] mirrors = server.config.mirrors.stream(StringConfigEntry.class).toArray(String[]::new);
        String clientMask = String.format("clients/%s.zip", args[0]);
        String profileMask = String.format("clients/%s.cfg", args[0]);

        for (String mirror : mirrors)
        {
            URL clientUrl = new URL(mirror + clientMask);
            URL profileUrl = new URL(mirror + profileMask);

            if (!HTTPRequestHelper.fileExist(clientUrl) || !HTTPRequestHelper.fileExist(profileUrl)) continue;

            // Create profile file
            LogHelper.subInfo("Client found. Creating profile file: '%s'", dirName);
            ClientProfile client;

            // Download required client
            LogHelper.subInfo("Downloading client, it may take some time");
            Files.createDirectory(clientDir);
            if(!UnzipHelper.downloadZip(clientUrl, clientDir)) return;

            try (BufferedReader reader = IOHelper.newReader(profileUrl))
            {
                client = new ClientProfile(TextConfigReader.read(reader, false));
            }
            client.setTitle(dirName);
            client.setVersion(version);
            client.setAssetIndex(version);
            client.block.getEntry("dir", StringConfigEntry.class).setValue(dirName);
            try (BufferedWriter writer = IOHelper.newWriter(IOHelper.resolveIncremental(server.profilesDir,
                    dirName, "cfg")))
            {
                TextConfigWriter.write(client.block, writer, true);
            }

            // Finished
            server.syncProfilesDir();
            server.syncUpdatesDir(Collections.singleton(dirName));
            LogHelper.subInfo("Client successfully downloaded: '%s'", dirName);
            LogHelper.subInfo("DON'T FORGET! Set up the assets directory!");
            return;
        }
        LogHelper.error("Error download %s. All mirrors return error", dirName);
    }
}
