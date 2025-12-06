package echangelocal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO (Data Transfer Object) pour la création et modification de compétences.
 * Pattern DTO - Transfert sécurisé des données entre les couches.
 */
public class CompetenceDto {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @NotBlank(message = "La catégorie est obligatoire")
    private String categorie;

    @NotNull(message = "Au moins une disponibilité est requise")
    private List<String> disponibilites = new ArrayList<>();

    private String commentaireEchange;

    private boolean disponible = true;

    // CONSTRUCTEURS
    public CompetenceDto() {
    }

    public CompetenceDto(String titre, String description, String categorie,
                         List<String> disponibilites, String commentaireEchange) {
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.disponibilites = disponibilites;
        this.commentaireEchange = commentaireEchange;
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
}