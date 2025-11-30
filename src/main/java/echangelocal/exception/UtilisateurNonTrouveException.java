package echangelocal.exception;

public class UtilisateurNonTrouveException extends RuntimeException {

    public UtilisateurNonTrouveException(String message) {
        super(message);
    }

    public UtilisateurNonTrouveException(String message, Throwable cause) {
        super(message, cause);
    }
}