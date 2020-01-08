package propra.file_types;


import propra.exceptions.IllegalHeaderException;
import propra.exceptions.InvalidChecksumException;
import propra.exceptions.UnknownCompressionException;
import propra.helpers.ImageFormats;
import propra.helpers.ProjectConstants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;


/**
 * Superklasse und ihre Subklassen, enthalten die Metainformationen über die Ein- und Ausgabedaten.
 */
public abstract class FileTypeSuper {

    static final int PROPRA_HEADER_OFFSET = 28;
    static final int TGA_HEADER_OFFSET = 18;
    protected final byte bitsprobildpunkt = 24;
    protected File file;
    protected short height;
    protected short width;
    protected String compression;
    protected byte[] header;
    protected ByteBuffer headerbb;
    protected Path filepath;
    ImageFormats imageFormats;
    Orientation orientation = Orientation.TOP_LEFT;

    public FileTypeSuper() {

    }


    /**
     * Constructor for output file.
     *
     * @param inputFile
     * @param outputPath
     * @param outputSuffix
     * @param mode
     */
    public FileTypeSuper(FileTypeSuper inputFile, Path outputPath, String outputSuffix, String mode) {
        // TODO Auto-generated constructor stub
        // Lade die Bilddatei und speichere sie in einem Byte Array.
        file = outputPath.toFile();
        this.filepath = outputPath;
        // Höhe und Breite
        height = inputFile.height;
        width = inputFile.width;
        // Datensegmente
        if (outputSuffix.equals(ProjectConstants.PROPRA)) {
            header = new byte[PROPRA_HEADER_OFFSET];
        }
        if (outputSuffix.equals(ProjectConstants.TGA)) {
            header = new byte[TGA_HEADER_OFFSET];
        }
        headerbb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        setCompression(mode);
    }

    /**
     * Constructor for source files.
     *
     * @param inputPath
     * @param file
     * @param inputSuffix
     */
    public FileTypeSuper(Path inputPath, File file, String inputSuffix) throws UnknownCompressionException {
        this.filepath = inputPath;
        this.file = file;
        if (inputSuffix.equals(ProjectConstants.PROPRA)) {
            header = new byte[PROPRA_HEADER_OFFSET];
        } else if (inputSuffix.equals(ProjectConstants.TGA)) {
            header = new byte[TGA_HEADER_OFFSET];
        } else {
            System.out.println("This Program only supports the TARGA and the ProPra2019 formats, please check your inputfile.");
            System.exit(123);
        }
        try {
            // Speichere den Header der Eingabedatei in ihren Objekten Objekt.
            BufferedInputStream inputStream = new BufferedInputStream(
                    new FileInputStream(this.file));
            inputStream.read(header);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(123);
        }
        headerbb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        setHeightAndWidth();
        setCompressionFromFile();
    }

    /**
     * Builds the header for the output file.
     *
     * @param inputfile object of the sorce file.
     * @return complete header ready for writing to file.
     */
    public abstract byte[] buildHeader(FileTypeSuper inputfile) throws UnknownCompressionException;

    public abstract void calculateChecksum(byte dataByte);

    public abstract void calculateChecksumOfArray(byte[] pixel);

    public abstract void calculateChecksumOfByteBuffer(ByteBuffer pixelBuffer, int limit);

    public abstract void checkChecksum() throws InvalidChecksumException;

    /**
     * Checks the validity of the file by comparing the header data to the file.
     */
    public void checkForErrorsInHeader() throws IllegalHeaderException {

        // ensuring that the image has valid dimensions.
        if ((getWidth() * getHeight()) == 0) {
            throw new IllegalHeaderException();
        }
    }

    /**
     * Vervollständigt die Metadaten des Ausgabeobjekts, so weit wie möglich, bevor der Header geschrieben wird.
     *
     * @param inputfile Objekt der Eingabedatei.
     */
    public abstract void finalizeOutputHeader(FileTypeSuper inputfile) throws UnknownCompressionException;


    public String getCompression() {
        return compression;
    }


    public void setCompression(String compression) {
        this.compression = compression;


    }

    public Path getFilepath() {
        return filepath;
    }

    public abstract byte[] getHeader();

    public short getHeight() {
        return height;
    }

    public ImageFormats getImageFormat() {
        return imageFormats;
    }

    public short getWidth() {
        return width;
    }

    public void setWidth(short width) {
        this.width = width;
    }

    abstract int returnChecksum();

    /**
     * Determines the compression type based on header data
     */
    protected abstract void setCompressionFromFile() throws UnknownCompressionException;

    public abstract void setCompressionOutputFile(String mode) throws UnknownCompressionException;

    /**
     * Extracts the height and width from the header
     * and saves it in the object.
     */
    abstract protected void setHeightAndWidth();


//    /**
//     * Legt die Operationen und Kompressionstypen für das Ausgabeformat und das Conversionspecsobjekt fest.
//     *
//     * @param convspec Objekt, welches die Informationen über die durchzuführende Operation enthält.
//     */
//    public void setOperation(Conversions convspec) {
//

//
//    }

    enum Orientation {BOTTOM_LEFT, TOP_LEFT}

}
