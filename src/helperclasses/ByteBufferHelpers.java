package src.helperclasses;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ByteBufferHelpers {
    public static void refillEmptyBuffer(ByteBuffer datasegmentByteBuffer, FileChannel inFileChannel) throws IOException {
        if (!datasegmentByteBuffer.hasRemaining()) {
            datasegmentByteBuffer.clear();
            inFileChannel.read(datasegmentByteBuffer);
            datasegmentByteBuffer.flip();
        }
    }
}
