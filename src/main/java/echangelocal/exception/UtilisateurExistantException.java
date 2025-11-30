package echangelocal.exception;

public class UtilisateurExistantException extends RuntimeException {

    public UtilisateurExistantException(String message) {
        super(message);
    }

    public UtilisateurExistantException(String message, Throwable cause) {
        super(message, cause);
    }
}