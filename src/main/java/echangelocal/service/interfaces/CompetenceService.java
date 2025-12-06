package echangelocal.service.interfaces;

import echangelocal.dto.CompetenceDto;
import echangelocal.dto.CompetenceDetailDto;
import echangelocal.model.Competence;
import echangelocal.model.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interface de service pour la gestion des compétences.
 * Pattern STRATEGY - Définition du contrat métier.
 */
public interface CompetenceService {

    // CRÉATION ET MODIFICATION
    Competence creerCompetence(CompetenceDto competenceDto, Utilisateur createur);

    Competence modifierCompetence(Long competenceId, CompetenceDto competenceDto, Utilisateur utilisateur);

    void supprimerCompetence(Long competenceId, Utilisateur utilisateur);

    // CONSULTATION
    Optional<Competence> trouverParId(Long id);

    Optional<CompetenceDetailDto> trouverDetailParId(Long id);

    List<Competence> trouverCompetencesParUtilisateur(Utilisateur utilisateur);

    // RECHERCHE ET FILTRAGE
    Page<CompetenceDetailDto> trouverCompetencesDisponibles(Pageable pageable);

    Page<CompetenceDetailDto> rechercherCompetences(String recherche, String categorie, String localisation, Pageable pageable);

    // UTILITAIRES
    boolean estProprietaireCompetence(Long competenceId, Utilisateur utilisateur);

    List<String> getCategoriesPopulaires();

    List<String> getDisponibilitesPredefinies();
}