package echangelocal.service.impl;

import echangelocal.dto.CompetenceDto;
import echangelocal.dto.CompetenceDetailDto;
import echangelocal.exception.CompetenceNonTrouveeException;
import echangelocal.exception.OperationNonAutoriseeException;
import echangelocal.model.Competence;
import echangelocal.model.Utilisateur;
import echangelocal.repository.CompetenceRepository;
import echangelocal.service.interfaces.CompetenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
  Implémentation du service de gestion des compétences.
  Pattern SERVICE LAYER - Logique métier centralisée.
 */
@Service
@Transactional
public class CompetenceServiceImpl implements CompetenceService {

    // Catégories prédéfinies pour les compétences - Pattern CONSTANTS
    private static final List<String> CATEGORIES_PREDEFINIES = Arrays.asList(
            "Bricolage", "Jardinage", "Cuisine", "Cours particuliers", "Informatique",
            "Mécanique", "Coiffure", "Beauté", "Sport", "Musique", "Art",
            "Langues", "Photographie", "Événementiel", "Transport", "Autre"
    );
    // Disponibilités prédéfinies - Pattern CONSTANTS
    private static final List<String> DISPONIBILITES_PREDEFINIES = Arrays.asList(
            "Week-ends", "Soirées", "Journées", "En semaine", "Ponctuel",
            "En ligne", "À domicile", "Flexible"
    );
    private final CompetenceRepository competenceRepository;

    //Constructeur avec injection de dépendances - Pattern DEPENDENCY INJECTION
    @Autowired
    public CompetenceServiceImpl(CompetenceRepository competenceRepository) {
        this.competenceRepository = competenceRepository;
    }

    //Crée une nouvelle compétence - Pattern TEMPLATE METHOD
    @Override
    public Competence creerCompetence(CompetenceDto competenceDto, Utilisateur createur) {
        // VALIDATION DES DONNÉES
        validerCompetenceDto(competenceDto);

        // MAPPING DTO -> ENTITÉ
        Competence competence = new Competence();
        mapperCompetenceDtoVersCompetence(competenceDto, competence);
        competence.setCreateur(createur);

        // SAUVEGARDE
        return competenceRepository.save(competence);
    }

    //Modifie une compétence existante
    @Override
    public Competence modifierCompetence(Long competenceId, CompetenceDto competenceDto, Utilisateur utilisateur) {
        // VÉRIFICATION DES DROITS
        Competence competence = trouverCompetenceAvecVerification(competenceId, utilisateur);

        // VALIDATION
        validerCompetenceDto(competenceDto);

        // MISE À JOUR
        mapperCompetenceDtoVersCompetence(competenceDto, competence);
        competence.mettreAJourDateModification();

        return competenceRepository.save(competence);
    }

    //Supprime une compétence
    @Override
    public void supprimerCompetence(Long competenceId, Utilisateur utilisateur) {
        Competence competence = trouverCompetenceAvecVerification(competenceId, utilisateur);
        competenceRepository.delete(competence);
    }

    //Trouve une compétence par son ID
    @Override
    @Transactional(readOnly = true)
    public Optional<Competence> trouverParId(Long id) {
        return competenceRepository.findById(id);
    }

    //Trouve les détails d'une compétence avec informations du créateur
    @Override
    @Transactional(readOnly = true)
    public Optional<CompetenceDetailDto> trouverDetailParId(Long id) {
        return competenceRepository.findByIdWithCreateur(id)
                .map(this::convertirVersDetailDto);
    }

    //Trouve toutes les compétences d'un utilisateur
    @Override
    @Transactional(readOnly = true)
    public List<Competence> trouverCompetencesParUtilisateur(Utilisateur utilisateur) {
        return competenceRepository.findByCreateurOrderByDateCreationDesc(utilisateur);
    }

    //Trouve les compétences disponibles avec pagination
    @Override
    @Transactional(readOnly = true)
    public Page<CompetenceDetailDto> trouverCompetencesDisponibles(Pageable pageable) {
        return competenceRepository.findByDisponibleTrueOrderByDateCreationDesc(pageable)
                .map(this::convertirVersDetailDto);
    }

    //Recherche avancée des compétences - Pattern STRATEGY pour différentes recherches
    @Override
    @Transactional(readOnly = true)
    public Page<CompetenceDetailDto> rechercherCompetences(String recherche, String categorie, String localisation, Pageable pageable) {
        Page<Competence> competences;

        // Recherche avancée avec tous les critères
        if ((recherche != null && !recherche.trim().isEmpty()) ||
                (categorie != null && !categorie.trim().isEmpty()) ||
                (localisation != null && !localisation.trim().isEmpty())) {

            competences = competenceRepository.rechercherAvancee(
                    recherche != null ? recherche.trim() : null,
                    categorie != null && !categorie.trim().isEmpty() ? categorie.trim() : null,
                    localisation != null && !localisation.trim().isEmpty() ? localisation.trim() : null,
                    pageable
            );
        } else {
            // Toutes les compétences disponibles
            competences = competenceRepository.findByDisponibleTrueOrderByDateCreationDesc(pageable);
        }

        return competences.map(this::convertirVersDetailDto);
    }

    //Vérifie si l'utilisateur est propriétaire de la compétence
    @Override
    @Transactional(readOnly = true)
    public boolean estProprietaireCompetence(Long competenceId, Utilisateur utilisateur) {
        return competenceRepository.findById(competenceId)
                .map(competence -> competence.getCreateur().getId().equals(utilisateur.getId()))
                .orElse(false);
    }

    //Retourne les catégories populaires - Pattern CONSTANTS
    @Override
    public List<String> getCategoriesPopulaires() {
        return new ArrayList<>(CATEGORIES_PREDEFINIES);
    }

    //Retourne les disponibilités prédéfinies - Pattern CONSTANTS
    @Override
    public List<String> getDisponibilitesPredefinies() {
        return new ArrayList<>(DISPONIBILITES_PREDEFINIES);
    }

    // ========== MÉTHODES PRIVÉES UTILITAIRES ==========

    //Valide les données du DTO - Pattern VALIDATION
    private void validerCompetenceDto(CompetenceDto competenceDto) {
        if (competenceDto == null) {
            throw new IllegalArgumentException("La compétence ne peut pas être nulle");
        }
        if (competenceDto.getDisponibilites() == null || competenceDto.getDisponibilites().isEmpty()) {
            throw new IllegalArgumentException("Au moins une disponibilité est requise");
        }
    }

    //Trouve une compétence avec vérification des droits
    private Competence trouverCompetenceAvecVerification(Long competenceId, Utilisateur utilisateur) {
        Competence competence = competenceRepository.findById(competenceId)
                .orElseThrow(() -> new CompetenceNonTrouveeException("Compétence non trouvée"));

        if (!competence.getCreateur().getId().equals(utilisateur.getId())) {
            throw new OperationNonAutoriseeException("Vous n'êtes pas autorisé à modifier cette compétence");
        }

        return competence;
    }

    //Mappe le DTO vers l'entité - Pattern MAPPER
    private void mapperCompetenceDtoVersCompetence(CompetenceDto dto, Competence competence) {
        competence.setTitre(dto.getTitre());
        competence.setDescription(dto.getDescription());
        competence.setCategorie(dto.getCategorie());
        competence.setDisponibilites(new ArrayList<>(dto.getDisponibilites()));
        competence.setCommentaireEchange(dto.getCommentaireEchange());
        competence.setDisponible(dto.isDisponible());
    }

    //Convertit l'entité en DTO détaillé - Pattern MAPPER
    private CompetenceDetailDto convertirVersDetailDto(Competence competence) {
        CompetenceDetailDto dto = new CompetenceDetailDto();
        dto.setId(competence.getId());
        dto.setTitre(competence.getTitre());
        dto.setDescription(competence.getDescription());
        dto.setCategorie(competence.getCategorie());
        dto.setDisponibilites(competence.getDisponibilites());
        dto.setCommentaireEchange(competence.getCommentaireEchange());
        dto.setDisponible(competence.isDisponible());
        dto.setDateCreation(competence.getDateCreation());
        dto.setDateModification(competence.getDateModification());

        // Informations du créateur
        Utilisateur createur = competence.getCreateur();
        dto.setCreateurNom(createur.getNomComplet());
        dto.setCreateurLocalisation(createur.getLocalisation());
        dto.setCreateurPhotoProfil(createur.getPhotoProfil());
        dto.setCreateurTelephoneVerifie(createur.isTelephoneVerifie());
        dto.setCreateurBiographie(createur.getBiographie());

        return dto;
    }
}