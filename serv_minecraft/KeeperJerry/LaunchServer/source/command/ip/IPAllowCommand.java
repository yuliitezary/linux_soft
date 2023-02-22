package launchserver.command.ip;

import launcher.helper.LogHelper;
import launchserver.LaunchServer;
import launchserver.auth.limiter.AuthLimiterIPConfig;
import launchserver.command.Command;
import sun.net.util.IPAddressUtil;

import java.util.Locale;

public class IPAllowCommand extends Command
{
    public IPAllowCommand(LaunchServer server)
    {
        super(server);
    }

    @Override
    public String getArgsDescription()
    {
        return "<type> <ip>";
    }

    @Override
    public String getUsageDescription()
    {
        return "Add/Remove IP to Allow List";
    }

    @Override
    public void invoke(String... args) throws Throwable
    {
        verifyArgs(args, 2);
        String type = args[0].toLowerCase(Locale.ROOT);
        String getIP = args[1];

        if (type.isEmpty())
        {
            LogHelper.error("Type cannot be empty!");
        }

        if (getIP.isEmpty())
        {
            LogHelper.error("IP address cannot be empty!");
            return;
        }

        if (!IPAddressUtil.isIPv4LiteralAddress(getIP) && !IPAddressUtil.isIPv6LiteralAddress(getIP))
        {
            LogHelper.error("This is not an IP address!");
            return;
        }

        if (type.equals("add"))
        {
            AuthLimiterIPConfig.Instance.addAllowIp(getIP).saveIPConfig();
            LogHelper.info("IP address add to Allow List!");
            return;
        }

        if (type.equals("del"))
        {
            AuthLimiterIPConfig.Instance.delAllowIp(getIP).saveIPConfig();
            LogHelper.info("IP address remove to Allow List!");
            return;
        }

        LogHelper.error("This is type is unknown!");
    }
}
