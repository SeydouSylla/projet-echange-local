package echangelocal.service.interfaces;

import echangelocal.dto.InscriptionDto;
import echangelocal.dto.ProfilDto;
import echangelocal.model.Utilisateur;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface UtilisateurService {

    Utilisateur inscrireUtilisateur(InscriptionDto inscriptionDto);
    Optional<Utilisateur> trouverParEmail(String email);
    Optional<Utilisateur> trouverParId(Long id);
    Utilisateur mettreAJourProfil(Long id, ProfilDto profilDto);
    Utilisateur mettreAJourPhotoProfil(Long utilisateurId, MultipartFile fichier) throws IOException;
    boolean verifierMotDePasse(String motDePasseClair, String motDePasseCrypte);
    void verifierTelephone(Long utilisateurId, String codeVerification);
    String genererCodeVerificationTelephone(Long utilisateurId);
}