package echangelocal.service.interfaces;

import echangelocal.dto.InscriptionDto;
import echangelocal.dto.ProfilDto;
import echangelocal.model.Utilisateur;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface UtilisateurService {

    // Inscrit un nouvel utilisateur
    Utilisateur inscrireUtilisateur(InscriptionDto inscriptionDto);

    // Trouve un utilisateur par email
    Optional<Utilisateur> trouverParEmail(String email);

    // Trouve un utilisateur par ID
    Optional<Utilisateur> trouverParId(Long id);

    // Met à jour le profil d'un utilisateur
    Utilisateur mettreAJourProfil(Long id, ProfilDto profilDto);

    // Met à jour la photo de profil d'un utilisateur
    Utilisateur mettreAJourPhotoProfil(Long utilisateurId, MultipartFile fichier) throws IOException;

    // Vérifie si un mot de passe clair correspond au mot de passe encodé
    boolean verifierMotDePasse(String motDePasseClair, String motDePasseCrypte);

    // Vérifie le code de vérification du téléphone
    void verifierTelephone(Long utilisateurId, String codeVerification);

    // Génère un code de vérification pour le téléphone
    String genererCodeVerificationTelephone(Long utilisateurId);
}
