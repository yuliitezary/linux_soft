package launcher.request;

import launcher.LauncherAPI;

import java.io.IOException;

public final class RequestException extends IOException
{
    private static final long serialVersionUID = 7558237657082664821L;

    @LauncherAPI
    public RequestException(String message)
    {
        super(message);
    }

    @LauncherAPI
    public RequestException(Throwable exc)
    {
        super(exc);
    }

    @LauncherAPI
    public RequestException(String message, Throwable exc)
    {
        super(message, exc);
    }

    @Override
    public String toString()
    {
        return getMessage();
    }
}
