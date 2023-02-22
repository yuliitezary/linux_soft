package launchserver.auth.provider;

import launcher.LauncherAPI;
import launcher.helper.SecurityHelper;
import launcher.helper.SecurityHelper.DigestAlgorithm;
import launcher.serialize.config.entry.BlockConfigEntry;
import launcher.serialize.config.entry.StringConfigEntry;
import launchserver.auth.AuthException;

public abstract class DigestAuthProvider extends AuthProvider
{
    private final DigestAlgorithm digest;

    @LauncherAPI
    protected DigestAuthProvider(BlockConfigEntry block)
    {
        super(block);
        digest = DigestAlgorithm.byName(block.getEntryValue("digest", StringConfigEntry.class));
    }

    @LauncherAPI
    protected final void verifyDigest(String validDigest, String password) throws AuthException
    {
        boolean valid;
        if (digest == DigestAlgorithm.PLAIN)
        {
            valid = password.equals(validDigest);
        }
        else if (validDigest == null)
        {
            valid = false;
        }
        else
        {
            byte[] actualDigest = SecurityHelper.digest(digest, password);
            valid = SecurityHelper.toHex(actualDigest).equals(validDigest);
        }

        // Verify is valid
        if (!valid)
        {
            authError("Incorrect username or password");
        }
    }
}
