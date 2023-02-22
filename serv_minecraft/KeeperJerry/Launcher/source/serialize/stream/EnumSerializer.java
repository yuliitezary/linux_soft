package launcher.serialize.stream;

import launcher.LauncherAPI;
import launcher.helper.VerifyHelper;
import launcher.serialize.HInput;
import launcher.serialize.HOutput;
import launcher.serialize.stream.EnumSerializer.Itf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class EnumSerializer<E extends Enum<?> & Itf>
{
    private final Map<Integer, E> map = new HashMap<>(16);

    @LauncherAPI
    public EnumSerializer(Class<E> clazz)
    {
        for (E e : clazz.getEnumConstants())
        {
            VerifyHelper.putIfAbsent(map, e.getNumber(), e, "Duplicate number for enum constant " + e.name());
        }
    }

    @LauncherAPI
    public static void write(HOutput output, Itf itf) throws IOException
    {
        output.writeVarInt(itf.getNumber());
    }

    @LauncherAPI
    public E read(HInput input) throws IOException
    {
        int n = input.readVarInt();
        return VerifyHelper.getMapValue(map, n, "Unknown enum number: " + n);
    }

    @FunctionalInterface
    public interface Itf
    {
        @LauncherAPI
        int getNumber();
    }
}
