package launcher.client;

import launcher.LauncherAPI;
import launcher.hasher.FileNameMatcher;
import launcher.helper.IOHelper;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.config.ConfigObject;
import launcher.serialize.config.entry.*;
import launcher.serialize.config.entry.ConfigEntry.Type;
import launcher.serialize.stream.StreamObject;

import java.io.IOException;
import java.net.InetSocketAddress;

@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
public final class ClientProfile extends ConfigObject implements Comparable<ClientProfile>
{
    @LauncherAPI
    public static final StreamObject.Adapter<ClientProfile> RO_ADAPTER = input -> new ClientProfile(input, true);
    private static final FileNameMatcher ASSET_MATCHER = new FileNameMatcher(
            new String[0], new String[]{"indexes", "objects"}, new String[0]);

    // Version
    private final StringConfigEntry version;
    private final StringConfigEntry assetIndex;

    // Client
    private final IntegerConfigEntry sortIndex;
    private final StringConfigEntry title;
    private final StringConfigEntry serverAddress;
    private final IntegerConfigEntry serverPort;
    private final StringConfigEntry jvmVersion;

    //  Updater and client watch service
    private final ListConfigEntry update;
    private final ListConfigEntry updateExclusions;
    private final ListConfigEntry updateVerify;
    private final BooleanConfigEntry updateFastCheck;

    // Client launcher
    private final StringConfigEntry mainClass;
    private final ListConfigEntry jvmArgs;
    private final ListConfigEntry classPath;
    private final ListConfigEntry clientArgs;

    @LauncherAPI
    public ClientProfile(BlockConfigEntry block)
    {
        super(block);

        // Version
        version = block.getEntry("version", StringConfigEntry.class);
        assetIndex = block.getEntry("assetIndex", StringConfigEntry.class);

        // Client
        sortIndex = block.getEntry("sortIndex", IntegerConfigEntry.class);
        title = block.getEntry("title", StringConfigEntry.class);
        serverAddress = block.getEntry("serverAddress", StringConfigEntry.class);
        serverPort = block.getEntry("serverPort", IntegerConfigEntry.class);
        jvmVersion = block.getEntry("jvmVersion", StringConfigEntry.class);

        //  Updater and client watch service
        update = block.getEntry("update", ListConfigEntry.class);
        updateVerify = block.getEntry("updateVerify", ListConfigEntry.class);
        updateExclusions = block.getEntry("updateExclusions", ListConfigEntry.class);
        updateFastCheck = block.getEntry("updateFastCheck", BooleanConfigEntry.class);

        // Client launcher
        mainClass = block.getEntry("mainClass", StringConfigEntry.class);
        classPath = block.getEntry("classPath", ListConfigEntry.class);
        jvmArgs = block.getEntry("jvmArgs", ListConfigEntry.class);
        clientArgs = block.getEntry("clientArgs", ListConfigEntry.class);
    }

    @LauncherAPI
    public ClientProfile(HInput input, boolean ro) throws IOException
    {
        this(new BlockConfigEntry(input, ro));
    }

    @Override
    public int compareTo(ClientProfile o)
    {
        return Integer.compare(getSortIndex(), o.getSortIndex());
    }

    @Override
    public String toString()
    {
        return title.getValue();
    }

    @LauncherAPI
    public String getAssetIndex()
    {
        return assetIndex.getValue();
    }

    @LauncherAPI
    public void setAssetIndex(String version)
    {
        this.assetIndex.setValue(version);
    }

    @LauncherAPI
    public FileNameMatcher getAssetUpdateMatcher()
    {
        return Version.compare(getVersion(), "1.7.3") >= 0 ? ASSET_MATCHER : null;
    }

    @LauncherAPI
    public String[] getClassPath()
    {
        return classPath.stream(StringConfigEntry.class).toArray(String[]::new);
    }

    @LauncherAPI
    public String[] getClientArgs()
    {
        return clientArgs.stream(StringConfigEntry.class).toArray(String[]::new);
    }

    @LauncherAPI
    public FileNameMatcher getClientUpdateMatcher()
    {
        String[] updateArray = update.stream(StringConfigEntry.class).toArray(String[]::new);
        String[] verifyArray = updateVerify.stream(StringConfigEntry.class).toArray(String[]::new);
        String[] exclusionsArray = updateExclusions.stream(StringConfigEntry.class).toArray(String[]::new);
        return new FileNameMatcher(updateArray, verifyArray, exclusionsArray);
    }

    @LauncherAPI
    public String getJvmVersion() {
        return jvmVersion.getValue();
    }

    @LauncherAPI
    public String[] getJvmArgs()
    {
        return jvmArgs.stream(StringConfigEntry.class).toArray(String[]::new);
    }

    @LauncherAPI
    public String getMainClass()
    {
        return mainClass.getValue();
    }

    @LauncherAPI
    public String getServerAddress()
    {
        return serverAddress.getValue();
    }

    @LauncherAPI
    public int getServerPort()
    {
        return serverPort.getValue();
    }

    @LauncherAPI
    public InetSocketAddress getServerSocketAddress()
    {
        return InetSocketAddress.createUnresolved(getServerAddress(), getServerPort());
    }

    @LauncherAPI
    public int getSortIndex()
    {
        return sortIndex.getValue();
    }

    @LauncherAPI
    public String getTitle()
    {
        return title.getValue();
    }

    @LauncherAPI
    public void setTitle(String title)
    {
        this.title.setValue(title);
    }

    @LauncherAPI
    public String getVersion()
    {
        return version.getValue();
    }

    @LauncherAPI
    public void setVersion(String version)
    {
        this.version.setValue(version);
    }

    @LauncherAPI
    public boolean isUpdateFastCheck()
    {
        return updateFastCheck.getValue();
    }

    @LauncherAPI
    public void verify()
    {
        // Version
        VerifyHelper.verify(getVersion(), VerifyHelper.NOT_EMPTY, "Game version can't be empty");
        IOHelper.verifyFileName(getAssetIndex()); // А в смысле, там же версия, какой нахуй FileName?

        // Client
        VerifyHelper.verify(getTitle(), VerifyHelper.NOT_EMPTY, "Profile title can't be empty");
        VerifyHelper.verify(getServerAddress(), VerifyHelper.NOT_EMPTY, "Server address can't be empty");
        VerifyHelper.verifyInt(getServerPort(), VerifyHelper.range(0, 65535), "Illegal server port: " + getServerPort());

        //  Updater and client watch service
        update.verifyOfType(Type.STRING);
        updateVerify.verifyOfType(Type.STRING);
        updateExclusions.verifyOfType(Type.STRING);

        // Client launcher
        jvmArgs.verifyOfType(Type.STRING);
        classPath.verifyOfType(Type.STRING);
        clientArgs.verifyOfType(Type.STRING);
        VerifyHelper.verify(getTitle(), VerifyHelper.NOT_EMPTY, "Main class can't be empty");
    }

    // Можно конечно угореть и парсить версии с https://launchermeta.mojang.com/mc/game/version_manifest.json
    // Но имеет ли это смысл? Даже такой простенький обработчик будет спокойно справляться с сравнением релизных версий
    public static class Version {
        public static int compare(String originVersion, String comparedVersion) {
            String[] originVersionParts = originVersion.split("\\.");
            String[] comparedVersionParts = comparedVersion.split("\\.");
            int length = Math.max(originVersionParts.length, comparedVersionParts.length);
            for(int i = 0; i < length; i++) {
                int originVersionPart = i < originVersionParts.length ? Integer.parseInt(originVersionParts[i]) : 0;
                int comparedVersionPart = i < comparedVersionParts.length ? Integer.parseInt(comparedVersionParts[i]) : 0;
                if(originVersionPart < comparedVersionPart) return -1;
                if(originVersionPart > comparedVersionPart) return 1;
            }
            return 0;
        }
    }
}
