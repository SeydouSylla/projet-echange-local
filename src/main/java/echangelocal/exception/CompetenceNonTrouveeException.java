package echangelocal.exception;

/**
  Exception levée lorsqu'une compétence n'est pas trouvée.
  Gestion métier des erreurs.
 */
public class CompetenceNonTrouveeException extends RuntimeException {

    public CompetenceNonTrouveeException(String message) {
        super(message);
    }

    public CompetenceNonTrouveeException(String message, Throwable cause) {
        super(message, cause);
    }
}