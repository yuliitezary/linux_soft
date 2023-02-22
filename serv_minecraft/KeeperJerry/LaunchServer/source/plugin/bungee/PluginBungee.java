package launchserver.plugin.bungee;

import launcher.helper.CommonHelper;
import launchserver.plugin.PluginBridge;
import net.md_5.bungee.api.plugin.Plugin;

public final class PluginBungee extends Plugin
{
    public volatile PluginBridge bridge = null;

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (bridge != null)
        {
            bridge.close();
            bridge = null;
        }
    }

    @Override
    public void onEnable()
    {
        super.onEnable();

        // Initialize LaunchServer
        try {
            bridge = new PluginBridge(getDataFolder().toPath());
        } catch (Throwable exc) {
            exc.printStackTrace();
        }

        // Register command
        CommonHelper.newThread("LaunchServer Thread", true, bridge).start();
        getProxy().getPluginManager().registerCommand(this, new CommandBungee(this));
    }
}