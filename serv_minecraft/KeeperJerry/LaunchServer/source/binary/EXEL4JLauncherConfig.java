package launchserver.binary;

import launcher.LauncherAPI;
import launcher.serialize.config.ConfigObject;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;

public class EXEL4JLauncherConfig extends ConfigObject
{
    @LauncherAPI
    public String productName;
    @LauncherAPI
    public String fileDesc;
    @LauncherAPI
    public String internalName;
    @LauncherAPI
    public String copyright;
    @LauncherAPI
    public String trademarks;

    @LauncherAPI
    public EXEL4JLauncherConfig(BlockConfigEntry block)
    {
        super(block);
        productName = block.hasEntry("productName") ? block.getEntryValue("productName", StringConfigEntry.class)
                : "LauncherSchool";
        fileDesc = block.hasEntry("fileDesc") ? block.getEntryValue("fileDesc", StringConfigEntry.class)
                : "LauncherSchool by KeeperJerry";
        internalName = block.hasEntry("internalName") ? block.getEntryValue("internalName", StringConfigEntry.class)
                : "Launcher";
        copyright = block.hasEntry("copyright") ? block.getEntryValue("copyright", StringConfigEntry.class)
                : "© KeeperJerry";
        trademarks = block.hasEntry("trademarks") ? block.getEntryValue("trademarks", StringConfigEntry.class)
                : "This product is licensed under GNU v3.0";
    }
}
