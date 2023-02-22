package launchserver.command.ip;

import launcher.helper.LogHelper;
import launchserver.LaunchServer;
import launchserver.auth.limiter.AuthLimiterIPConfig;
import launchserver.command.Command;
import sun.net.util.IPAddressUtil;

import java.util.Locale;

public class IPBlockCommand extends Command
{
    public IPBlockCommand(LaunchServer server)
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
        return "Add/Remove IP to Block List";
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
            AuthLimiterIPConfig.Instance.addBlockIp(getIP).saveIPConfig();
            LogHelper.info("IP address add to Block List!");
            return;
        }

        if (type.equals("del"))
        {
            AuthLimiterIPConfig.Instance.delBlockIp(getIP).saveIPConfig();
            LogHelper.info("IP address remove to Block List!");
            return;
        }

        LogHelper.error("This is type is unknown!");
    }
}
