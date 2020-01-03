package src.Test;


public class einfacheFarbkodierungsKonversion {

    /**
     * Diese Klasse enthält, die Algorithmen für die "einfache" Konvertiertung
     * zwischen Datensegmenten, welche die gleiche Konviertierung benutzen, aber
     * unterschiedliche Farbkodierungen.
     */
    public einfacheFarbkodierungsKonversion() {


    }

    /**
     * Methode zur Konvertierung von Datensegmenten, die den gleichen Konvertierungsalgorithmus benuten,
     * aber unterschiedliche Farbkodierungen (bei gleicher Anzahl von Bytes pro Pixel und gleicher Farbmenge (RBG))
     *
     * @param convspec Hilfsobjekt, welches die für die Operation relevanten Daten enthält.
     * @return konvertiertes ByteArray
     */
//    public static byte[] convertColourscheme(ConversionSpecs convspec) {
//        //Initialisierung
//        int[] offsets = convspec.offsetCalculator(convspec);
//        byte[] datasegment = convspec.getInputDatasegments();
//        byte[] outputarray = new byte[datasegment.length];
//
//        if (convspec.getOperation() == ImageConverter.UNCOMPRESSED) {
//            for (int i = 0; i < (outputarray.length); i += 3) {
//                outputarray[i] = datasegment[i + offsets[0]];
//                outputarray[i + 1] = datasegment[i + offsets[1] + 1];
//                outputarray[i + 2] = datasegment[i + offsets[2] + 2];
//            }
//        }
//        if (convspec.getOperation() == ImageConverter.RLE) {
//            int i = 0;
//            while (i < (datasegment.length - 3)) {
//                if (datasegment[i] < 0) {
//                    outputarray[i] = datasegment[i];
//                    i++;
//                    outputarray[i] = datasegment[i + offsets[0]];
//                    outputarray[i + 1] = datasegment[i + offsets[1] + 1];
//                    outputarray[i + 2] = datasegment[i + offsets[2] + 2];
//                    i += 3;
//                    continue;
//                }
//                if (datasegment[i] > -1) {
//                    outputarray[i] = datasegment[i];
//                    int laeufer = (datasegment[i] + 1);
//                    i++;
//                    // jedes Pixel 3 byte lang und +1 (siehe TARGA-Spezifikation)
//                    for (int j = 0; j < laeufer; j++) {
//                        outputarray[i] = datasegment[i + offsets[0]];
//                        outputarray[i + 1] = datasegment[i + offsets[1] + 1];
//                        outputarray[i + 2] = datasegment[i + offsets[2] + 2];
//                        i += 3;
//                    }
//                }
//
//            }
//
//        }
//
//        return outputarray;
//
//    }

//    public static void convertColourscheme2(ConversionSpecs conversionSpecs) throws IOException {
//
//        FileChannel fileChannel = HelperMethods.initialiseInputChannel( conversionSpecs.getInputPath().toFile(), conversionSpecs.getInputformat().getHeader().length);
//
//        HelperMethods.initialiseOutputFile(conversionSpecs.getOutputformat());
//
//
//
//        int[] offsets = conversionSpecs.getOffsets();
//        int counter = 0;
//        byte[][] pixels = new byte[conversionSpecs.getInputformat().getWidth()][3];
//        ByteBuffer byteBuffer = ByteBuffer.allocate(64 * 1024);
//        ByteBuffer lineBuffer = ByteBuffer.allocate(conversionSpecs.getInputformat().getWidth() * 3);
//
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
//        ReadableByteChannel readableByteChannel = Channels.newChannel(byteArrayInputStream);
//        ByteArrayOutputStream rleByteArrayOutputStream = new ByteArrayOutputStream();
//
//        if (conversionSpecs.getOperation() == Constants.UNCOMPRESSED) {
//            byte[] pixel = new byte[3];
//            byte[] outputPixel = new byte[3];
//
//            while (fileChannel.read(byteBuffer) > -1) {
//                byteBuffer.flip();
//                while ((byteBuffer.remaining()) >= 3) {
//                    Pixel.getPixelFromBuffer(byteBuffer, pixel);
//                    //CHECKSUM
//                    for (byte b : pixel) {
//                        conversionSpecs.getInputformat().calculateChecksum(b);
//                    }
//
//                    Pixel.changePixelOrder(pixel, outputPixel, offsets);
//                    conversionSpecs.getOutputformat().calculateChecksumOfArray(outputPixel);
//                    rleByteArrayOutputStream.write(outputPixel);
//                }
//                if (rleByteArrayOutputStream.size() >= byteBuffer.limit()) {
//                    Files.write(conversionSpecs.getOutputPath(), rleByteArrayOutputStream.toByteArray(), StandardOpenOption.APPEND);
//                    rleByteArrayOutputStream.reset();
//                }
//                byteBuffer.compact();
//            }
//
//
//        } else {
//
//            int limit;
//            Mode mode = null;
//            byte singleByteBuffer;
//            int rleCounter = 0;
//            int rawCounter = 0;
//            byte[] pixel = new byte[3];
//            byte[] outputPixel = new byte[3];
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//            while ((limit = fileChannel.read(byteBuffer)) > 1) {
//                byteBuffer.rewind();
//                while (byteBuffer.position() < byteBuffer.limit()) { //da damit garanitiwer wird byteBuffer.position() < limit
//                    if (mode == null) {
//                        singleByteBuffer = byteBuffer.get();
//                        conversionSpecs.getInputformat().calculateChecksum(singleByteBuffer);
//                        conversionSpecs.getOutputformat().calculateChecksum(singleByteBuffer);
//                        rleByteArrayOutputStream.write(singleByteBuffer);
//                        if (singleByteBuffer < 0) {
//                            rleCounter = (singleByteBuffer & 0x7f) + 1;
//                            mode = Mode.RLE;
//                        } else {
//                            rawCounter = singleByteBuffer + 1;
//                            mode = Mode.RAW;
//                        }
//                    }
//                    if (mode == Mode.RLE) {
//                        if (byteBuffer.limit() - byteBuffer.position() < 3) {
//                            if (rleByteArrayOutputStream.size() >= byteBuffer.limit()) {
//                                Files.write(conversionSpecs.getOutputPath(), rleByteArrayOutputStream.toByteArray(), StandardOpenOption.APPEND);
//                                rleByteArrayOutputStream.reset();
//                            }
//                            byteBuffer.compact();
//                            int position = byteBuffer.position();
//                            limit = fileChannel.read(byteBuffer);
//                            byteBuffer.limit(limit + position);
//                            byteBuffer.rewind();
//                        }
//
//                        //InputPixel
//                        Pixel.getPixelFromBuffer(byteBuffer, pixel);
//                        conversionSpecs.getInputformat().calculateChecksumOfArray(pixel);
//                        //Outputpixel
//                        Pixel.changePixelOrder(pixel, outputPixel, offsets);
//                        conversionSpecs.getOutputformat().calculateChecksumOfArray(outputPixel);
//                        rleByteArrayOutputStream.write(outputPixel);
//                        mode = null;
//
//                    } else if (mode == Mode.RAW) {
//                        if (byteBuffer.limit() - byteBuffer.position() < 3 * rawCounter) {
//                            if (rleByteArrayOutputStream.size() >= byteBuffer.limit()) {
//                                Files.write(conversionSpecs.getOutputPath(), rleByteArrayOutputStream.toByteArray(), StandardOpenOption.APPEND);
//                                rleByteArrayOutputStream.reset();
//                            }
//                            byteBuffer.compact();
//                            int position = byteBuffer.position();
//                            limit = fileChannel.read(byteBuffer);
//                            byteBuffer.limit(limit + position);
//                            byteBuffer.rewind();
//                        }
//                        for (int i = 0; i < rawCounter; i++) {
//                            //InputPixel
//                            Pixel.getPixelFromBuffer(byteBuffer, pixel);
//                            conversionSpecs.getInputformat().calculateChecksumOfArray(pixel);
//                            //Outputpixel
//                            Pixel.changePixelOrder(pixel, outputPixel, offsets);
//                            conversionSpecs.getOutputformat().calculateChecksumOfArray(outputPixel);
//                            rleByteArrayOutputStream.write(outputPixel);
//                        }
//                        mode = null;
//                    }
//                }
//                if (rleByteArrayOutputStream.size() >= byteBuffer.limit()) {
//                    Files.write(conversionSpecs.getOutputPath(), rleByteArrayOutputStream.toByteArray(), StandardOpenOption.APPEND);
//                    rleByteArrayOutputStream.reset();
//                }
//                byteBuffer.compact();
//            }
//        }
//    }


    enum Mode {
        RAW, RLE, START
    }


}