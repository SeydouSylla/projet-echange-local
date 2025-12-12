package echangelocal.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class DemandeEchangeDto {

    private Long id;

    private Long annonceId;

    private Long competenceId;

    @NotBlank(message = "La proposition d'échange est obligatoire")
    private String propositionEchange;

    @NotBlank(message = "Le message d'accompagnement est obligatoire")
    private String messageDemande;

    @NotNull(message = "La date d'échange est obligatoire")
    @Future(message = "La date d'échange doit être future")
    private LocalDateTime dateProposee;

    // GETTERS ET SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAnnonceId() {
        return annonceId;
    }

    public void setAnnonceId(Long annonceId) {
        this.annonceId = annonceId;
    }

    public Long getCompetenceId() {
        return competenceId;
    }

    public void setCompetenceId(Long competenceId) {
        this.competenceId = competenceId;
    }

    public String getPropositionEchange() {
        return propositionEchange;
    }

    public void setPropositionEchange(String propositionEchange) {
        this.propositionEchange = propositionEchange;
    }

    public String getMessageDemande() {
        return messageDemande;
    }

    public void setMessageDemande(String messageDemande) {
        this.messageDemande = messageDemande;
    }

    public LocalDateTime getDateProposee() {
        return dateProposee;
    }

    public void setDateProposee(LocalDateTime dateProposee) {
        this.dateProposee = dateProposee;
    }
}