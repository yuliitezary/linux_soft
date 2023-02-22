package launchserver.plugin.bungee;

import launchserver.plugin.PluginBridge;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.command.ConsoleCommandSender;

public final class CommandBungee extends Command
{
    private static final BaseComponent[] NOT_CONSOLE_MESSAGE = TextComponent.fromLegacyText(ChatColor.RED + "This command can only be used from the console!");
    private static final BaseComponent[] NOT_INITIALIZED_MESSAGE = TextComponent.fromLegacyText(ChatColor.RED + "LaunchServer was not fully loaded!");

    // Instance
    public final PluginBungee plugin;

    public CommandBungee(PluginBungee plugin)
    {
        super("launchserver", "launchserver.admin", "launcher", "ls", "l");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String... args)
    {
        if (!(sender instanceof ConsoleCommandSender))
        {
            sender.sendMessage(NOT_CONSOLE_MESSAGE);
            return;
        }

        // Eval command
        PluginBridge bridge = plugin.bridge;
        if (bridge == null)
        {
            sender.sendMessage(NOT_INITIALIZED_MESSAGE);
        }
        else
        {
            bridge.eval(args);
        }
    }
}