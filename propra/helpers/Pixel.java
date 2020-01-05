package propra.helpers;

import java.nio.ByteBuffer;

public class Pixel {

    public static ImageFormats inputFormat;
    public static ImageFormats outputFormat;
    static int[] offsets;
    static byte[] pixel = new byte[3];

    public static void changePixelOrder(byte[] pixel, byte[] outputPixel, int[] offsets) {
        outputPixel[offsets[0]] = pixel[0];
        outputPixel[offsets[1] + 1] = pixel[1];
        outputPixel[offsets[2] + 2] = pixel[2];
    }

    public static byte[] changePixelOrder(byte[] pixels, int[] offsets) {
        int i = 0;
        byte[] outputPixel = new byte[pixels.length];
        while (i < pixels.length) {
            outputPixel[offsets[0] + i] = pixels[i];
            i++;
            outputPixel[offsets[1] + i] = pixels[i];
            i++;
            outputPixel[offsets[2] + i] = pixels[i];
            i++;
        }
        return outputPixel;
    }

    public static byte[] getPixel(byte singleByte, int bytePostion) {
//        .tga BGR
//        .prpra GBR
//        RGB ist das Zwischenspeicherformat.
        if (inputFormat == ImageFormats.BGR) {
            switch (bytePostion % 3) {
                case 0:
                    pixel[2] = singleByte;
                    break;
                case 1:
                    pixel[1] = singleByte;
                    break;
                case 2:
                    pixel[0] = singleByte;
                    break;
            }

        } else {
            switch (bytePostion % 3) {
                case 0:
                    pixel[1] = singleByte;
                    break;
                case 1:
                    pixel[2] = singleByte;
                    break;
                case 2:
                    pixel[0] = singleByte;
                    break;
            }
        }
        return pixel;
    }

    public static byte[] getPixel(byte[] inputPixel) {
        //        .tga BGR
        //        .prpra GBR
        //        RGB ist das Zwischenspeicherformat.
        byte r;
        byte g;
        byte b;

        if (inputFormat == ImageFormats.BGR) {
            b = inputPixel[0];
            g = inputPixel[1];
            r = inputPixel[2];

            pixel[0] = r;
            pixel[1] = g;
            pixel[2] = b;

        } else {
            g = inputPixel[0];
            b = inputPixel[1];
            r = inputPixel[2];

            pixel[0] = r;
            pixel[1] = g;
            pixel[2] = b;

        }
        return pixel;
    }

    public static void getPixelFromBuffer(ByteBuffer byteBuffer, byte[] pixel) {
        pixel[0] = byteBuffer.get();
        pixel[1] = byteBuffer.get();
        pixel[2] = byteBuffer.get();
    }

    public static byte[] putPixel(byte[] inputPixel) {
        //        .tga BGR
        //        .prpra GBR
        //        RGB ist das Zwischenspeicherformat.
        byte r;
        byte g;
        byte b;


        if (outputFormat == ImageFormats.BGR) {
            r = inputPixel[0];
            g = inputPixel[1];
            b = inputPixel[2];

            pixel[0] = b;
            pixel[1] = g;
            pixel[2] = r;


        } else {
            r = inputPixel[0];
            g = inputPixel[1];
            b = inputPixel[2];

            pixel[0] = g;
            pixel[1] = b;
            pixel[2] = r;

        }
        return pixel;
    }

    public static void setInputFormat(ImageFormats inputFormat) {
        Pixel.inputFormat = inputFormat;
    }

    public static void setOutputFormat(ImageFormats outputFormat) {
        Pixel.outputFormat = outputFormat;
    }

    public static byte[] transformPixel(byte[] inputPixel) {
        return putPixel(getPixel(inputPixel));
    }


}
