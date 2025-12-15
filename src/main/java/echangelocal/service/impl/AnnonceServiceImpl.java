package echangelocal.service.impl;

import echangelocal.dto.AnnonceDto;
import echangelocal.dto.AnnonceDetailDto;
import echangelocal.exception.AnnonceNonTrouveeException;
import echangelocal.exception.OperationNonAutoriseeException;
import echangelocal.model.Annonce;
import echangelocal.model.Utilisateur;
import echangelocal.repository.AnnonceRepository;
import echangelocal.service.interfaces.AnnonceService;
import echangelocal.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnnonceServiceImpl implements AnnonceService {

    // Catégories prédéfinies pour l'application
    private static final List<String> CATEGORIES_PREDEFINIES = Arrays.asList(
            "Outillage", "Électroménager", "Électronique", "Jardinage", "Sport",
            "Loisirs", "Véhicules", "Immobilier", "Mode", "Livres", "Musique",
            "Jeux vidéo", "Bricolage", "Décoration", "Autre"
    );
    private final AnnonceRepository annonceRepository;
    private final String uploadDir; // Chemin d'upload injecté

    @Autowired
    public AnnonceServiceImpl(AnnonceRepository annonceRepository,
                              @Value("${app.upload.dir}") String uploadDir) {
        this.annonceRepository = annonceRepository;
        this.uploadDir = uploadDir; // Enregistrement du chemin d'upload
    }

    @Override
    public Annonce creerAnnonce(AnnonceDto annonceDto, Utilisateur createur) throws IOException {
        // Validation des données
        validerAnnonceDto(annonceDto);

        // Création de l'annonce
        Annonce annonce = new Annonce();
        mapperAnnonceDtoVersAnnonce(annonceDto, annonce);
        annonce.setCreateur(createur);

        // Sauvegarde de l'annonce
        Annonce annonceSauvegardee = annonceRepository.save(annonce);

        // Traitement des images si présentes
        if (annonceDto.getFichiersImages() != null && !annonceDto.getFichiersImages().isEmpty()) {
            traiterImagesAnnonce(annonceSauvegardee, annonceDto.getFichiersImages());
        }

        return annonceSauvegardee;
    }

    @Override
    public Annonce modifierAnnonce(Long annonceId, AnnonceDto annonceDto, Utilisateur utilisateur) throws IOException {
        // Vérification de l'existence et des droits
        Annonce annonce = trouverAnnonceAvecVerification(annonceId, utilisateur);

        // Validation des données
        validerAnnonceDto(annonceDto);

        // Mise à jour de l'annonce
        mapperAnnonceDtoVersAnnonce(annonceDto, annonce);
        annonce.mettreAJourDateModification();

        // Traitement des nouvelles images
        if (annonceDto.getFichiersImages() != null && !annonceDto.getFichiersImages().isEmpty()) {
            traiterImagesAnnonce(annonce, annonceDto.getFichiersImages());
        }

        return annonceRepository.save(annonce);
    }

    @Override
    public void supprimerAnnonce(Long annonceId, Utilisateur utilisateur) {
        Annonce annonce = trouverAnnonceAvecVerification(annonceId, utilisateur);

        // Suppression des images associées
        if (annonce.getImages() != null && !annonce.getImages().isEmpty()) {
            supprimerImagesAnnonce(annonce);
        }

        annonceRepository.delete(annonce);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Annonce> trouverParId(Long id) {
        return annonceRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AnnonceDetailDto> trouverDetailParId(Long id) {
        return annonceRepository.findByIdWithCreateur(id)
                .map(this::convertirVersDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Annonce> trouverAnnoncesParUtilisateur(Utilisateur utilisateur) {
        return annonceRepository.findByCreateurOrderByDateCreationDesc(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnnonceDetailDto> trouverAnnoncesDisponibles(Pageable pageable) {
        return annonceRepository.findByDisponibleTrueOrderByDateCreationDesc(pageable)
                .map(this::convertirVersDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnnonceDetailDto> rechercherAnnonces(String recherche, String categorie, Pageable pageable) {
        Page<Annonce> annonces;

        if (recherche != null && !recherche.trim().isEmpty() && categorie != null && !categorie.trim().isEmpty()) {
            // Recherche combinée (catégorie + mots-clés)
            annonces = annonceRepository.rechercherParCategorieEtMotsCles(categorie, recherche.trim(), pageable);
        } else if (recherche != null && !recherche.trim().isEmpty()) {
            // Recherche par mots-clés seulement
            annonces = annonceRepository.rechercherParMotsCles(recherche.trim(), pageable);
        } else if (categorie != null && !categorie.trim().isEmpty()) {
            // Recherche par catégorie seulement
            annonces = annonceRepository.findByCategorieAndDisponibleTrueOrderByDateCreationDesc(categorie, pageable);
        } else {
            // Toutes les annonces disponibles
            annonces = annonceRepository.findByDisponibleTrueOrderByDateCreationDesc(pageable);
        }

        return annonces.map(this::convertirVersDetailDto);
    }

    @Override
    public void ajouterImagesAnnonce(Long annonceId, List<MultipartFile> fichiers, Utilisateur utilisateur) throws IOException {
        Annonce annonce = trouverAnnonceAvecVerification(annonceId, utilisateur);
        traiterImagesAnnonce(annonce, fichiers);
        annonceRepository.save(annonce);
    }

    @Override
    public void supprimerImageAnnonce(Long annonceId, String nomImage, Utilisateur utilisateur) throws IOException {
        Annonce annonce = trouverAnnonceAvecVerification(annonceId, utilisateur);

        if (annonce.getImages().remove(nomImage)) {
            // Supprimer le fichier physique
            Path cheminComplet = Paths.get(getRepertoireUploadAnnonces(), nomImage);
            FileStorageUtil.supprimerFichier(cheminComplet);
            annonceRepository.save(annonce);
        }
    }

    @Override
    public boolean estProprietaireAnnonce(Long annonceId, Utilisateur utilisateur) {
        return annonceRepository.findById(annonceId)
                .map(annonce -> annonce.getCreateur().getId().equals(utilisateur.getId()))
                .orElse(false);
    }

    @Override
    public List<String> getCategoriesPopulaires() {
        return new ArrayList<>(CATEGORIES_PREDEFINIES);
    }

    // Méthodes privées utilitaires

    private Annonce trouverAnnonceAvecVerification(Long annonceId, Utilisateur utilisateur) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new AnnonceNonTrouveeException("Annonce non trouvée"));

        if (!annonce.getCreateur().getId().equals(utilisateur.getId())) {
            throw new OperationNonAutoriseeException("Vous n'êtes pas autorisé à modifier cette annonce");
        }

        return annonce;
    }

    private void validerAnnonceDto(AnnonceDto annonceDto) {
        if (annonceDto == null) {
            throw new IllegalArgumentException("L'annonce ne peut pas être nulle");
        }
        // Validation supplémentaire peut être ajoutée ici
    }

    private void mapperAnnonceDtoVersAnnonce(AnnonceDto dto, Annonce annonce) {
        annonce.setTitre(dto.getTitre());
        annonce.setDescription(dto.getDescription());
        annonce.setCategorie(dto.getCategorie());
        annonce.setTypeAnnonce(dto.getTypeAnnonce());
        annonce.setCommentaireEchange(dto.getCommentaireEchange());
        annonce.setDisponible(dto.isDisponible());
    }

    private void traiterImagesAnnonce(Annonce annonce, List<MultipartFile> fichiers) throws IOException {
        String repertoireUpload = getRepertoireUploadAnnonces();

        for (MultipartFile fichier : fichiers) {
            if (fichier != null && !fichier.isEmpty()) {
                String nomFichier = FileStorageUtil.sauvegarderFichier(fichier, repertoireUpload);
                annonce.ajouterImage(nomFichier);
            }
        }
    }

    private void supprimerImagesAnnonce(Annonce annonce) {
        String repertoireUpload = getRepertoireUploadAnnonces();

        for (String nomImage : annonce.getImages()) {
            try {
                Path cheminComplet = Paths.get(repertoireUpload, nomImage);
                FileStorageUtil.supprimerFichier(cheminComplet);
            } catch (IOException e) {
                // Logger l'erreur mais continuer la suppression
                System.err.println("Erreur lors de la suppression de l'image: " + nomImage);
            }
        }
    }

    private AnnonceDetailDto convertirVersDetailDto(Annonce annonce) {
        AnnonceDetailDto dto = new AnnonceDetailDto();
        dto.setId(annonce.getId());
        dto.setTitre(annonce.getTitre());
        dto.setDescription(annonce.getDescription());
        dto.setCategorie(annonce.getCategorie());
        dto.setTypeAnnonce(annonce.getTypeAnnonce());
        dto.setImages(annonce.getImages());
        dto.setCommentaireEchange(annonce.getCommentaireEchange());
        dto.setDisponible(annonce.isDisponible());
        dto.setDateCreation(annonce.getDateCreation());
        dto.setDateModification(annonce.getDateModification());

        // Informations du créateur
        Utilisateur createur = annonce.getCreateur();
        dto.setCreateurNom(createur.getNomComplet());
        dto.setCreateurLocalisation(createur.getLocalisation());
        dto.setCreateurPhotoProfil(createur.getPhotoProfil());
        dto.setCreateurTelephoneVerifie(createur.isTelephoneVerifie());

        return dto;
    }

    private String getRepertoireUploadAnnonces() {
        // Créer le chemin complet pour les annonces dans le répertoire d'upload
        return Paths.get(uploadDir, "annonces").toString();
    }
}