package launchserver.command;

import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launchserver.LaunchServer;

import java.util.UUID;

public abstract class Command
{
    @LauncherAPI
    protected final LaunchServer server;

    @LauncherAPI
    protected Command(LaunchServer server)
    {
        this.server = server;
    }

    @LauncherAPI
    protected static UUID parseUUID(String s) throws CommandException
    {
        try
        {
            return UUID.fromString(s);
        }
        catch (IllegalArgumentException ignored)
        {
            throw new CommandException(String.format("Invalid UUID: '%s'", s));
        }
    }

    @LauncherAPI
    protected static String parseUsername(String username) throws CommandException
    {
        try
        {
            return VerifyHelper.verifyUsername(username);
        }
        catch (IllegalArgumentException e)
        {
            throw new CommandException(e.getMessage());
        }
    }

    @LauncherAPI
    public abstract String getArgsDescription(); // "<required> [optional]"

    @LauncherAPI
    public abstract String getUsageDescription();

    @LauncherAPI
    public abstract void invoke(String... args) throws Throwable;

    @LauncherAPI
    protected final void verifyArgs(String[] args, int min) throws CommandException
    {
        if (args.length < min)
        {
            throw new CommandException("Command usage: " + getArgsDescription());
        }
    }
}
