package echangelocal.dto;

import jakarta.validation.constraints.*;

/*
  DTO pour le transfert de données d'avis
*/
public class AvisDto {

    private Long id;

    @NotNull(message = "L'ID de la demande d'échange est obligatoire")
    private Long demandeEchangeId;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimum est 1 étoile")
    @Max(value = 5, message = "La note maximum est 5 étoiles")
    private Integer note;

    @NotBlank(message = "Le commentaire est obligatoire")
    @Size(min = 10, max = 1000, message = "Le commentaire doit contenir entre 10 et 1000 caractères")
    private String commentaire;

    // Informations supplémentaires pour l'affichage
    private String nomEvalue;
    private String prenomEvalue;
    private String nomEvaluateur;
    private String prenomEvaluateur;
    private String dateCreation;

    // Constructeurs
    public AvisDto() {
    }

    public AvisDto(Long demandeEchangeId, Integer note, String commentaire) {
        this.demandeEchangeId = demandeEchangeId;
        this.note = note;
        this.commentaire = commentaire;
    }

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

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getNomEvalue() {
        return nomEvalue;
    }

    public void setNomEvalue(String nomEvalue) {
        this.nomEvalue = nomEvalue;
    }

    public String getPrenomEvalue() {
        return prenomEvalue;
    }

    public void setPrenomEvalue(String prenomEvalue) {
        this.prenomEvalue = prenomEvalue;
    }

    public String getNomEvaluateur() {
        return nomEvaluateur;
    }

    public void setNomEvaluateur(String nomEvaluateur) {
        this.nomEvaluateur = nomEvaluateur;
    }

    public String getPrenomEvaluateur() {
        return prenomEvaluateur;
    }

    public void setPrenomEvaluateur(String prenomEvaluateur) {
        this.prenomEvaluateur = prenomEvaluateur;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }
}