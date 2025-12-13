package echangelocal.controller;

import echangelocal.dto.AvisDto;
import echangelocal.model.Avis;
import echangelocal.model.DemandeEchange;
import echangelocal.model.Utilisateur;
import echangelocal.service.impl.AuthenticationServiceImpl;
import echangelocal.service.interfaces.AvisService;
import echangelocal.service.interfaces.DemandeEchangeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
  Contrôleur pour la gestion des avis et de la réputation
  Respecte le pattern MVC et la séparation des responsabilités
 */
@Controller
@RequestMapping("/avis")
public class AvisController {

    private final AvisService avisService;
    private final DemandeEchangeService demandeEchangeService;
    private final AuthenticationServiceImpl authenticationService;

    @Autowired
    public AvisController(AvisService avisService,
                          DemandeEchangeService demandeEchangeService,
                          AuthenticationServiceImpl authenticationService) {
        this.avisService = avisService;
        this.demandeEchangeService = demandeEchangeService;
        this.authenticationService = authenticationService;
    }

    // ============ MÉTHODES PRIVÉES UTILITAIRES ============

    private Utilisateur getUtilisateurAuthentifie() {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            throw new SecurityException("Utilisateur non authentifié");
        }
        return utilisateur;
    }

    // ============ FORMULAIRES D'AVIS ============

    //Affiche le formulaire pour laisser un avis
    @GetMapping("/laisser/{demandeEchangeId}")
    public String afficherFormulaireAvis(@PathVariable Long demandeEchangeId,
                                         Model model,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            if (!avisService.peutLaisserAvis(demandeEchangeId, utilisateur)) {
                redirectAttributes.addFlashAttribute("error",
                        "Vous ne pouvez pas laisser d'avis pour cet échange");
                return "redirect:/echanges/mes-demandes";
            }

            DemandeEchange demande = demandeEchangeService.trouverParId(demandeEchangeId)
                    .orElseThrow(() -> new IllegalArgumentException("Échange non trouvé"));

            AvisDto avisDto = new AvisDto();
            avisDto.setDemandeEchangeId(demandeEchangeId);

            model.addAttribute("avisDto", avisDto);
            model.addAttribute("demande", demande);
            model.addAttribute("utilisateur", utilisateur);

            return "avis/formulaire-avis";
        } catch (SecurityException e) {
            return "redirect:/connexion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/echanges/mes-demandes";
        }
    }

    //Traite la soumission du formulaire d'avis
    @PostMapping("/laisser")
    public String creerAvis(@Valid @ModelAttribute AvisDto avisDto,
                            BindingResult result,
                            Model model,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            if (result.hasErrors()) {
                DemandeEchange demande = demandeEchangeService.trouverParId(avisDto.getDemandeEchangeId())
                        .orElseThrow(() -> new IllegalArgumentException("Échange non trouvé"));
                model.addAttribute("demande", demande);
                model.addAttribute("utilisateur", utilisateur);
                return "avis/formulaire-avis";
            }

            avisService.creerAvis(avisDto, utilisateur);
            redirectAttributes.addFlashAttribute("success",
                    "Votre avis a été publié avec succès ! Merci pour votre contribution.");

            return "redirect:/avis/mes-avis";

        } catch (SecurityException e) {
            return "redirect:/connexion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/echanges/mes-demandes";
        }
    }

    // ============ CONSULTATION DES AVIS ============

    //Affiche les avis reçus par l'utilisateur connecté
    @GetMapping("/mes-avis")
    public String afficherMesAvis(@RequestParam(defaultValue = "0") int page,
                                  Model model,
                                  HttpSession session) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            Pageable pageable = PageRequest.of(page, 10);
            Page<Avis> avisRecusPage = avisService.trouverAvisRecusPagines(utilisateur, pageable);

            List<Avis> avisDonnes = avisService.trouverAvisDonnes(utilisateur);

            Map<String, Object> statistiques = avisService.obtenirStatistiques(utilisateur);

            model.addAttribute("avisRecusPage", avisRecusPage);
            model.addAttribute("avisDonnes", avisDonnes);
            model.addAttribute("statistiques", statistiques);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", avisRecusPage.getTotalPages());

            return "avis/mes-avis";
        } catch (SecurityException e) {
            return "redirect:/connexion";
        }
    }

    // Affiche tous les avis publics (accessible à tous)
    @GetMapping("/tous")
    public String afficherTousLesAvis(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(required = false) Integer noteMin,
                                      @RequestParam(required = false) String recherche,
                                      Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        Page<Avis> avisPage;

        // Filtrer selon les critères
        if ((noteMin != null && noteMin >= 1 && noteMin <= 5) ||
                (recherche != null && !recherche.trim().isEmpty())) {
            avisPage = avisService.trouverAvisPublicsAvecFiltres(noteMin, recherche, pageable);
        } else {
            avisPage = avisService.trouverTousLesAvisPublics(pageable);
        }

        model.addAttribute("avisPage", avisPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", avisPage.getTotalPages());
        model.addAttribute("noteMin", noteMin);
        model.addAttribute("recherche", recherche);

        return "avis/tous-les-avis";
    }

    // Affiche les avis d'un utilisateur spécifique (profil public)
    @GetMapping("/utilisateur/{utilisateurId}")
    public String afficherAvisUtilisateur(@PathVariable Long utilisateurId,
                                          @RequestParam(defaultValue = "0") int page,
                                          Model model) {
        // On redirige vers mes-avis
        return "redirect:/avis/mes-avis";
    }

    // ============ MODIFICATION ET SUPPRESSION ============

    //Affiche le formulaire de modification d'un avis
    @GetMapping("/modifier/{avisId}")
    public String afficherFormulaireModification(@PathVariable Long avisId,
                                                 Model model,
                                                 HttpSession session,
                                                 RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            Avis avis = avisService.trouverParId(avisId)
                    .orElseThrow(() -> new IllegalArgumentException("Avis non trouvé"));

            if (!avis.getEvaluateur().equals(utilisateur)) {
                redirectAttributes.addFlashAttribute("error", "Non autorisé");
                return "redirect:/avis/mes-avis";
            }

            if (!avis.peutEtreModifie()) {
                redirectAttributes.addFlashAttribute("error", "Délai de modification dépassé (24h)");
                return "redirect:/avis/mes-avis";
            }

            AvisDto avisDto = new AvisDto();
            avisDto.setId(avis.getId());
            avisDto.setDemandeEchangeId(avis.getDemandeEchange().getId());
            avisDto.setNote(avis.getNote());
            avisDto.setCommentaire(avis.getCommentaire());

            model.addAttribute("avisDto", avisDto);
            model.addAttribute("demande", avis.getDemandeEchange());
            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("modification", true);

            return "avis/formulaire-avis";
        } catch (SecurityException e) {
            return "redirect:/connexion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/avis/mes-avis";
        }
    }

    // Traite la modification d'un avis
    @PostMapping("/modifier/{avisId}")
    public String modifierAvis(@PathVariable Long avisId,
                               @Valid @ModelAttribute AvisDto avisDto,
                               BindingResult result,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            if (result.hasErrors()) {
                Avis avis = avisService.trouverParId(avisId)
                        .orElseThrow(() -> new IllegalArgumentException("Avis non trouvé"));
                model.addAttribute("demande", avis.getDemandeEchange());
                model.addAttribute("utilisateur", utilisateur);
                model.addAttribute("modification", true);
                return "avis/formulaire-avis";
            }

            avisService.modifierAvis(avisId, avisDto, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Votre avis a été modifié avec succès");

            return "redirect:/avis/mes-avis";

        } catch (SecurityException e) {
            return "redirect:/connexion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/avis/mes-avis";
        }
    }

    // Supprime (masque) un avis
    @PostMapping("/supprimer/{avisId}")
    public String supprimerAvis(@PathVariable Long avisId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            avisService.supprimerAvis(avisId, utilisateur);
            redirectAttributes.addFlashAttribute("success", "L'avis a été supprimé");

            return "redirect:/avis/mes-avis";

        } catch (SecurityException e) {
            return "redirect:/connexion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/avis/mes-avis";
        }
    }
}