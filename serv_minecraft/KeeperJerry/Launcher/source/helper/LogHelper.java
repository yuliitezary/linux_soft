package launcher.helper;

import launcher.Launcher;
import launcher.LauncherAPI;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiOutputStream;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LogHelper
{
    @LauncherAPI
    public static final String DEBUG_PROPERTY = "launcher.debug";
    @LauncherAPI
    public static final String NO_JANSI_PROPERTY = "launcher.noJAnsi";
    @LauncherAPI
    public static final boolean JANSI;

    // Output settings
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss", Locale.US);
    private static final AtomicBoolean DEBUG_ENABLED = new AtomicBoolean(Boolean.getBoolean(DEBUG_PROPERTY));
    private static final Set<Output> OUTPUTS = Collections.newSetFromMap(new ConcurrentHashMap<>(2));
    private static final Output STD_OUTPUT;

    static
    {
        // Use JAnsi if available
        boolean jansi;
        try
        {
            if (Boolean.getBoolean(NO_JANSI_PROPERTY))
            {
                jansi = false;
            }
            else
            {
                Class.forName("org.fusesource.jansi.Ansi");
                AnsiConsole.systemInstall();
                jansi = true;
            }
        }
        catch (ClassNotFoundException ignored)
        {
            jansi = false;
        }
        JANSI = jansi;

        // Add std writer
        STD_OUTPUT = System.out::println;
        addOutput(STD_OUTPUT);

        // Add file log writer
        String logFile = System.getProperty("launcher.logFile");
        if (logFile != null)
        {
            try
            {
                addOutput(IOHelper.toPath(logFile));
            }
            catch (IOException e)
            {
                error(e);
            }
        }
    }

    private LogHelper()
    {
    }

    @LauncherAPI
    public static void addOutput(Output output)
    {
        OUTPUTS.add(Objects.requireNonNull(output, "output"));
    }

    @LauncherAPI
    public static void addOutput(Path file) throws IOException
    {
        if (JANSI)
        {
            addOutput(new JAnsiOutput(IOHelper.newOutput(file, true)));
        }
        else
        {
            addOutput(IOHelper.newWriter(file, true));
        }
    }

    @LauncherAPI
    public static void addOutput(Writer writer) throws IOException
    {
        addOutput(new WriterOutput(writer));
    }

    @LauncherAPI
    public static void debug(String message)
    {
        if (isDebugEnabled())
        {
            log(Level.DEBUG, message, false);
        }
    }

    @LauncherAPI
    public static void debug(String format, Object... args)
    {
        debug(String.format(format, args));
    }

    @LauncherAPI
    public static void error(Throwable exc)
    {
        error(isDebugEnabled() ? toString(exc) : exc.toString());
    }

    @LauncherAPI
    public static void error(String message)
    {
        log(Level.ERROR, message, false);
    }

    @LauncherAPI
    public static void error(String format, Object... args)
    {
        error(String.format(format, args));
    }

    @LauncherAPI
    public static void info(String message)
    {
        log(Level.INFO, message, false);
    }

    @LauncherAPI
    public static void info(String format, Object... args)
    {
        info(String.format(format, args));
    }

    @LauncherAPI
    public static boolean isDebugEnabled()
    {
        return DEBUG_ENABLED.get();
    }

    @LauncherAPI
    public static void setDebugEnabled(boolean debugEnabled)
    {
        DEBUG_ENABLED.set(debugEnabled);
    }

    @LauncherAPI
    public static void log(Level level, String message, boolean sub)
    {
        String dateTime = DATE_TIME_FORMATTER.format(LocalDateTime.now());
        println(JANSI ? ansiFormatLog(level, dateTime, message, sub) :
                formatLog(level, message, dateTime, sub));
    }

    @LauncherAPI
    public static void printVersion(String product)
    {
        println(JANSI ? ansiFormatVersion(product) : formatVersion(product));
    }

    @LauncherAPI
    public static synchronized void println(String message)
    {
        for (Output output : OUTPUTS)
        {
            output.println(message);
        }
    }

    @LauncherAPI
    public static boolean removeOutput(Output output)
    {
        return OUTPUTS.remove(output);
    }

    @LauncherAPI
    public static boolean removeStdOutput()
    {
        return removeOutput(STD_OUTPUT);
    }

    @LauncherAPI
    public static void subDebug(String message)
    {
        if (isDebugEnabled())
        {
            log(Level.DEBUG, message, true);
        }
    }

    @LauncherAPI
    public static void subDebug(String format, Object... args)
    {
        subDebug(String.format(format, args));
    }

    @LauncherAPI
    public static void subInfo(String message)
    {
        log(Level.INFO, message, true);
    }

    @LauncherAPI
    public static void subInfo(String format, Object... args)
    {
        subInfo(String.format(format, args));
    }

    @LauncherAPI
    public static void subWarning(String message)
    {
        log(Level.WARNING, message, true);
    }

    @LauncherAPI
    public static void subWarning(String format, Object... args)
    {
        subWarning(String.format(format, args));
    }

    @LauncherAPI
    public static String toString(Throwable exc)
    {
        try (StringWriter sw = new StringWriter())
        {
            try (PrintWriter pw = new PrintWriter(sw))
            {
                exc.printStackTrace(pw);
            }
            return sw.toString();
        }
        catch (IOException e)
        {
            throw new AssertionError(e);
        }
    }

    @LauncherAPI
    public static void warning(String message)
    {
        log(Level.WARNING, message, false);
    }

    @LauncherAPI
    public static void warning(String format, Object... args)
    {
        warning(String.format(format, args));
    }

    private static String ansiFormatLog(Level level, String dateTime, String message, boolean sub)
    {
        Color levelColor;
        boolean bright = level != Level.DEBUG;
        switch (level)
        {
            case WARNING:
                levelColor = Color.YELLOW;
                break;
            case ERROR:
                levelColor = Color.RED;
                break;
            default: // INFO, DEBUG, Unknown
                levelColor = Color.WHITE;
                break;
        }

        // Date-time
        Ansi ansi = new Ansi();
        ansi.fg(Color.WHITE).a(dateTime);

        // Level
        ansi.fgBright(Color.WHITE).a(" [").bold();
        if (bright)
        {
            ansi.fgBright(levelColor);
        }
        else
        {
            ansi.fg(levelColor);
        }
        ansi.a(level).boldOff().fgBright(Color.WHITE).a("] ");

        // Message
        if (bright)
        {
            ansi.fgBright(levelColor);
        }
        else
        {
            ansi.fg(levelColor);
        }
        if (sub)
        {
            ansi.a(' ').a(Attribute.ITALIC);
        }
        ansi.a(message);

        // Finish with reset code
        return ansi.reset().toString();
    }

    private static String ansiFormatVersion(String product)
    {
        return new Ansi().bold(). // Setup
                fgBright(Color.MAGENTA).a("KeeperJerry's "). // Autor mirror
                fgBright(Color.CYAN).a(product). // Product
                fgBright(Color.WHITE).a(" v").fgBright(Color.BLUE).a(Launcher.VERSION). // Version
                fgBright(Color.WHITE).a(" (build #").fgBright(Color.RED).a(Launcher.BUILD).fgBright(Color.WHITE).a(')'). // Build#
                reset().toString(); // To string
    }

    private static String formatLog(Level level, String message, String dateTime, boolean sub)
    {
        if (sub)
        {
            message = ' ' + message;
        }
        return dateTime + " [" + level.name + "] " + message;
    }

    private static String formatVersion(String product)
    {
        return String.format("KeeperJerry's %s v%s (build #%s)", product, Launcher.VERSION, Launcher.BUILD);
    }

    @LauncherAPI
    public enum Level
    {
        DEBUG("DEBUG"), INFO("INFO"), WARNING("WARN"), ERROR("ERROR");
        public final String name;

        Level(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    @LauncherAPI
    @FunctionalInterface
    public interface Output
    {
        void println(String message);
    }

    private static final class JAnsiOutput extends WriterOutput
    {
        private JAnsiOutput(OutputStream output) throws IOException
        {
            super(IOHelper.newWriter(new AnsiOutputStream(output)));
        }
    }

    private static class WriterOutput implements Output, AutoCloseable
    {
        private final Writer writer;

        private WriterOutput(Writer writer)
        {
            this.writer = writer;
        }

        @Override
        public void close() throws IOException
        {
            writer.close();
        }

        @Override
        public void println(String message)
        {
            try
            {
                writer.write(message + System.lineSeparator());
                writer.flush();
            }
            catch (IOException ignored)
            {
                // Do nothing?
            }
        }
    }
}
