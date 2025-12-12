package echangelocal.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UtilisateurExistantException.class)
    public String handleUtilisateurExistantException(UtilisateurExistantException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "authentification/inscription";
    }

    @ExceptionHandler(UtilisateurNonTrouveException.class)
    public String handleUtilisateurNonTrouveException(UtilisateurNonTrouveException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/connexion";
    }

    @ExceptionHandler(TelephoneInvalideException.class)
    public String handleTelephoneInvalideException(TelephoneInvalideException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/profil";
    }

    @ExceptionHandler(AnnonceNonTrouveeException.class)
    public String handleAnnonceNonTrouveeException(AnnonceNonTrouveeException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/annonces/liste";
    }

    @ExceptionHandler(OperationNonAutoriseeException.class)
    public String handleOperationNonAutoriseeException(OperationNonAutoriseeException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/annonces/liste";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "La taille du fichier dépasse la limite autorisée.");
        return "redirect:/annonces/creer";
    }

    //Pour les competences
    @ExceptionHandler(CompetenceNonTrouveeException.class)
    public String handleCompetenceNonTrouveeException(CompetenceNonTrouveeException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/competences/liste";
    }

    //Pour les echanges
    @ExceptionHandler(DemandeEchangeNonTrouveeException.class)
    public String handleDemandeEchangeNonTrouveeException(DemandeEchangeNonTrouveeException e,
                                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/echanges/mes-demandes";
    }

    @ExceptionHandler(DemandeEchangeException.class)
    public String handleDemandeEchangeException(DemandeEchangeException e,
                                                RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/echanges/mes-demandes";
    }
}