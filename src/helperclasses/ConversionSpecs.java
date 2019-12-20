package src.helperclasses;

import src.filetypes.FileTypeSuper;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

/**
 * Diese Klasse enthält die für die Konvertierung relevanten Informationen.
 *
 *
 *
 */
public class ConversionSpecs {

    private FileTypeSuper inputformat;
    private String inputFormatString;
    private FileTypeSuper outputformat;
    private String outputFormatString;
    private Path inputPath;
    private Path outputPath;
    private String operation; // Entweder der Kompressionstyp, oder die N für
    // die Base-N-Kodierung
    private byte[] inputDatasegments;
    private ByteArrayOutputStream ByteArrayOutputStream;
    private String alphabetBaseN;

    public void convertTo(FileTypeSuper input,
                          FileTypeSuper output) {

        System.out.println("HELLO :D:D:D:D");
    }

    public String getAlphabetBaseN() {
        return alphabetBaseN;
    }

    public void setAlphabetBaseN(String alphabetBaseN) {
        this.alphabetBaseN = alphabetBaseN;
    }

    public byte[] getInputDatasegments() {
        return inputDatasegments;
    }

    public void setInputDatasegments(byte[] inputDatasegments) {
        this.inputDatasegments = inputDatasegments;
    }

    public String getInputFormatString() {
        return inputFormatString;
    }

    public void setInputFormatString(String inputFormatString) {
        this.inputFormatString = inputFormatString;
    }

    // Getter- und Setter-Methoden
    public FileTypeSuper getInputformat() {
        return inputformat;
    }

    public void setInputformat(FileTypeSuper inputformat) {
        this.inputformat = inputformat;
    }

    public Path getInputPath() {
        return inputPath;
    }

    public void setInputPath(Path inputPath) {
        this.inputPath = inputPath;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;

    }

    public String getOutputFormatString() {
        return outputFormatString;
    }

    public void setOutputFormatString(String outputFormatString) {
        this.outputFormatString = outputFormatString;
    }

    public FileTypeSuper getOutputformat() {
        return outputformat;
    }

    public void setOutputformat(FileTypeSuper outputformat) {
        this.outputformat = outputformat;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
    }

    public ByteArrayOutputStream getbOAS() {
        return ByteArrayOutputStream;
    }

    /**
     * Berechnet die Offsets für die Konvertierung der Farbkodierungen
     *
     * @param convspec Objekt, welches die Informationen über die durchzuführende Operation enthält.
     * @return integer Array, welches die benötigten Offsets in der Reihenfolge Farbkodierung
     * der AUSGABEkonvertierung enthält.
     */
    public int[] offsetCalculator(ConversionSpecs convspec) {
        FileTypeSuper inputformat = convspec.getInputformat();
        FileTypeSuper outputformat = convspec.getOutputformat();

        int[] offsets = new int[convspec
                .getInputformat().getColourscheme().length];

        for (int i = 0; i < offsets.length; i++) {
            offsets[i] = outputformat.getColourSchemeMap()
                    .get(inputformat.getColourscheme()[i])
                    - inputformat.getColourSchemeMap()
                    .get(inputformat.getColourscheme()[i]);
        }
        return offsets;
    }

    public void setByteArrayOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
        this.ByteArrayOutputStream = byteArrayOutputStream;
    }
}