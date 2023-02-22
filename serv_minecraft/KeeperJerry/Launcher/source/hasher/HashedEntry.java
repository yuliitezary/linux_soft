package launcher.hasher;

import launcher.LauncherAPI;
import launcher.serialize.HInput;
import launcher.serialize.stream.EnumSerializer;
import launcher.serialize.stream.EnumSerializer.Itf;
import launcher.serialize.stream.StreamObject;

import java.io.IOException;

public abstract class HashedEntry extends StreamObject
{
    @LauncherAPI
    public boolean flag; // For external usage

    @LauncherAPI
    public abstract Type getType();

    @LauncherAPI
    public abstract long size();

    @LauncherAPI
    public enum Type implements Itf
    {
        DIR(1), FILE(2);
        private static final EnumSerializer<Type> SERIALIZER = new EnumSerializer<>(Type.class);
        private final int n;

        Type(int n)
        {
            this.n = n;
        }

        public static Type read(HInput input) throws IOException
        {
            return SERIALIZER.read(input);
        }

        @Override
        public int getNumber()
        {
            return n;
        }
    }
}
