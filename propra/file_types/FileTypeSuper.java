package propra.file_types;


import propra.conversion_facilitators.CommandLineInterpreter;
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
     * @param commandLineInterpreter contains a 'translation' of the input to the program
     * @param inputFile
     */
    public FileTypeSuper(CommandLineInterpreter commandLineInterpreter, FileTypeSuper inputFile) {
        // TODO Auto-generated constructor stub
        // Lade die Bilddatei und speichere sie in einem Byte Array.
        file = commandLineInterpreter.getOutputPath().toFile();
        this.filepath = commandLineInterpreter.getOutputPath();
        // Höhe und Breite
        height = inputFile.height;
        width = inputFile.width;
        // Datensegmente
        if (commandLineInterpreter.getOutputSuffix().equals(ProjectConstants.PROPRA)) {
            header = new byte[PROPRA_HEADER_OFFSET];
        }
        if (commandLineInterpreter.getOutputSuffix().equals(ProjectConstants.TGA)) {
            header = new byte[TGA_HEADER_OFFSET];
        }
        headerbb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        setCompression(commandLineInterpreter.getMode());
    }

    /**
     * Konstruktor für die Eingabedateien.
     *
     * @param commandLineInterpreter Objekt, welches die Informationen über die durchzuführende Operation enthält.
     */
    public FileTypeSuper(CommandLineInterpreter commandLineInterpreter) {
        this.filepath = commandLineInterpreter.getInputPath();
        this.file = commandLineInterpreter.getInputPath().toFile();
        if (commandLineInterpreter.getInputSuffix().equals(ProjectConstants.PROPRA)) {
            header = new byte[PROPRA_HEADER_OFFSET];
        } else if (commandLineInterpreter.getInputSuffix().equals(ProjectConstants.TGA)) {
            header = new byte[TGA_HEADER_OFFSET];
        } else {
            System.out.println("This Program only supports the TARGA and the ProPra2019 formats, please check your inputfile.");
            System.exit(123);
        }
        try {
            // Speichere den Header der Eingabedatei in ihren Objekten Objekt.
            BufferedInputStream inputStream = new BufferedInputStream(
                    new FileInputStream(file));
            inputStream.read(header);
            inputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        headerbb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        setHeightandWidth();
        setCompressionFromFile();
    }

    /**
     * Vervollständigt die Metadaten des Ausgabeobjekts, so weit wie möglich, bevor der Header geschrieben wird.
     *
     * @param inputfile Objekt der Eingabedatei.
     */
    public abstract void ausgabeHeaderFertigInitialisieren(FileTypeSuper inputfile);

    public void ausgabeHeaderFertigInitialisieren() {
        // TODO Auto-generated method stub

    }

    /**
     * Baut den Header für die AUsgabedatei zusammen
     *
     * @param inputfile Objekt der Eingabedatei.
     * @return Vollständiger Header der Ausgabedatei
     */
    public abstract byte[] buildHeader(FileTypeSuper inputfile);

    public abstract void calculateChecksum(byte dataByte);

    public abstract void calculateChecksumOfArray(byte[] pixel);

    public abstract void calculateChecksumOfByteBuffer(ByteBuffer pixelBuffer, int limit);

    /**
     * Checks the validity of the file by comparing the header data to the file.
     */
    public void fehlerausgabe() {

        // ensuring that the image has valid dimensions.
        if ((getWidth() * getHeight()) == 0) {
            System.err.println("At least one of the dimensions is 0.");
            System.exit(123);
        }
    }

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
    protected abstract void setCompressionFromFile();

    /**
     * Extracts the height and width from the header
     * and saves it in the object.
     */
    abstract protected void setHeightandWidth();


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
