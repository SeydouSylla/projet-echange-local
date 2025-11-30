package echangelocal.exception;

public class TelephoneInvalideException extends RuntimeException {

    public TelephoneInvalideException(String message) {
        super(message);
    }

    public TelephoneInvalideException(String message, Throwable cause) {
        super(message, cause);
    }
}