package echangelocal.service.interfaces;

public interface AuthenticationService {

    boolean authentifierUtilisateur(String email, String motDePasse);
    void deconnecterUtilisateur();
}