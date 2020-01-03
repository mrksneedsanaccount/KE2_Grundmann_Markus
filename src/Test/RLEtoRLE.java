package src.Test;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class RLEtoRLE {


    byte singleByteBuffer = 0;
    int rleCounter = 0;
    int rawCounter = 0;
    int counter = 0;
    byte[] pixel = new byte[3];
    byte[] pixels;
    ByteBuffer byteBuffer;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


    public RLEtoRLE(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public void run() throws IOException {

        Mode mode = null;


        while (byteBuffer.hasRemaining()) { //da damit garanitiwer wird byteBuffer.position() < limit
            if (mode == null) {
                singleByteBuffer = byteBuffer.get();
                byteArrayOutputStream.write(singleByteBuffer);
                if (singleByteBuffer < 0) {
                    rleCounter = (singleByteBuffer & 0x7f) + 1;
                    mode = Mode.RLE;
                } else {
                    rawCounter = singleByteBuffer + 1;
                    mode = Mode.RAW;
                }
            }
            if (mode == Mode.RLE) {
                if (byteBuffer.limit() - byteBuffer.position() < 3) {

                    break;
                }

                byteBuffer.get(pixel);
                //Todo Farbkodierungsmethode
                byteArrayOutputStream.write(pixel);
                mode = null;

            } else if (mode == Mode.RAW) {
                if (byteBuffer.limit() - byteBuffer.position() < 3 * rawCounter) {

                    break;
                }
                for (int i = 0; i < rawCounter; i++) {
                    byteBuffer.get(pixel);
                    //Todo Farbkodierungsmethode
                    byteArrayOutputStream.write(pixel);
                }
                mode = null;
            }

        }
        byteBuffer.compact();

    }



    public void getOutputArray(ByteArrayOutputStream outputStream) throws IOException {
         byteArrayOutputStream.writeTo(outputStream);
        byteArrayOutputStream.reset();
    }









    enum Mode {
        RLE, RAW
    }
}