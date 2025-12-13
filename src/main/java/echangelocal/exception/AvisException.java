package echangelocal.exception;

/*
  Exception métier pour la gestion des avis
 */
public class AvisException extends RuntimeException {

    public AvisException(String message) {
        super(message);
    }

    public AvisException(String message, Throwable cause) {
        super(message, cause);
    }
}