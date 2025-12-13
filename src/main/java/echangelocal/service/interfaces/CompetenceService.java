package echangelocal.service.interfaces;

import echangelocal.dto.CompetenceDto;
import echangelocal.dto.CompetenceDetailDto;
import echangelocal.model.Competence;
import echangelocal.model.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

// Interface de service pour la gestion des compétences
// Pattern STRATEGY - Définition du contrat métier
public interface CompetenceService {

    // ============ CRÉATION ET MODIFICATION ============

    // Crée une nouvelle compétence pour un utilisateur
    Competence creerCompetence(CompetenceDto competenceDto, Utilisateur createur);

    // Modifie une compétence existante appartenant à l'utilisateur
    Competence modifierCompetence(Long competenceId, CompetenceDto competenceDto, Utilisateur utilisateur);

    // Supprime une compétence appartenant à l'utilisateur
    void supprimerCompetence(Long competenceId, Utilisateur utilisateur);

    // ============ CONSULTATION ============

    // Trouve une compétence par son identifiant
    Optional<Competence> trouverParId(Long id);

    // Trouve les détails complets d'une compétence par son identifiant
    Optional<CompetenceDetailDto> trouverDetailParId(Long id);

    // Trouve toutes les compétences créées par un utilisateur
    List<Competence> trouverCompetencesParUtilisateur(Utilisateur utilisateur);

    // ============ RECHERCHE ET FILTRAGE ============

    // Trouve toutes les compétences disponibles avec pagination
    Page<CompetenceDetailDto> trouverCompetencesDisponibles(Pageable pageable);

    // Recherche des compétences selon des critères de filtrage
    Page<CompetenceDetailDto> rechercherCompetences(String recherche, String categorie, String localisation, Pageable pageable);

    // ============ MÉTHODES UTILITAIRES ============

    // Vérifie si l'utilisateur est propriétaire de la compétence
    boolean estProprietaireCompetence(Long competenceId, Utilisateur utilisateur);

    // Récupère les catégories de compétences les plus populaires
    List<String> getCategoriesPopulaires();

    // Récupère les disponibilités prédéfinies pour les compétences
    List<String> getDisponibilitesPredefinies();
}
