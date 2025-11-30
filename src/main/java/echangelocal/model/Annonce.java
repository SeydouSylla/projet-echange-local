package echangelocal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "annonces")
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @NotBlank(message = "La catégorie est obligatoire")
    private String categorie;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le type d'annonce est obligatoire")
    private TypeAnnonce typeAnnonce;

    @ElementCollection
    @CollectionTable(name = "annonce_images", joinColumns = @JoinColumn(name = "annonce_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    private String commentaireEchange; // Ce que l'utilisateur souhaite en échange

    @Enumerated(EnumType.STRING)
    private StatutAnnonce statut = StatutAnnonce.DISPONIBLE;

    @NotNull(message = "La disponibilité est obligatoire")
    private boolean disponible = true;

    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur createur;

    // Constructeurs
    public Annonce() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    public Annonce(String titre, String description, String categorie, TypeAnnonce typeAnnonce, Utilisateur createur) {
        this();
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.typeAnnonce = typeAnnonce;
        this.createur = createur;
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

    public TypeAnnonce getTypeAnnonce() {
        return typeAnnonce;
    }

    public void setTypeAnnonce(TypeAnnonce typeAnnonce) {
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

    public StatutAnnonce getStatut() {
        return statut;
    }

    public void setStatut(StatutAnnonce statut) {
        this.statut = statut;
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

    public Utilisateur getCreateur() {
        return createur;
    }

    public void setCreateur(Utilisateur createur) {
        this.createur = createur;
    }

    // Méthodes utilitaires
    public void ajouterImage(String imageUrl) {
        this.images.add(imageUrl);
    }

    public void mettreAJourDateModification() {
        this.dateModification = LocalDateTime.now();
    }

    // Enum pour le type d'annonce
    public enum TypeAnnonce {
        PRET, DON, ECHANGE
    }

    // Enum pour le statut de l'annonce
    public enum StatutAnnonce {
        DISPONIBLE, EN_COURS, TERMINEE, SUPPRIMEE
    }
}