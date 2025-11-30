package echangelocal.controller;

import echangelocal.dto.InscriptionDto;
import echangelocal.dto.ProfilDto;
import echangelocal.model.Utilisateur;
import echangelocal.service.interfaces.UtilisateurService;
import echangelocal.service.impl.AuthenticationServiceImpl;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class AuthentificationController {

    private final UtilisateurService utilisateurService;
    private final AuthenticationServiceImpl authenticationService;

    @Autowired
    public AuthentificationController(UtilisateurService utilisateurService,
                                      AuthenticationServiceImpl authenticationService) {
        this.utilisateurService = utilisateurService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/inscription")
    public String afficherFormulaireInscription(Model model) {
        model.addAttribute("inscriptionDto", new InscriptionDto());
        return "authentification/inscription";
    }

    @PostMapping("/inscription")
    public String traiterInscription(@Valid @ModelAttribute InscriptionDto inscriptionDto,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "authentification/inscription";
        }

        try {
            utilisateurService.inscrireUtilisateur(inscriptionDto);
            redirectAttributes.addFlashAttribute("success", "Inscription réussie! Vous pouvez maintenant vous connecter.");
            return "redirect:/connexion";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "authentification/inscription";
        }
    }

    @GetMapping("/connexion")
    public String afficherFormulaireConnexion(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "deconnecte", required = false) String deconnecte,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe incorrect. Veuillez réessayer.");
        }

        if (deconnecte != null) {
            model.addAttribute("success", "Vous avez été déconnecté avec succès.");
        }

        return "authentification/connexion";
    }

    @GetMapping("/deconnexion")
    public String deconnexion(HttpSession session) {
        authenticationService.deconnecterUtilisateur();
        session.invalidate();
        return "redirect:/?deconnecte=true";
    }

    @GetMapping("/profil")
    public String afficherProfil(HttpSession session, Model model) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();

        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        session.setAttribute("utilisateur", utilisateur);
        model.addAttribute("utilisateur", utilisateur);

        // Préparer le DTO pour le formulaire
        ProfilDto profilDto = new ProfilDto();
        profilDto.setPrenom(utilisateur.getPrenom());
        profilDto.setNom(utilisateur.getNom());
        profilDto.setLocalisation(utilisateur.getLocalisation());
        profilDto.setBiographie(utilisateur.getBiographie());
        profilDto.setTelephone(utilisateur.getTelephone());

        model.addAttribute("profilDto", profilDto);

        return "authentification/profil";
    }

    @PostMapping("/profil")
    public String mettreAJourProfil(@Valid @ModelAttribute ProfilDto profilDto,
                                    BindingResult result,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        Utilisateur utilisateurSession = authenticationService.getUtilisateurConnecte();
        if (utilisateurSession == null) {
            return "redirect:/connexion";
        }

        if (result.hasErrors()) {
            return "authentification/profil";
        }

        try {
            Utilisateur utilisateurMaj = utilisateurService.mettreAJourProfil(utilisateurSession.getId(), profilDto);
            rafraichirUtilisateurSession(session);
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profil";
    }

    @PostMapping("/profil/photo")
    public String mettreAJourPhotoProfil(@RequestParam("photo") MultipartFile fichier,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {

        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        if (fichier.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une photo");
            return "redirect:/profil";
        }

        try {
            Utilisateur utilisateurMaj = utilisateurService.mettreAJourPhotoProfil(utilisateur.getId(), fichier);
            session.setAttribute("utilisateur", utilisateurMaj);
            redirectAttributes.addFlashAttribute("success", "Photo de profil mise à jour avec succès!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du téléchargement de la photo");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profil";
    }

    @PostMapping("/profil/verifier-telephone")
    public String verifierTelephone(@RequestParam String codeVerification,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        try {
            utilisateurService.verifierTelephone(utilisateur.getId(), codeVerification);
            rafraichirUtilisateurSession(session);
            redirectAttributes.addFlashAttribute("success", "Numéro de téléphone vérifié avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profil";
    }

    @PostMapping("/profil/demander-verification")
    public String demanderVerificationTelephone(HttpSession session,
                                                RedirectAttributes redirectAttributes) {

        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();
        if (utilisateur == null) {
            return "redirect:/connexion";
        }

        try {
            utilisateurService.genererCodeVerificationTelephone(utilisateur.getId());
            redirectAttributes.addFlashAttribute("info", "Code de vérification envoyé. Vérifiez votre console.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profil";
    }

    private void rafraichirUtilisateurSession(HttpSession session) {
        Utilisateur utilisateurConnecte = authenticationService.getUtilisateurConnecte();
        if (utilisateurConnecte != null) {
            // Recharger l'utilisateur depuis la base pour avoir les données à jour
            Utilisateur utilisateurMaj = utilisateurService.trouverParId(utilisateurConnecte.getId())
                    .orElse(utilisateurConnecte);
            session.setAttribute("utilisateur", utilisateurMaj);
        }
    }
}