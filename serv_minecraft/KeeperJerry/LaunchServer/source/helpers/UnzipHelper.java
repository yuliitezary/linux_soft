package launchserver.helpers;

import launcher.helper.IOHelper;
import launcher.helper.LogHelper;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipHelper {
    private static void unpack(URL url, Path dir) throws IOException
    {
        try (ZipInputStream input = IOHelper.newZipInput(url))
        {
            for (ZipEntry entry = input.getNextEntry(); entry != null; entry = input.getNextEntry())
            {
                if (entry.isDirectory())
                {
                    continue; // Skip directories
                }

                // Unpack entry
                String name = entry.getName();
                LogHelper.subInfo("Downloading file: '%s'", name);
                IOHelper.transfer(input, dir.resolve(IOHelper.toPath(name)));
            }
        }
    }

    public static boolean downloadZip(URL url, Path dir) {
        LogHelper.debug("Try download %s", url.toString());
        try {
            unpack(url, dir);
        } catch (IOException e) {
            LogHelper.error("Download %s failed (%s: %s)", url.toString(), e.getClass().getName(), e.getMessage());
            return false;
        }
        return true;
    }
}
