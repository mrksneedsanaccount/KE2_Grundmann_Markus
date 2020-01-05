package propra.helpers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ByteBufferHelpers {
    public static void refillEmptyBuffer(ByteBuffer byteBuffer, FileChannel inFileChannel) throws IOException {
        if (!byteBuffer.hasRemaining()) {
            byteBuffer.clear();
            if (inFileChannel.read(byteBuffer) < 0) {
                System.err.println("Something went wrong when reading the encoded Huffman tree.");
                System.exit(123);
            }
            byteBuffer.flip();
        }
    }
}
