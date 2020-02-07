package propra.file_types;

import propra.exceptions.IllegalHeaderException;
import propra.exceptions.UnknownCompressionException;
import propra.helpers.ProjectConstants;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * Klasse, welche die Metadaten von Objekten im TGA-Format enthält.
 */
public class TGA extends FileTypeSuper {


    // .propra
    // 0-9 Formatkennung
    // 10-11 Bildbreite
    // 12-13 Bildhöhe
    // 14 Bits pro Bildpunkt 24
    // 15 Kompressionstyp = 0
    // 16- 23 Datensegmentgröße (3*Breite*Höhe)
    // 24-27 Prüfsumme
    // 28-x Datensegmente
    // .tga
    // 0 Bild-ID
    // 1 Farbpalettentyp
    // 2 Bildtyp (hier 2)
    // 3-4 Palettenbeginn
    // 5-6 Palettenlänge
    // 7 Größe eines Paletteneintrags
    // 8-9 X-Koordinate
    // 10-11 Y-Koordinate
    // 12-13 Bildbreite
    // 14-15 Bildhöhe
    // 16 Bits pro Bildpunkt
    // 17 Bild-Attribut-Byte
    // 18 - x Bilddaten
    // (x+1)-y Rest


    private final byte bildID = 0;
    private final byte farbPallete = 0;
    private final short palettenBeginn = 0;
    private final short palettenLaenge = 0;
    private final byte groessePalleteneintrag = 0;
    short ycoord;
    private byte bildTyp;
    private short xcoord = -1;
    private byte bildAttributByte = 0x20;


    /**
     * Constructor for objects holding the metadata of the source file.
     *
     * @param inputPath   String representation of the input path.
     * @param file        The input picture's input File object.
     * @param inputSuffix String representation of the suffix.
     */
    public TGA(Path inputPath, File file, String inputSuffix) throws UnknownCompressionException {
        super(inputPath, file, inputSuffix);
        imageFormats = ImageFormats.BGR;
    }


    /**
     * Outpfile Konstruktor
     *
     * @param inputFile    Das Objekt, der Eingabedatei. (Zur Unterscheidung der Eingabe- und Ausgabedateiheader
     * @param outputPath String representation of the output path.
     * @param outputSuffix String
     * @param mode What kind compression is to be performed.
     */
    public TGA(FileTypeSuper inputFile, Path outputPath, String outputSuffix, String mode) {
        super(inputFile, outputPath, outputSuffix, mode);
        imageFormats = ImageFormats.BGR;
        bildAttributByte = 0x20;


    }

    @Override
    public void checkChecksum() {

    }

    @Override
    public byte[] buildHeader(FileTypeSuper inputfile) {

        switch (getCompression()) {
            case ProjectConstants.UNCOMPRESSED:
                bildTyp = 2;
                break;
            case ProjectConstants.RLE:
                bildTyp = 10;
                break;
        }
        headerbb.put(2, bildTyp);
        headerbb.putShort(10, inputfile.height);
        headerbb.putShort(12, inputfile.getWidth());
        headerbb.putShort(14, inputfile.height);
        headerbb.put(16, bitsprobildpunkt);
        headerbb.put(17, (byte) 0x20);
        return headerbb.array();
    }

    @Override
    public void calculateChecksum(byte dataByte) {

    }

    @Override
    public void calculateChecksumOfArray(byte[] pixel) {

    }

    @Override
    public void calculateChecksumOfByteBuffer(ByteBuffer pixelBuffer, int limit) {

    }

    @Override
    public void calculateChecksumOfByteArray(byte[] pixelBuffer, int limit) {

    }

    @Override
    public void checkForErrorsInHeader() throws IllegalHeaderException {
        super.checkForErrorsInHeader();
        // image dimensions
        if ((getWidth() * height) == 0) {
            throw new IllegalHeaderException("At least one of the image dimensions in the header is 0.");
        }
        //image origin point
        if (headerbb.getShort(8) != 0 || headerbb.getShort(10) != height) {
            throw new IllegalHeaderException("The origin of TGA file is not in the top left corner." + "" +
                    "You can use a free tool (GIMP for example) to change the origin point of your source file.");
        }
        // image type
        if (!(header[2] == 2 || header[2] == 10)) {
            throw new IllegalHeaderException("Image type is not supported.");
        }
        // Zu wenig Bilddaten
        if (bildTyp == 2) {
            if (getWidth() * height * 3 > (filepath.toFile().length() - TGA_HEADER_OFFSET)) {
                throw new IllegalHeaderException("This file's datas egment is smaller than the required minimum size.");
            }
        }

    }

    @Override
    public void finalizeOutputHeader(FileTypeSuper inputfile) {
//        headerbb.put(2, bildTyp);
//        headerbb.putShort(10, height);
//        headerbb.putShort(12, width);
//        headerbb.putShort(14, height);
//        headerbb.put(16, bitsprobildpunkt);
//        headerbb.put(17, bildAttributByte);


    }

    // Methoden

    public byte[] getHeader() {
        return header;
    }

    @Override
    int returnChecksum() {
        return 0;
    }

    @Override
    protected void setCompressionFromFile() throws UnknownCompressionException {
        // TODO Auto-generated method stub
        bildTyp = headerbb.get(2);
        switch (bildTyp) {
            case 2:
                compression = ProjectConstants.UNCOMPRESSED;
                break;
            case 10:
                compression = ProjectConstants.RLE;
                break;
            default:
                throw new UnknownCompressionException();
        }

    }

    @Override
    public void setCompressionOutputFile(String mode) throws UnknownCompressionException {

        switch (mode) {
            case ProjectConstants.UNCOMPRESSED:
                bildTyp = 2;
                break;
            case ProjectConstants.RLE:
                bildTyp = 10;
                break;
            default:
                throw new UnknownCompressionException();
        }
    }


    @Override
    protected void setHeightAndWidth() {
        setWidth(headerbb.getShort(12));
        height = headerbb.getShort(14);

    }
}
