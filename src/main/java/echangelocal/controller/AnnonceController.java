package echangelocal.controller;

import echangelocal.dto.AnnonceDto;
import echangelocal.dto.AnnonceDetailDto;
import echangelocal.model.Annonce;
import echangelocal.model.Utilisateur;
import echangelocal.service.impl.AuthenticationServiceImpl;
import echangelocal.service.interfaces.AnnonceService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/annonces")
public class AnnonceController {

    private final AnnonceService annonceService;
    private final AuthenticationServiceImpl authenticationService;

    @Autowired
    public AnnonceController(AnnonceService annonceService,
                             AuthenticationServiceImpl authenticationService) {
        this.annonceService = annonceService;
        this.authenticationService = authenticationService;
    }

    // Afficher le formulaire de création d'annonce
    @GetMapping("/creer")
    public String afficherFormulaireCreation(Model model, HttpSession session) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        model.addAttribute("annonceDto", new AnnonceDto());
        model.addAttribute("categories", annonceService.getCategoriesPopulaires());
        model.addAttribute("typesAnnonce", Annonce.TypeAnnonce.values());
        return "annonces/creer";
    }

    // Traiter la création d'annonce
    @PostMapping("/creer")
    public String creerAnnonce(@Valid @ModelAttribute AnnonceDto annonceDto,
                               BindingResult result,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", annonceService.getCategoriesPopulaires());
            model.addAttribute("typesAnnonce", Annonce.TypeAnnonce.values());
            return "annonces/creer";
        }

        try {
            Annonce annonce = annonceService.creerAnnonce(annonceDto, utilisateur);
            redirectAttributes.addFlashAttribute("success",
                    "Annonce créée avec succès ! Elle est maintenant visible par la communauté.");
            return "redirect:/annonces/mes-annonces";
        } catch (IOException e) {
            model.addAttribute("error", "Erreur lors du téléchargement des images");
            model.addAttribute("categories", annonceService.getCategoriesPopulaires());
            model.addAttribute("typesAnnonce", Annonce.TypeAnnonce.values());
            return "annonces/creer";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", annonceService.getCategoriesPopulaires());
            model.addAttribute("typesAnnonce", Annonce.TypeAnnonce.values());
            return "annonces/creer";
        }
    }

    // Lister les annonces disponibles avec pagination
    @GetMapping("/liste")
    public String listerAnnonces(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size,
                                 @RequestParam(required = false) String recherche,
                                 @RequestParam(required = false) String categorie,
                                 Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        Page<AnnonceDetailDto> annoncesPage = annonceService.rechercherAnnonces(recherche, categorie, pageable);

        model.addAttribute("annoncesPage", annoncesPage);
        model.addAttribute("recherche", recherche);
        model.addAttribute("categorie", categorie);
        model.addAttribute("categories", annonceService.getCategoriesPopulaires());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", annoncesPage.getTotalPages());

        return "annonces/liste";
    }

    // Afficher les détails d'une annonce
    @GetMapping("/{id}")
    public String afficherDetailAnnonce(@PathVariable Long id, Model model) {
        Optional<AnnonceDetailDto> annonceOpt = annonceService.trouverDetailParId(id);

        if (annonceOpt.isEmpty()) {
            model.addAttribute("error", "Annonce non trouvée");
            return "redirect:/annonces/liste";
        }

        model.addAttribute("annonce", annonceOpt.get());

        // Vérifier si l'utilisateur connecté est le propriétaire
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur != null) {
            model.addAttribute("estProprietaire",
                    annonceService.estProprietaireAnnonce(id, utilisateur));
        }

        return "annonces/detail";
    }

    // Afficher les annonces de l'utilisateur connecté
    @GetMapping("/mes-annonces")
    public String afficherMesAnnonces(Model model, HttpSession session) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        List<Annonce> mesAnnonces = annonceService.trouverAnnoncesParUtilisateur(utilisateur);
        model.addAttribute("mesAnnonces", mesAnnonces);
        model.addAttribute("nombreAnnoncesActives",
                annonceService.trouverAnnoncesParUtilisateur(utilisateur)
                        .stream()
                        .filter(Annonce::isDisponible)
                        .count());

        return "annonces/mes-annonces";
    }

    // Afficher le formulaire de modification
    @GetMapping("/modifier/{id}")
    public String afficherFormulaireModification(@PathVariable Long id, Model model, HttpSession session) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        Optional<Annonce> annonceOpt = annonceService.trouverParId(id);
        if (annonceOpt.isEmpty() || !annonceService.estProprietaireAnnonce(id, utilisateur)) {
            return "redirect:/annonces/mes-annonces";
        }

        Annonce annonce = annonceOpt.get();
        AnnonceDto annonceDto = new AnnonceDto();
        annonceDto.setId(annonce.getId());
        annonceDto.setTitre(annonce.getTitre());
        annonceDto.setDescription(annonce.getDescription());
        annonceDto.setCategorie(annonce.getCategorie());
        annonceDto.setTypeAnnonce(annonce.getTypeAnnonce());
        annonceDto.setCommentaireEchange(annonce.getCommentaireEchange());
        annonceDto.setDisponible(annonce.isDisponible());

        model.addAttribute("annonceDto", annonceDto);
        model.addAttribute("categories", annonceService.getCategoriesPopulaires());
        model.addAttribute("typesAnnonce", Annonce.TypeAnnonce.values());
        model.addAttribute("imagesExistantes", annonce.getImages());

        return "annonces/modifier";
    }

    // Traiter la modification d'annonce
    @PostMapping("/modifier/{id}")
    public String modifierAnnonce(@PathVariable Long id,
                                  @Valid @ModelAttribute AnnonceDto annonceDto,
                                  BindingResult result,
                                  Model model,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", annonceService.getCategoriesPopulaires());
            model.addAttribute("typesAnnonce", Annonce.TypeAnnonce.values());
            return "annonces/modifier";
        }

        try {
            Annonce annonce = annonceService.modifierAnnonce(id, annonceDto, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Annonce modifiée avec succès !");
            return "redirect:/annonces/mes-annonces";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", annonceService.getCategoriesPopulaires());
            model.addAttribute("typesAnnonce", Annonce.TypeAnnonce.values());
            return "annonces/modifier";
        }
    }

    // Supprimer une annonce
    @PostMapping("/supprimer/{id}")
    public String supprimerAnnonce(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        try {
            annonceService.supprimerAnnonce(id, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Annonce supprimée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/annonces/mes-annonces";
    }

    // Ajouter des images à une annonce existante
    @PostMapping("/{id}/ajouter-images")
    public String ajouterImages(@PathVariable Long id,
                                @RequestParam("fichiers") List<MultipartFile> fichiers,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        try {
            annonceService.ajouterImagesAnnonce(id, fichiers, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Images ajoutées avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout des images");
        }

        return "redirect:/annonces/modifier/" + id;
    }

    // Supprimer une image d'une annonce
    @PostMapping("/{id}/supprimer-image")
    public String supprimerImage(@PathVariable Long id,
                                 @RequestParam String nomImage,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        try {
            annonceService.supprimerImageAnnonce(id, nomImage, utilisateur);
            redirectAttributes.addFlashAttribute("success", "Image supprimée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression de l'image");
        }

        return "redirect:/annonces/modifier/" + id;
    }
}