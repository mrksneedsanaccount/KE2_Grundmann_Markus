package src.filetypes;

import src.helperclasses.ConversionSpecs;
import src.propra.imageconverter.ImageConverter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;


/**
 * Superklasse und ihre Subklassen, enthalten die Metainformationen über die Ein- und Ausgabedaten.
 */
public abstract class FileTypeSuper {

    static final int PROPRA_HEADER_OFFSET = 28;
    static final int TGA_HEADER_OFFSET = 18;
    protected final byte bitsprobildpunkt = 24;
    protected File file;
    protected byte[] imageArrays;
    protected int pixels;
    protected short height;
    protected short width;
    protected String[] colourscheme;
    protected Map<String, Integer> colourSchemeMap;
    protected String compression;
    protected ConversionSpecs conversionspec;
    protected byte[] header;
    protected ByteBuffer headerbb;

    public FileTypeSuper() {

    }

    /** Ausgabedatei Konstruktor.
     *
     * @param conversionSpecs Objekt, welches die Informationen über die durchzuführende Operation enthält.
     * @param inputformat das Objekt der Eingabedatei, um das Objekt zu initialisieren. (Hauptzweck ist es die beiden
     *                    Konstruktoren vorneinander zu unterscheiden.)
     */
    public FileTypeSuper(ConversionSpecs conversionSpecs, FileTypeSuper inputformat) {
        // TODO Auto-generated constructor stub
        this.conversionspec = conversionSpecs;
        // Lade die Bilddatei und speichere sie in einem Byte Array.
        file = conversionSpecs.getInputPath().toFile();
        // Höhe und Breite
        height = conversionSpecs.getInputformat().height;
        setWidth(conversionSpecs.getInputformat().getWidth());
        // Datensegmente
        if (conversionSpecs.getOutputFormatString() == ImageConverter.PROPRA) {
            header = new byte[PROPRA_HEADER_OFFSET];
        }
        if (conversionSpecs.getOutputFormatString() == ImageConverter.TGA) {
            header = new byte[TGA_HEADER_OFFSET];
        }
        headerbb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);

        colourSchemeInfo();
    }

    /**
     * Konstruktor für die Eingabedateien.
     *
     * @param conversionSpecs Objekt, welches die Informationen über die durchzuführende Operation enthält.
     */
    public FileTypeSuper(ConversionSpecs conversionSpecs) {
        this.conversionspec = conversionSpecs;
        this.file = conversionSpecs.getInputPath().toFile();
        if (conversionSpecs.getInputFormatString() == ImageConverter.PROPRA) {
            header = new byte[PROPRA_HEADER_OFFSET];
        }
        if (conversionSpecs.getInputFormatString() == ImageConverter.TGA) {
            header = new byte[TGA_HEADER_OFFSET];
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
        heightandwidth();
        compression();
        colourSchemeInfo();
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


    abstract public byte[] buildHeader();


    /** Baut den Header für die AUsgabedatei zusammen
     *
     * @param inputfile Objekt der Eingabedatei.
     * @return Vollständiger Header der Ausgabedatei
     */
    public abstract byte[] buildHeader(FileTypeSuper inputfile);

    /**
     * Legt die Farbkodierung in einer Map
     * und einem StringArray fest.
     */
    protected abstract void colourSchemeInfo();

    /**
     * Legt die Kompression der Eingabedatei fest.
     */
    protected abstract void compression();

    protected byte[] extractDatasegments(int anzahldatasegmente, int offset,
                                         byte[] byteArray) {
        byte[] b = new byte[anzahldatasegmente];
        ByteBuffer bb = ByteBuffer.wrap(byteArray)
                .position(offset).get(b, 0, anzahldatasegmente);
        return b;
    }

    /**
     * Legt die Operationen und Kompressionstypen für das Ausgabeformat und das Conversionspecsobjekt fest.
     *
     * @param convspec Objekt, welches die Informationen über die durchzuführende Operation enthält.
     */
    public void setOperation(ConversionSpecs convspec) {

        if (this.compression == null) {// um das Programm mit dem KE1-Eingabeformat konform zu machen.
            this.compression = convspec.getOperation();
        } else {
            this.compression = conversionspec.getInputformat().getCompression();
            convspec.setOperation(conversionspec.getInputformat().getCompression());
        }
    }

    /**
     *Prüft, ob die Eingabedateien frei von offensichtliche Fehlern sind.
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

    public Map<String, Integer> getColourSchemeMap() {
        return colourSchemeMap;
    }

    public void setColourSchemeMap(Map<String, Integer> colourSchemeMap) {
        this.colourSchemeMap = colourSchemeMap;
    }

    public String[] getColourscheme() {
        return colourscheme;
    }

    public void setColourscheme(String[] colourscheme) {
        this.colourscheme = colourscheme;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public ConversionSpecs getConversionspec() {
        return conversionspec;
    }

    public void setConversionspec(ConversionSpecs conversionspec) {
        this.conversionspec = conversionspec;
    }

    public abstract byte[] getHeader();

    protected void setHeader(byte[] header) {
        this.header = header;
    }

    public short getHeight() {
        return height;
    }

    public void setHeight(short height) {
        this.height = height;
    }

    public int getPixels() {
        return pixels;
    }

    public void setPixels(int pixels) {
        this.pixels = pixels;
    }

    public short getWidth() {
        return width;
    }

    public void setWidth(short width) {
        this.width = width;
    }

    /**
     * Berechnet Höhe und Breite in Pixeln.
     */
    abstract protected void heightandwidth();



    public abstract void setConversionspec();

}
