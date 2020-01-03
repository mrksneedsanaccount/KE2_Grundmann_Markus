package src.filetypes;


import src.helperclasses.ProjectConstants;
import src.helperclasses.ImageFormats;
import src.propra.conversionfacilitators.CommandLineInterpreter;

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

    public FileTypeSuper() {

    }


    /**
     * Ausgabedatei Konstruktor.
     *
     * @param conversionSpecs Objekt, welches die Informationen über die durchzuführende Operation enthält.
     * @param inputFile       das Objekt der Eingabedatei, um das Objekt zu initialisieren. (Hauptzweck ist es die beiden
     *                        Konstruktoren vorneinander zu unterscheiden.)
     */
    public FileTypeSuper(CommandLineInterpreter conversionSpecs, FileTypeSuper inputFile) {
        // TODO Auto-generated constructor stub
        // Lade die Bilddatei und speichere sie in einem Byte Array.
        file = conversionSpecs.getOutputPath().toFile();
        this.filepath = conversionSpecs.getOutputPath();
        // Höhe und Breite
        height = inputFile.height;
        width = inputFile.width;
        // Datensegmente
        if (conversionSpecs.getOutputSuffix().equals(ProjectConstants.PROPRA)) {
            header = new byte[PROPRA_HEADER_OFFSET];
        }
        if (conversionSpecs.getOutputSuffix().equals(ProjectConstants.TGA)) {
            header = new byte[TGA_HEADER_OFFSET];
        }
        headerbb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        setCompression(conversionSpecs.getMode());
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
     * Prüft, ob die Eingabedateien frei von offensichtliche Fehlern sind.
     */
    public void fehlerausgabe() {
        // Fehler finden.
        // Bilddimensionen darauf überprüfen, ob entweder Höhe oder Länge 0
        // sind.
        if ((getWidth() * getHeight()) == 0) {
            System.err.println("mindestens eine Bilddimension ist 0.");
            System.exit(123);
        }
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
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
     * Legt die Kompression der Eingabedatei fest.
     */
    protected abstract void setCompressionFromFile();

    /**
     * Berechnet Höhe und Breite in Pixeln.
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

}
