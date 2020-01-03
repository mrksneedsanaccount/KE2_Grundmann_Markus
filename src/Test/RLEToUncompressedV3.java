package src.Test;

import src.helperclasses.Pixel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class RLEToUncompressedV3 {


    Mode mode = null;
    byte singleByteBuffer = 0;

    int counter = 0;
    byte[] pixel = new byte[3];
    byte[] pixels;
    ByteBuffer byteBuffer;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private int[] offset;


    public RLEToUncompressedV3(ByteBuffer byteBuffer, int[] offset){
        this.byteBuffer = byteBuffer;
        this.offset = offset;
    }

    private byte[] processRLEPacket(ByteBuffer byteBuffer, Mode mode, int counter) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] pixel = new byte[3];

        if (mode == Mode.RLE) {
            //Todo Farbkodierungsmethode
            byteBuffer.get(pixel);
            pixel = Pixel.changePixelOrder(pixel, offset);
            for (int i = 0; i < counter; i++) {
                byteArrayOutputStream.write(pixel);
            }

        } else if (mode == Mode.RAW) {
            //Todo Farbkodierungsmethode
            for (int i = 0; i < counter; i++) {
                byteBuffer.get(pixel);
                pixel = Pixel.changePixelOrder(pixel, offset);
                byteArrayOutputStream.write(pixel);
            }

        }

        return byteArrayOutputStream.toByteArray();
    }

    public void run() throws IOException {
        while (true) { //da damit garanitiwer wird byteBuffer.position() < limit
            if (mode == null) {
                singleByteBuffer = byteBuffer.get();
                if (singleByteBuffer < 0) {
                    counter = (singleByteBuffer & 0x7f) + 1;
                    mode = Mode.RLE;
                } else {
                    counter = singleByteBuffer + 1;
                    mode = Mode.RAW;
                }
            }
            if ((byteBuffer.remaining() < counter * 3 && mode == Mode.RAW) || (byteBuffer.remaining() < 3 && mode == Mode.RLE)) {
                break;
            }
            pixels = processRLEPacket(byteBuffer, mode, counter);
            byteArrayOutputStream.write(pixels);
            mode = null;
            if (byteBuffer.remaining() < 1) {
                break;
            }
        }
    }




    public void getOutputArray(ByteArrayOutputStream outputStream) throws IOException {
        byteArrayOutputStream.writeTo(outputStream);
        byteArrayOutputStream.reset();
    }


    enum Mode {
        RLE, RAW
    }
}