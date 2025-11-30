package echangelocal.controller;

import echangelocal.model.Utilisateur;
import echangelocal.service.impl.AuthenticationServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccueilController {

    private final AuthenticationServiceImpl authenticationService;

    @Autowired
    public AccueilController(AuthenticationServiceImpl authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/accueil")
    public String accueil(HttpSession session, Model model) {
        Utilisateur utilisateur = authenticationService.getUtilisateurConnecte();

        if (utilisateur != null) {
            session.setAttribute("utilisateur", utilisateur);
            model.addAttribute("utilisateur", utilisateur);
        }

        return "accueil";
    }

    @GetMapping("/")
    public String racine() {
        return "redirect:/accueil";
    }
}