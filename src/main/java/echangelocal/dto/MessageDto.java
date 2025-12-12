package echangelocal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MessageDto {

    private Long id;

    @NotNull
    private Long demandeEchangeId;

    @NotBlank(message = "Le message ne peut pas être vide")
    private String contenu;

    // GETTERS ET SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDemandeEchangeId() {
        return demandeEchangeId;
    }

    public void setDemandeEchangeId(Long demandeEchangeId) {
        this.demandeEchangeId = demandeEchangeId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}