package echangelocal.service.interfaces;

import echangelocal.dto.AnnonceDto;
import echangelocal.dto.AnnonceDetailDto;
import echangelocal.model.Annonce;
import echangelocal.model.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface AnnonceService {

    // Création et modification
    Annonce creerAnnonce(AnnonceDto annonceDto, Utilisateur createur) throws IOException;

    Annonce modifierAnnonce(Long annonceId, AnnonceDto annonceDto, Utilisateur utilisateur) throws IOException;

    void supprimerAnnonce(Long annonceId, Utilisateur utilisateur);

    // Consultation
    Optional<Annonce> trouverParId(Long id);

    Optional<AnnonceDetailDto> trouverDetailParId(Long id);

    List<Annonce> trouverAnnoncesParUtilisateur(Utilisateur utilisateur);

    // Recherche et filtrage
    Page<AnnonceDetailDto> trouverAnnoncesDisponibles(Pageable pageable);

    Page<AnnonceDetailDto> rechercherAnnonces(String recherche, String categorie, Pageable pageable);

    // Gestion des images
    void ajouterImagesAnnonce(Long annonceId, List<MultipartFile> fichiers, Utilisateur utilisateur) throws IOException;

    void supprimerImageAnnonce(Long annonceId, String nomImage, Utilisateur utilisateur) throws IOException;

    // Utilitaires
    boolean estProprietaireAnnonce(Long annonceId, Utilisateur utilisateur);

    List<String> getCategoriesPopulaires();
}