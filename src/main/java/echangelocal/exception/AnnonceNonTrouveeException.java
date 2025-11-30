package echangelocal.exception;

public class AnnonceNonTrouveeException extends RuntimeException {

    public AnnonceNonTrouveeException(String message) {
        super(message);
    }

    public AnnonceNonTrouveeException(String message, Throwable cause) {
        super(message, cause);
    }
}