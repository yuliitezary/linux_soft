package launchserver.response.update;

import launcher.hasher.HashedDir;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.signed.SignedObjectHolder;
import launchserver.LaunchServer;
import launchserver.response.Response;

import java.util.Map.Entry;
import java.util.Set;

public final class UpdateListResponse extends Response
{
    public UpdateListResponse(LaunchServer server, String ip, HInput input, HOutput output)
    {
        super(server, ip, input, output);
    }

    @Override
    public void reply() throws Throwable
    {
        Set<Entry<String, SignedObjectHolder<HashedDir>>> updateDirs = server.getUpdateDirs();

        // Write all update dirs names
        output.writeLength(updateDirs.size(), 0);
        for (Entry<String, SignedObjectHolder<HashedDir>> entry : updateDirs)
        {
            output.writeString(entry.getKey(), 255);
        }
    }
}
