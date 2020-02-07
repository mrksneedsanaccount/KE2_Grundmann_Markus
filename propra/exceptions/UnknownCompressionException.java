package propra.exceptions;

public class UnknownCompressionException extends Exception {

    public UnknownCompressionException(String message) {
        super(message);
    }

    public UnknownCompressionException() {
        super("Either the source file's compression is unknown, or an error occurred in assigning the target file's compression.");
    }
}
