package launchserver.response;

import launcher.request.PingRequest;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launchserver.LaunchServer;

import java.io.IOException;

public final class PingResponse extends Response
{
    public PingResponse(LaunchServer server, String ip, HInput input, HOutput output)
    {
        super(server, ip, input, output);
    }

    @Override
    public void reply() throws IOException
    {
        output.writeUnsignedByte(PingRequest.EXPECTED_BYTE);
    }
}
