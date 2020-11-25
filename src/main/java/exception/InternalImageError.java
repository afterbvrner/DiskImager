package exception;

public class InternalImageError extends RuntimeException {
    public InternalImageError() {
    }

    public InternalImageError(String message) {
        super(message);
    }
}
