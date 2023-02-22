package launchserver.auth.handler;

import launcher.helper.IOHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.config.entry.BlockConfigEntry;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BinaryFileAuthHandler extends FileAuthHandler
{
    BinaryFileAuthHandler(BlockConfigEntry block)
    {
        super(block);
    }

    @Override
    protected void readAuthFile() throws IOException
    {
        try (HInput input = new HInput(IOHelper.newInput(file)))
        {
            int count = input.readLength(0);
            for (int i = 0; i < count; i++)
            {
                UUID uuid = input.readUUID();
                Entry entry = new Entry(input);
                addAuth(uuid, entry);
            }
        }
    }

    @Override
    protected void writeAuthFileTmp() throws IOException
    {
        Set<Map.Entry<UUID, Entry>> entrySet = entrySet();
        try (HOutput output = new HOutput(IOHelper.newOutput(fileTmp)))
        {
            output.writeLength(entrySet.size(), 0);
            for (Map.Entry<UUID, Entry> entry : entrySet)
            {
                output.writeUUID(entry.getKey());
                entry.getValue().write(output);
            }
        }
    }
}
