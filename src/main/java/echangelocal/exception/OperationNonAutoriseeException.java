package echangelocal.exception;

public class OperationNonAutoriseeException extends RuntimeException {

    public OperationNonAutoriseeException(String message) {
        super(message);
    }

    public OperationNonAutoriseeException(String message, Throwable cause) {
        super(message, cause);
    }
}