package echangelocal.controller;

import echangelocal.dto.DemandeEchangeDto;
import echangelocal.dto.MessageDto;
import echangelocal.model.DemandeEchange;
import echangelocal.model.Utilisateur;
import echangelocal.service.impl.AuthenticationServiceImpl;
import echangelocal.service.interfaces.DemandeEchangeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/*
  Contrôleur pour la gestion des demandes d'échange
  Respecte le pattern MVC et la séparation des responsabilités
 */
@Controller
@RequestMapping("/echanges")
public class DemandeEchangeController {

    private final DemandeEchangeService demandeEchangeService;
    private final AuthenticationServiceImpl authenticationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public DemandeEchangeController(DemandeEchangeService demandeEchangeService,
                                    AuthenticationServiceImpl authenticationService,
                                    SimpMessagingTemplate messagingTemplate) {
        this.demandeEchangeService = demandeEchangeService;
        this.authenticationService = authenticationService;
        this.messagingTemplate = messagingTemplate;
    }

    // ============ MÉTHODES PRIVÉES UTILITAIRES ============

    private Utilisateur getUtilisateurAuthentifie() {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            throw new SecurityException("Utilisateur non authentifié");
        }
        return utilisateur;
    }

    private String gererErreurFormulaireDemande(DemandeEchangeDto demandeDto,
                                                Model model,
                                                Exception e) {
        String type = demandeDto.getAnnonceId() != null ? "annonce" : "competence";
        Long itemId = demandeDto.getAnnonceId() != null ?
                demandeDto.getAnnonceId() : demandeDto.getCompetenceId();

        model.addAttribute("error", e.getMessage());
        model.addAttribute("type", type);
        model.addAttribute("itemId", itemId);
        model.addAttribute("demandeDto", demandeDto);
        return "echanges/demande-form";
    }

    private Pageable preparerPageable(int page, String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && "asc".equals(sortParams[1]) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(direction, sortParams[0]);
        return PageRequest.of(page, 10, sortObj);
    }

    // ============ FORMULAIRES DE DEMANDE ============

    @GetMapping("/demander/annonce/{annonceId}")
    public String afficherFormulaireDemandeAnnonce(@PathVariable Long annonceId,
                                                   Model model,
                                                   HttpSession session) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            DemandeEchangeDto demandeDto = new DemandeEchangeDto();
            demandeDto.setAnnonceId(annonceId);

            model.addAttribute("demandeDto", demandeDto);
            model.addAttribute("type", "annonce");
            model.addAttribute("itemId", annonceId);
            return "echanges/demande-form";
        } catch (SecurityException e) {
            return "redirect:/connexion";
        }
    }

    @GetMapping("/demander/competence/{competenceId}")
    public String afficherFormulaireDemandeCompetence(@PathVariable Long competenceId,
                                                      Model model,
                                                      HttpSession session) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            DemandeEchangeDto demandeDto = new DemandeEchangeDto();
            demandeDto.setCompetenceId(competenceId);

            model.addAttribute("demandeDto", demandeDto);
            model.addAttribute("type", "competence");
            model.addAttribute("itemId", competenceId);
            return "echanges/demande-form";
        } catch (SecurityException e) {
            return "redirect:/connexion";
        }
    }

    // ============ TRAITEMENT DES DEMANDES ============

    @PostMapping("/demander")
    public String creerDemande(@Valid @ModelAttribute DemandeEchangeDto demandeDto,
                               BindingResult result,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            if (result.hasErrors()) {
                String type = demandeDto.getAnnonceId() != null ? "annonce" : "competence";
                Long itemId = demandeDto.getAnnonceId() != null ?
                        demandeDto.getAnnonceId() : demandeDto.getCompetenceId();

                model.addAttribute("type", type);
                model.addAttribute("itemId", itemId);
                model.addAttribute("demandeDto", demandeDto);
                return "echanges/demande-form";
            }

            DemandeEchange demande = demandeEchangeService.creerDemandeEchange(demandeDto, utilisateur);
            redirectAttributes.addFlashAttribute("success",
                    "Votre demande d'échange a été envoyée avec succès !");

            return "redirect:/echanges/demandes-envoyees";

        } catch (SecurityException e) {
            return "redirect:/connexion";
        } catch (Exception e) {
            return gererErreurFormulaireDemande(demandeDto, model, e);
        }
    }

    // ============ GESTION DES DEMANDES ============

    @GetMapping("/mes-demandes")
    public String afficherMesDemandes(Model model, HttpSession session) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            List<DemandeEchange> demandesEnvoyees = demandeEchangeService.trouverDemandesEnvoyees(utilisateur);
            List<DemandeEchange> demandesRecues = demandeEchangeService.trouverDemandesRecues(utilisateur);
            long demandesEnAttente = demandeEchangeService.compterDemandesEnAttente(utilisateur);
            List<DemandeEchange> echangesActifs = demandeEchangeService.trouverEchangesActifs(utilisateur);
            List<DemandeEchange> echangesTermines = demandeEchangeService.trouverEchangesTermines(utilisateur);

            model.addAttribute("demandesEnvoyees", demandesEnvoyees);
            model.addAttribute("demandesRecues", demandesRecues);
            model.addAttribute("demandesEnAttente", demandesEnAttente);
            model.addAttribute("echangesActifs", echangesActifs);
            model.addAttribute("echangesTermines", echangesTermines);
            model.addAttribute("utilisateur", utilisateur);

            return "echanges/mes-demandes";
        } catch (SecurityException e) {
            return "redirect:/connexion";
        }
    }

    // ============ PAGES PAGINÉES AVEC FILTRES ============

    @GetMapping("/demandes-recues")
    public String afficherDemandesRecues(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(required = false) String statut,
                                         @RequestParam(defaultValue = "dateCreation,desc") String sort,
                                         Model model,
                                         HttpSession session) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            Pageable pageable = preparerPageable(page, sort);
            Page<DemandeEchange> demandesPage = getDemandesPageRecues(utilisateur, statut, pageable);

            model.addAttribute("demandesPage", demandesPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", demandesPage.getTotalPages());
            model.addAttribute("sort", sort);
            model.addAttribute("statut", statut);

            model.addAttribute("totalDemandes", demandesPage.getTotalElements());
            model.addAttribute("demandesEnAttente",
                    demandeEchangeService.compterDemandesRecuesParStatut(utilisateur, DemandeEchange.StatutDemande.EN_ATTENTE));
            model.addAttribute("demandesAcceptees",
                    demandeEchangeService.compterDemandesRecuesParStatut(utilisateur, DemandeEchange.StatutDemande.ACCEPTEE));
            model.addAttribute("demandesRefusees",
                    demandeEchangeService.compterDemandesRecuesParStatut(utilisateur, DemandeEchange.StatutDemande.REFUSEE));

            return "echanges/demandes-recues";
        } catch (SecurityException e) {
            return "redirect:/connexion";
        }
    }

    @GetMapping("/demandes-envoyees")
    public String afficherDemandesEnvoyees(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(required = false) String statut,
                                           @RequestParam(defaultValue = "dateCreation,desc") String sort,
                                           Model model,
                                           HttpSession session) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            Pageable pageable = preparerPageable(page, sort);
            Page<DemandeEchange> demandesPage = getDemandesPageEnvoyees(utilisateur, statut, pageable);

            model.addAttribute("demandesPage", demandesPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", demandesPage.getTotalPages());
            model.addAttribute("sort", sort);
            model.addAttribute("statut", statut);

            model.addAttribute("totalDemandes", demandesPage.getTotalElements());
            model.addAttribute("demandesEnAttente",
                    demandeEchangeService.compterDemandesEnvoyeesParStatut(utilisateur, DemandeEchange.StatutDemande.EN_ATTENTE));
            model.addAttribute("demandesAcceptees",
                    demandeEchangeService.compterDemandesEnvoyeesParStatut(utilisateur, DemandeEchange.StatutDemande.ACCEPTEE));
            model.addAttribute("demandesRefusees",
                    demandeEchangeService.compterDemandesEnvoyeesParStatut(utilisateur, DemandeEchange.StatutDemande.REFUSEE));
            model.addAttribute("demandesAnnulees",
                    demandeEchangeService.compterDemandesEnvoyeesParStatut(utilisateur, DemandeEchange.StatutDemande.ANNULEE));

            return "echanges/demandes-envoyees";
        } catch (SecurityException e) {
            return "redirect:/connexion";
        }
    }

    @GetMapping("/historique")
    public String afficherHistorique(@RequestParam(defaultValue = "0") int page,
                                     Model model,
                                     HttpSession session) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "dateCreation"));
            Page<DemandeEchange> echangesTerminesPage = demandeEchangeService.trouverEchangesTerminesPage(utilisateur, pageable);

            model.addAttribute("echangesPage", echangesTerminesPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", echangesTerminesPage.getTotalPages());
            model.addAttribute("utilisateur", utilisateur);

            return "echanges/historique";
        } catch (SecurityException e) {
            return "redirect:/connexion";
        }
    }

    // ============ MÉTHODES PRIVÉES POUR LE FILTRAGE ============

    private Page<DemandeEchange> getDemandesPageRecues(Utilisateur utilisateur,
                                                       String statut,
                                                       Pageable pageable) {
        if (statut != null && !statut.isEmpty()) {
            try {
                DemandeEchange.StatutDemande statutDemande = DemandeEchange.StatutDemande.valueOf(statut);
                return demandeEchangeService.trouverDemandesRecuesParStatut(utilisateur, statutDemande, pageable);
            } catch (IllegalArgumentException e) {
                return demandeEchangeService.trouverDemandesRecuesPage(utilisateur, pageable);
            }
        }
        return demandeEchangeService.trouverDemandesRecuesPage(utilisateur, pageable);
    }

    private Page<DemandeEchange> getDemandesPageEnvoyees(Utilisateur utilisateur,
                                                         String statut,
                                                         Pageable pageable) {
        if (statut != null && !statut.isEmpty()) {
            try {
                DemandeEchange.StatutDemande statutDemande = DemandeEchange.StatutDemande.valueOf(statut);
                return demandeEchangeService.trouverDemandesEnvoyeesParStatut(utilisateur, statutDemande, pageable);
            } catch (IllegalArgumentException e) {
                return demandeEchangeService.trouverDemandesEnvoyeesPage(utilisateur, pageable);
            }
        }
        return demandeEchangeService.trouverDemandesEnvoyeesPage(utilisateur, pageable);
    }

    // ============ ACTIONS SUR LES DEMANDES ============

    @PostMapping("/accepter/{id}")
    public String accepterDemande(@PathVariable Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();
            demandeEchangeService.accepterDemande(id, utilisateur);
            redirectAttributes.addFlashAttribute("success",
                    "Demande acceptée ! L'échange est maintenant actif.");
        } catch (SecurityException e) {
            return "redirect:/connexion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/echanges/demandes-recues";
    }

    @PostMapping("/refuser/{id}")
    public String refuserDemande(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();
            demandeEchangeService.refuserDemande(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Demande refusée.");
        } catch (SecurityException e) {
            return "redirect:/connexion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/echanges/demandes-recues";
    }

    @PostMapping("/annuler/{id}")
    public String annulerDemande(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();
            demandeEchangeService.annulerDemande(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Demande annulée.");
        } catch (SecurityException e) {
            return "redirect:/connexion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/echanges/demandes-envoyees";
    }

    // ============ MESSAGERIE ============

    @GetMapping("/messagerie/{id}")
    public String afficherMessagerie(@PathVariable Long id,
                                     Model model,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            // Utiliser trouverParIdAvecMessages
            DemandeEchange demande = demandeEchangeService.trouverParIdAvecMessages(id)
                    .orElseThrow(() -> new IllegalArgumentException("Échange non trouvé"));

            // Vérifier que l'utilisateur peut consulter cet historique
            if (!demandeEchangeService.peutConsulterHistorique(id, utilisateur)) {
                redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/echanges/mes-demandes";
            }

            // Déterminer si la messagerie est en lecture seule
            boolean lectureSeule = demande.getStatut() == DemandeEchange.StatutDemande.TERMINEE;

            MessageDto messageDto = new MessageDto();
            messageDto.setDemandeEchangeId(id);

            model.addAttribute("demande", demande);
            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("messageDto", messageDto);
            model.addAttribute("lectureSeule", lectureSeule);

            return "echanges/messagerie";
        } catch (SecurityException e) {
            return "redirect:/connexion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du chargement de la messagerie: " + e.getMessage());
            return "redirect:/echanges/mes-demandes";
        }
    }

    @PostMapping("/envoyer-message")
    public String envoyerMessage(@Valid @ModelAttribute MessageDto messageDto,
                                 BindingResult result,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = getUtilisateurAuthentifie();

            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Le message ne peut pas être vide");
                return "redirect:/echanges/messagerie/" + messageDto.getDemandeEchangeId();
            }

            demandeEchangeService.envoyerMessage(messageDto, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Message envoyé !");

        } catch (SecurityException e) {
            return "redirect:/connexion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/echanges/messagerie/" + messageDto.getDemandeEchangeId();
    }

    // ============ WEBSOCKET (Ceci est optionnel) ============

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageDto messageDto) {
        // Pour les notifications en temps réel
    }
}