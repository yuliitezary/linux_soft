package launchserver.auth.limiter;

import launcher.LauncherAPI;
import launchserver.LaunchServer;

import java.util.HashMap;

public class AuthLimiter
{
    @LauncherAPI
    public static final long TIMEOUT = 10 * 60 * 1000; //10 минут
    public final int rateLimit;
    public final int rateLimitMilis;
    private final HashMap<String, AuthEntry> map;

    public AuthLimiter(LaunchServer srv)
    {
        map = new HashMap<>();
        rateLimit = srv.config.authLimitConfig.authRateLimit;
        rateLimitMilis = srv.config.authLimitConfig.authRateLimitMilis;
    }

    public boolean isLimit(String ip)
    {
        if (map.containsKey(ip))
        {
            AuthEntry rate = map.get(ip);
            long currenttime = System.currentTimeMillis();
            if (rate.ts + rateLimitMilis < currenttime)
            {
                rate.value = 0;
            }
            if (rate.value >= rateLimit && rateLimit > 0)
            {
                rate.value++;
                rate.ts = currenttime;
                return true;
            }
            rate.value++;
            rate.ts = currenttime;
            return false;
        }
        map.put(ip, new AuthEntry(1, System.currentTimeMillis()));
        return false;
    }
}
