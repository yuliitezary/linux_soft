package launcher.hasher;

import com.sun.nio.file.ExtendedWatchEventModifier;
import com.sun.nio.file.SensitivityWatchEventModifier;
import launcher.LauncherAPI;
import launcher.hasher.HashedEntry.Type;
import launcher.helper.IOHelper;
import launcher.helper.JVMHelper;
import launcher.helper.JVMHelper.OS;
import launcher.helper.LogHelper;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

public final class DirWatcher implements Runnable, AutoCloseable
{
    private static final boolean FILE_TREE_SUPPORTED = JVMHelper.OS_TYPE == OS.MUSTDIE;

    // Constants
    private static final Modifier[] MODIFIERS = {
            SensitivityWatchEventModifier.HIGH
    };
    private static final Modifier[] FILE_TREE_MODIFIERS = {
            ExtendedWatchEventModifier.FILE_TREE, SensitivityWatchEventModifier.HIGH
    };
    private static final Kind<?>[] KINDS = {
            StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE
    };

    // Instance
    private final Path dir;
    private final HashedDir hdir;
    private final FileNameMatcher matcher;
    private final WatchService service;
    private final boolean digest;

    @LauncherAPI
    public DirWatcher(Path dir, HashedDir hdir, FileNameMatcher matcher, boolean digest) throws IOException
    {
        this.dir = Objects.requireNonNull(dir, "dir");
        this.hdir = Objects.requireNonNull(hdir, "hdir");
        this.matcher = matcher;
        this.digest = digest;
        service = dir.getFileSystem().newWatchService();

        // Use FILE_TREE if supported
        if (FILE_TREE_SUPPORTED)
        {
            dir.register(service, KINDS, FILE_TREE_MODIFIERS);
            return;
        }

        // Register dirs recursively
        IOHelper.walk(dir, new RegisterFileVisitor(), true);
    }

    private static void handleError(Throwable e)
    {
        LogHelper.error(e);
        JVMHelper.halt0(0x0BADFEE1);
    }

    private static Deque<String> toPath(Iterable<Path> path)
    {
        Deque<String> result = new LinkedList<>();
        for (Path pe : path)
        {
            result.add(pe.toString());
        }
        return result;
    }

    @Override
    @LauncherAPI
    public void close() throws IOException
    {
        service.close();
    }

    @Override
    @LauncherAPI
    public void run()
    {
        try
        {
            processLoop();
        }
        catch (InterruptedException | ClosedWatchServiceException ignored)
        {
            // Do nothing (closed etc)
        }
        catch (Throwable exc)
        {
            handleError(exc);
        }
    }

    private void processKey(WatchKey key) throws IOException
    {
        Path watchDir = (Path) key.watchable();
        for (WatchEvent<?> event : key.pollEvents())
        {
            Kind<?> kind = event.kind();
            if (kind.equals(StandardWatchEventKinds.OVERFLOW))
            {
                if (Boolean.getBoolean("launcher.dirwatcher.ignoreOverflows"))
                {
                    continue; // Sometimes it's better to ignore than interrupt fair playing
                }
                throw new IOException("Overflow");
            }

            // Resolve paths and verify is not exclusion
            Path path = watchDir.resolve((Path) event.context());
            Deque<String> stringPath = toPath(dir.relativize(path));
            if (matcher != null && !matcher.shouldVerify(stringPath))
            {
                continue; // Exclusion; should not be verified
            }

            // Verify is REALLY modified (not just attributes)
            if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY))
            {
                HashedEntry entry = hdir.resolve(stringPath);
                if (entry != null && (entry.getType() != Type.FILE || ((HashedFile) entry).isSame(path, digest)))
                {
                    continue; // Modified attributes, not need to worry :D
                }
            }

            // Forbidden modification!
            throw new SecurityException(String.format("Forbidden modification (%s, %d times): '%s'", kind, event.count(), path));
        }
        key.reset();
    }

    private void processLoop() throws IOException, InterruptedException
    {
        while (!Thread.interrupted())
        {
            processKey(service.take());
        }
    }

    private final class RegisterFileVisitor extends SimpleFileVisitor<Path>
    {
        private final Deque<String> path = new LinkedList<>();

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
        {
            FileVisitResult result = super.postVisitDirectory(dir, exc);
            if (!DirWatcher.this.dir.equals(dir))
            {
                path.removeLast();
            }
            return result;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
        {
            FileVisitResult result = super.preVisitDirectory(dir, attrs);
            if (DirWatcher.this.dir.equals(dir))
            {
                dir.register(service, KINDS, MODIFIERS);
                return result;
            }

            // Maybe it's unnecessary to go deeper
            path.add(IOHelper.getFileName(dir));
            if (matcher != null && !matcher.shouldVerify(path))
            {
                return FileVisitResult.SKIP_SUBTREE;
            }

            // Register
            dir.register(service, KINDS, MODIFIERS);
            return result;
        }
    }
}
