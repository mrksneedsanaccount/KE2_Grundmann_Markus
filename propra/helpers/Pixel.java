package propra.helpers;

import propra.file_types.ImageFormats;

import java.nio.ByteBuffer;

public class Pixel {

    public static ImageFormats inputFormat;
    public static ImageFormats outputFormat;
    static int[] offsets;
    static byte[] pixel = new byte[3];
    private static byte inR;
    private static byte inB;
    private static byte inG;

    private static byte outR;
    private static byte outB;
    private static byte outG;


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
        //        byte r;
        //        byte g;
        //        byte b;

        if (inputFormat == ImageFormats.BGR) {
            outB = inputPixel[0];
            outG = inputPixel[1];
            outR = inputPixel[2];
            pixel[0] = outR;
            pixel[1] = outG;
            pixel[2] = outB;
        } else if (inputFormat == ImageFormats.RGB) {
            pixel[0] = inputPixel[0];
            pixel[1] = inputPixel[1];
            pixel[2] = inputPixel[2];
        } else {
            outG = inputPixel[0];
            outB = inputPixel[1];
            outR = inputPixel[2];
            pixel[0] = outR;
            pixel[1] = outG;
            pixel[2] = outB;

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
//        byte r;
//        byte g;
//        byte b;


        if (outputFormat == ImageFormats.BGR) {
            inR = inputPixel[0];
            inG = inputPixel[1];
            inB = inputPixel[2];
            pixel[0] = inB;
            pixel[1] = inG;
            pixel[2] = inR;
        } else if (outputFormat == ImageFormats.RGB) {
            inR = inputPixel[0];
            inG = inputPixel[1];
            inB = inputPixel[2];
            pixel[0] = inR;
            pixel[1] = inG;
            pixel[2] = inB;
        } else {
            inR = inputPixel[0];
            inG = inputPixel[1];
            inB = inputPixel[2];
            pixel[0] = inG;
            pixel[1] = inB;
            pixel[2] = inR;
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
