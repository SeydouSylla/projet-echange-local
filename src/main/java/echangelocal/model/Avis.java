package echangelocal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/*
  Entité représentant un avis laissé après un échange
  Respecte le principe Single Responsibility : gère uniquement les données d'un avis
 */
@Entity
@Table(name = "avis", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"demande_echange_id", "evaluateur_id"})
})
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_echange_id", nullable = false)
    private DemandeEchange demandeEchange;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluateur_id", nullable = false)
    private Utilisateur evaluateur; // Celui qui donne l'avis

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evalue_id", nullable = false)
    private Utilisateur evalue; // Celui qui reçoit l'avis

    @NotNull
    @Min(value = 1, message = "La note minimum est 1")
    @Max(value = 5, message = "La note maximum est 5")
    @Column(nullable = false)
    private Integer note;

    @NotBlank(message = "Le commentaire est obligatoire")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String commentaire;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "visible", nullable = false)
    private boolean visible = true;

    // Constructeur par défaut
    public Avis() {
        this.dateCreation = LocalDateTime.now();
    }

    // Constructeur complet
    public Avis(DemandeEchange demandeEchange, Utilisateur evaluateur,
                Utilisateur evalue, Integer note, String commentaire) {
        this();
        this.demandeEchange = demandeEchange;
        this.evaluateur = evaluateur;
        this.evalue = evalue;
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

    public DemandeEchange getDemandeEchange() {
        return demandeEchange;
    }

    public void setDemandeEchange(DemandeEchange demandeEchange) {
        this.demandeEchange = demandeEchange;
    }

    public Utilisateur getEvaluateur() {
        return evaluateur;
    }

    public void setEvaluateur(Utilisateur evaluateur) {
        this.evaluateur = evaluateur;
    }

    public Utilisateur getEvalue() {
        return evalue;
    }

    public void setEvalue(Utilisateur evalue) {
        this.evalue = evalue;
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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Méthode utilitaire pour mettre à jour l'avis
     * Respecte le principe Open/Closed
     */
    public void modifier(Integer nouvelleNote, String nouveauCommentaire) {
        this.note = nouvelleNote;
        this.commentaire = nouveauCommentaire;
        this.dateModification = LocalDateTime.now();
    }

    /**
     * Vérifie si l'avis peut être modifié (moins de 24h après création)
     */
    public boolean peutEtreModifie() {
        return dateCreation.plusDays(1).isAfter(LocalDateTime.now());
    }
}