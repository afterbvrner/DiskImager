package exception;

public class NotEnoughSpaceException extends RuntimeException {
    public NotEnoughSpaceException() {
        super("All clusters are filled");
    }

    public NotEnoughSpaceException(String message) {
        super(message);
    }
}
