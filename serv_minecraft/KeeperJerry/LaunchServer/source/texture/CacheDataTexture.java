package launchserver.texture;

import launcher.client.PlayerProfile.Texture;

public final class CacheDataTexture
{
    protected final Texture skin, cloak;
    protected final Throwable exc;
    protected final long until;

    protected CacheDataTexture(Texture skin, Texture cloak, long until)
    {
        this.skin = skin;
        this.cloak = cloak;
        this.until = until;
        exc = null;
    }

    protected CacheDataTexture(Throwable exc, long until)
    {
        this.exc = exc;
        this.until = until;
        skin = cloak = null;
    }
}