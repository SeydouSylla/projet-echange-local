package echangelocal.dto;

import echangelocal.model.Annonce;

import java.time.LocalDateTime;
import java.util.List;

public class AnnonceDetailDto {

    private Long id;
    private String titre;
    private String description;
    private String categorie;
    private Annonce.TypeAnnonce typeAnnonce;
    private List<String> images;
    private String commentaireEchange;
    private boolean disponible;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String createurNom;
    private String createurLocalisation;
    private String createurPhotoProfil;
    private boolean createurTelephoneVerifie;

    // Constructeurs
    public AnnonceDetailDto() {
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public Annonce.TypeAnnonce getTypeAnnonce() {
        return typeAnnonce;
    }

    public void setTypeAnnonce(Annonce.TypeAnnonce typeAnnonce) {
        this.typeAnnonce = typeAnnonce;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getCommentaireEchange() {
        return commentaireEchange;
    }

    public void setCommentaireEchange(String commentaireEchange) {
        this.commentaireEchange = commentaireEchange;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public String getCreateurNom() {
        return createurNom;
    }

    public void setCreateurNom(String createurNom) {
        this.createurNom = createurNom;
    }

    public String getCreateurLocalisation() {
        return createurLocalisation;
    }

    public void setCreateurLocalisation(String createurLocalisation) {
        this.createurLocalisation = createurLocalisation;
    }

    public String getCreateurPhotoProfil() {
        return createurPhotoProfil;
    }

    public void setCreateurPhotoProfil(String createurPhotoProfil) {
        this.createurPhotoProfil = createurPhotoProfil;
    }

    public boolean isCreateurTelephoneVerifie() {
        return createurTelephoneVerifie;
    }

    public void setCreateurTelephoneVerifie(boolean createurTelephoneVerifie) {
        this.createurTelephoneVerifie = createurTelephoneVerifie;
    }
}