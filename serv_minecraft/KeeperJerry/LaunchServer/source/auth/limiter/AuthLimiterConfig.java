package launchserver.auth.limiter;

import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launcher.serialize.config.ConfigObject;
import launcher.serialize.config.entry.*;
import launchserver.LaunchServer;

public class AuthLimiterConfig extends ConfigObject
{
    @LauncherAPI
    public LaunchServer server;
    @LauncherAPI
    public int authRateLimit;
    @LauncherAPI
    public int authRateLimitMilis;
    @LauncherAPI
    public String authRejectString;
    @LauncherAPI
    public String authBannedString;
    @LauncherAPI
    public String authNotWhitelistString;
    @LauncherAPI
    public boolean blockOnConnect;
    @LauncherAPI
    public boolean useAllowIp;
    @LauncherAPI
    public boolean useBlockIp;
    @LauncherAPI
    public boolean onlyAllowIp;

    @LauncherAPI
    public AuthLimiterConfig(BlockConfigEntry block)
    {
        super(block);
        authRateLimit = VerifyHelper.verifyInt(block.getEntryValue("authRateLimit", IntegerConfigEntry.class),
                VerifyHelper.range(0, 1000000), "Illegal authRateLimit");
        authRateLimitMilis = VerifyHelper.verifyInt(block.getEntryValue("authRateLimitMilis", IntegerConfigEntry.class),
                VerifyHelper.range(10, 10000000), "Illegal authRateLimitMillis");
        authRejectString = block.hasEntry("authRejectString") ?
                block.getEntryValue("authRejectString", StringConfigEntry.class) : "Превышен лимит авторизаций. Подождите некоторое время перед повторной попыткой";
        authNotWhitelistString = block.hasEntry("authNotWhitelistString") ?
                block.getEntryValue("authNotWhitelistString", StringConfigEntry.class) : "Вашего IP нет в белом списке!";
        authBannedString = block.hasEntry("authBannedString") ?
                block.getEntryValue("authBannedString", StringConfigEntry.class) : "Ваш IP заблокирован!";
        blockOnConnect = block.getEntryValue("blockOnConnect", BooleanConfigEntry.class);
        onlyAllowIp = block.getEntryValue("onlyAllowIp", BooleanConfigEntry.class);
        useAllowIp = block.getEntryValue("useAllowIp", BooleanConfigEntry.class);
        useBlockIp = block.getEntryValue("useBlockIp", BooleanConfigEntry.class);
    }
}
