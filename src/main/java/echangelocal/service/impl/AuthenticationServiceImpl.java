package echangelocal.service.impl;

import echangelocal.exception.UtilisateurNonTrouveException;
import echangelocal.model.Utilisateur;
import echangelocal.repository.UtilisateurRepository;
import echangelocal.service.interfaces.AuthenticationService;
import echangelocal.service.interfaces.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UtilisateurService utilisateurService;
    private final UtilisateurRepository utilisateurRepository;

    // Constructeur pour injecter les services nécessaires à l’authentification
    @Autowired
    public AuthenticationServiceImpl(UtilisateurService utilisateurService,
                                     UtilisateurRepository utilisateurRepository) {
        this.utilisateurService = utilisateurService;
        this.utilisateurRepository = utilisateurRepository;
    }

    // Vérifie les identifiants de l'utilisateur à partir de son email et mot de passe
    @Override
    public boolean authentifierUtilisateur(String email, String motDePasse) {
        Optional<Utilisateur> utilisateurOpt = utilisateurService.trouverParEmail(email);

        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            return utilisateurService.verifierMotDePasse(motDePasse, utilisateur.getMotDePasse());
        }

        return false;
    }

    // Déconnecte l'utilisateur en supprimant le contexte de sécurité
    @Override
    public void deconnecterUtilisateur() {
        SecurityContextHolder.clearContext();
    }

    // Récupère l'utilisateur actuellement authentifié depuis le contexte de sécurité
    public Utilisateur getUtilisateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        String email = authentication.getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UtilisateurNonTrouveException("Utilisateur non trouvé"));
    }
}
