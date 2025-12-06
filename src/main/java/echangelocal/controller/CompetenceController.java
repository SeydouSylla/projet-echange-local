package echangelocal.controller;

import echangelocal.dto.CompetenceDto;
import echangelocal.dto.CompetenceDetailDto;
import echangelocal.model.Competence;
import echangelocal.model.Utilisateur;
import echangelocal.service.impl.AuthenticationServiceImpl;
import echangelocal.service.interfaces.CompetenceService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour la gestion des compétences.
 * Pattern MVC - Gestion des requêtes HTTP et navigation.
 */
@Controller
@RequestMapping("/competences")
public class CompetenceController {

    private final CompetenceService competenceService;
    private final AuthenticationServiceImpl authenticationService;

    //Injection de dépendances - Pattern DEPENDENCY INJECTION
    @Autowired
    public CompetenceController(CompetenceService competenceService,
                                AuthenticationServiceImpl authenticationService) {
        this.competenceService = competenceService;
        this.authenticationService = authenticationService;
    }

    // ========== AFFICHAGE DES FORMULAIRES ==========

    //Affiche le formulaire de création de compétence
    @GetMapping("/creer")
    public String afficherFormulaireCreation(Model model, HttpSession session) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        model.addAttribute("competenceDto", new CompetenceDto());
        preparerModelPourFormulaire(model);
        return "echange_competences/creer";
    }

    //Affiche le formulaire de modification de compétence
    @GetMapping("/modifier/{id}")
    public String afficherFormulaireModification(@PathVariable Long id, Model model, HttpSession session) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        Optional<Competence> competenceOpt = competenceService.trouverParId(id);
        if (competenceOpt.isEmpty() || !competenceService.estProprietaireCompetence(id, utilisateur)) {
            return "redirect:/competences/mes-competences";
        }

        Competence competence = competenceOpt.get();
        CompetenceDto competenceDto = new CompetenceDto();
        competenceDto.setId(competence.getId());
        competenceDto.setTitre(competence.getTitre());
        competenceDto.setDescription(competence.getDescription());
        competenceDto.setCategorie(competence.getCategorie());
        competenceDto.setDisponibilites(competence.getDisponibilites());
        competenceDto.setCommentaireEchange(competence.getCommentaireEchange());
        competenceDto.setDisponible(competence.isDisponible());

        model.addAttribute("competenceDto", competenceDto);
        preparerModelPourFormulaire(model);

        return "echange_competences/modifier";
    }

    // ========== TRAITEMENT DES FORMULAIRES ==========

    //Traite la création d'une compétence
    @PostMapping("/creer")
    public String creerCompetence(@Valid @ModelAttribute CompetenceDto competenceDto,
                                  BindingResult result,
                                  Model model,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        if (result.hasErrors()) {
            preparerModelPourFormulaire(model);
            return "echange_competences/creer";
        }

        try {
            Competence competence = competenceService.creerCompetence(competenceDto, utilisateur);
            redirectAttributes.addFlashAttribute("success",
                    "Compétence créée avec succès ! Elle est maintenant visible par la communauté.");
            return "redirect:/competences/mes-competences";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            preparerModelPourFormulaire(model);
            return "echange_competences/creer";
        }
    }

    //Traite la modification d'une compétence
    @PostMapping("/modifier/{id}")
    public String modifierCompetence(@PathVariable Long id,
                                     @Valid @ModelAttribute CompetenceDto competenceDto,
                                     BindingResult result,
                                     Model model,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        if (result.hasErrors()) {
            preparerModelPourFormulaire(model);
            return "echange_competences/modifier";
        }

        try {
            Competence competence = competenceService.modifierCompetence(id, competenceDto, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Compétence modifiée avec succès !");
            return "redirect:/competences/mes-competences";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            preparerModelPourFormulaire(model);
            return "echange_competences/modifier";
        }
    }

    // ========== CONSULTATION ET RECHERCHE ==========

    //Liste les compétences disponibles avec recherche et filtrage
    @GetMapping("/liste")
    public String listerCompetences(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "12") int size,
                                    @RequestParam(required = false) String recherche,
                                    @RequestParam(required = false) String categorie,
                                    @RequestParam(required = false) String localisation,
                                    Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        Page<CompetenceDetailDto> competencesPage = competenceService.rechercherCompetences(recherche, categorie, localisation, pageable);

        model.addAttribute("competencesPage", competencesPage);
        model.addAttribute("recherche", recherche);
        model.addAttribute("categorie", categorie);
        model.addAttribute("localisation", localisation);
        preparerModelPourListe(model);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", competencesPage.getTotalPages());

        return "echange_competences/liste";
    }

    //Affiche les détails d'une compétence
    @GetMapping("/{id}")
    public String afficherDetailCompetence(@PathVariable Long id, Model model) {
        Optional<CompetenceDetailDto> competenceOpt = competenceService.trouverDetailParId(id);

        if (competenceOpt.isEmpty()) {
            model.addAttribute("error", "Compétence non trouvée");
            return "redirect:/competences/liste";
        }

        model.addAttribute("competence", competenceOpt.get());

        // Vérifier si l'utilisateur connecté est le propriétaire
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur != null) {
            model.addAttribute("estProprietaire",
                    competenceService.estProprietaireCompetence(id, utilisateur));
        }

        return "echange_competences/detail";
    }

    // ========== GESTION DES COMPÉTENCES UTILISATEUR ==========

    //Affiche les compétences de l'utilisateur connecté
    @GetMapping("/mes-competences")
    public String afficherMesCompetences(Model model, HttpSession session) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        List<Competence> mesCompetences = competenceService.trouverCompetencesParUtilisateur(utilisateur);
        model.addAttribute("mesCompetences", mesCompetences);
        model.addAttribute("nombreCompetencesActives",
                competenceService.trouverCompetencesParUtilisateur(utilisateur)
                        .stream()
                        .filter(Competence::isDisponible)
                        .count());

        return "echange_competences/mes-competences";
    }

    //Supprime une compétence
    @PostMapping("/supprimer/{id}")
    public String supprimerCompetence(@PathVariable Long id,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        try {
            competenceService.supprimerCompetence(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Compétence supprimée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/competences/mes-competences";
    }

    // MÉTHODES UTILITAIRES PRIVÉES

    //Prépare le modèle pour les formulaires - Pattern HELPER METHOD
    private void preparerModelPourFormulaire(Model model) {
        model.addAttribute("categories", competenceService.getCategoriesPopulaires());
        model.addAttribute("disponibilitesPredefinies", competenceService.getDisponibilitesPredefinies());
    }

    //Prépare le modèle pour les listes - Pattern HELPER METHOD
    private void preparerModelPourListe(Model model) {
        model.addAttribute("categories", competenceService.getCategoriesPopulaires());
    }
}