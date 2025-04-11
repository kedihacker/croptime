import java.io.DataOutputStream;
import java.io.IOException;

public class NBTWriter {
    public static void writeString(DataOutputStream output, String value) throws IOException {
        byte[] bytes = value.getBytes("UTF-8");
        output.writeShort(bytes.length);
        output.write(bytes);
    }

    // Add more methods to write other NBT types as needed
}
