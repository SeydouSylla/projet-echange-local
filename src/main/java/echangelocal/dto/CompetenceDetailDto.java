package echangelocal.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO spécialisé pour l'affichage détaillé des compétences.
 * Pattern DTO - Agrégation de données pour l'affichage.
 */
public class CompetenceDetailDto {

    private Long id;
    private String titre;
    private String description;
    private String categorie;
    private List<String> disponibilites;
    private String commentaireEchange;
    private boolean disponible;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String createurNom;
    private String createurLocalisation;
    private String createurPhotoProfil;
    private boolean createurTelephoneVerifie;
    private String createurBiographie;

    // CONSTRUCTEURS
    public CompetenceDetailDto() {
    }

    // GETTERS ET SETTERS
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

    public List<String> getDisponibilites() {
        return disponibilites;
    }

    public void setDisponibilites(List<String> disponibilites) {
        this.disponibilites = disponibilites;
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

    public String getCreateurBiographie() {
        return createurBiographie;
    }

    public void setCreateurBiographie(String createurBiographie) {
        this.createurBiographie = createurBiographie;
    }
}