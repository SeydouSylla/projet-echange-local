package echangelocal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une compétence proposée par un utilisateur.
 * Pattern ENTITY - Représentation objet de la table en base de données.
 */
@Entity
@Table(name = "competences")
public class Competence {

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

    @ElementCollection
    @CollectionTable(name = "competence_disponibilites", joinColumns = @JoinColumn(name = "competence_id"))
    @Column(name = "disponibilite")
    private List<String> disponibilites = new ArrayList<>();

    private String commentaireEchange; // Ce que l'utilisateur souhaite en échange

    @Enumerated(EnumType.STRING)
    private StatutCompetence statut = StatutCompetence.DISPONIBLE;

    @NotNull(message = "La disponibilité est obligatoire")
    private boolean disponible = true;

    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur createur;

    // CONSTRUCTEURS
    public Competence() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    public Competence(String titre, String description, String categorie, Utilisateur createur) {
        this();
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.createur = createur;
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

    public StatutCompetence getStatut() {
        return statut;
    }

    public void setStatut(StatutCompetence statut) {
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

    // MÉTHODES UTILITAIRES
    public void ajouterDisponibilite(String disponibilite) {
        this.disponibilites.add(disponibilite);
    }

    public void mettreAJourDateModification() {
        this.dateModification = LocalDateTime.now();
    }

    // ENUM POUR LE STATUT DE LA COMPÉTENCE
    public enum StatutCompetence {
        DISPONIBLE, EN_COURS, TERMINEE, SUPPRIMEE
    }
}