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
        return "inscription";
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

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "La taille du fichier dépasse la limite autorisée.");
        return "redirect:/profil";
    }
}