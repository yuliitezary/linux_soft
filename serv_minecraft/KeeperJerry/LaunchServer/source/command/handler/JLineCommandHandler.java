package launchserver.command.handler;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import launcher.helper.LogHelper;
import launcher.helper.LogHelper.Output;
import launchserver.LaunchServer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public final class JLineCommandHandler extends CommandHandler
{
    private final ConsoleReader reader;

    public JLineCommandHandler(LaunchServer server) throws IOException
    {
        super(server);

        // Set reader
        reader = new ConsoleReader();
        reader.setExpandEvents(false);
        reader.addCompleter(new JLineConsoleCompleter());

        // Replace writer
        LogHelper.removeStdOutput();
        LogHelper.addOutput(new JLineOutput());
    }

    @Override
    public void bell() throws IOException
    {
        reader.beep();
    }

    @Override
    public void clear() throws IOException
    {
        reader.clearScreen();
    }

    @Override
    public String readLine() throws IOException
    {
        return reader.readLine();
    }

    private final class JLineOutput implements Output
    {
        @Override
        public void println(String message)
        {
            try
            {
                reader.println(ConsoleReader.RESET_LINE + message);
                reader.drawLine();
                reader.flush();
            }
            catch (IOException ignored)
            {
                // Ignored
            }
        }
    }

    public class JLineConsoleCompleter implements Completer
    {
        @Override
        public int complete(String line, int pos, List<CharSequence> list)
        {
            if (pos == 0) {
                list.addAll(commandsMap().keySet());
            } else {
                commandsMap().keySet().stream().filter(c -> c.startsWith(line)).findFirst().ifPresent(list::add);
            }
            return 0;
        }
    }
}
