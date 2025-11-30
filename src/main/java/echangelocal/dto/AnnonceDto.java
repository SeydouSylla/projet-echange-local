package echangelocal.dto;

import echangelocal.model.Annonce;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class AnnonceDto {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @NotBlank(message = "La catégorie est obligatoire")
    private String categorie;

    @NotNull(message = "Le type d'annonce est obligatoire")
    private Annonce.TypeAnnonce typeAnnonce;

    private List<MultipartFile> fichiersImages = new ArrayList<>();

    private String commentaireEchange;

    private boolean disponible = true;

    // Constructeurs
    public AnnonceDto() {
    }

    public AnnonceDto(String titre, String description, String categorie,
                      Annonce.TypeAnnonce typeAnnonce, String commentaireEchange) {
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.typeAnnonce = typeAnnonce;
        this.commentaireEchange = commentaireEchange;
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

    public List<MultipartFile> getFichiersImages() {
        return fichiersImages;
    }

    public void setFichiersImages(List<MultipartFile> fichiersImages) {
        this.fichiersImages = fichiersImages;
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