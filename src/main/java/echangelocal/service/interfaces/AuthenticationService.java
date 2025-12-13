package echangelocal.service.interfaces;

public interface AuthenticationService {

    // Authentifie un utilisateur à partir de son email et de son mot de passe
    boolean authentifierUtilisateur(String email, String motDePasse);

    // Déconnecte l'utilisateur actuellement authentifié
    void deconnecterUtilisateur();
}
