import java.io.DataInputStream;
import java.io.IOException;

public class NBTReader {
    public static String readString(DataInputStream input) throws IOException {
        int length = input.readUnsignedShort();
        byte[] bytes = new byte[length];
        input.readFully(bytes);
        return new String(bytes, "UTF-8");
    }

    // Add more methods to read other NBT types as needed
}
