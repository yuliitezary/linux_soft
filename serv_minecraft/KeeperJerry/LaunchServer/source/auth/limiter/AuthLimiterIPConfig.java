package launchserver.auth.limiter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AuthLimiterIPConfig
{
    public static Path ipConfigPath;
    public static Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    public static AuthLimiterIPConfig Instance;

    @Expose
    List<String> allowIp = new ArrayList<>();
    @Expose
    List<String> blockIp = new ArrayList<>();

    public static void load(Path file) throws Exception {
        ipConfigPath = file;
        if (IOHelper.exists(ipConfigPath)) {
            LogHelper.subDebug("IP List file found! Loading...");
            try
            {
                Instance = gson.fromJson(IOHelper.newReader(ipConfigPath), AuthLimiterIPConfig.class);
                return;
            }
            catch (JsonIOException | IOException error) {
                LogHelper.subWarning("Ip List not reading!");
                if (LogHelper.isDebugEnabled()) LogHelper.error(error);
            }
            catch (JsonSyntaxException error) {
                LogHelper.subWarning("Invalid file syntax!");
                if (LogHelper.isDebugEnabled()) LogHelper.error(error);
            }
        }

        LogHelper.subWarning("IP List file not found! Creating file...");
        Instance = new AuthLimiterIPConfig();
        Instance.saveIPConfig();
    }

    public void saveIPConfig() throws Exception
    {
        File ipConfigFile = ipConfigPath.toFile();
        if (!ipConfigFile.exists()) ipConfigFile.createNewFile();

        FileWriter fw = new FileWriter(ipConfigFile, false);
        fw.write(gson.toJson(this));
        fw.close();
        //gson.toJson(this, IOHelper.newWriter(ipConfigPath));
    }

    public List<String> getAllowIp() {
        return allowIp;
    }

    public AuthLimiterIPConfig addAllowIp(String allowIp) {
        this.allowIp.add(allowIp);
        return this;
    }

    public AuthLimiterIPConfig delAllowIp(String allowIp) {
        this.allowIp.removeIf(e -> e.equals(allowIp));
        return this;
    }

    public List<String> getBlockIp() {
        return blockIp;
    }

    public AuthLimiterIPConfig addBlockIp(String blockIp) {
        this.blockIp.add(blockIp);
        return this;
    }

    public AuthLimiterIPConfig delBlockIp(String blockIp) {
        this.blockIp.removeIf(e -> e.equals(blockIp));
        return this;
    }
}