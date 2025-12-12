package echangelocal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "demandes_echange")
public class DemandeEchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demandeur_id", nullable = false)
    private Utilisateur demandeur;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id")
    private Annonce annonce;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competence_id")
    private Competence competence;

    @NotBlank(message = "La proposition d'échange est obligatoire")
    @Column(name = "proposition_echange", length = 500)
    private String propositionEchange;

    @NotBlank(message = "Le message d'accompagnement est obligatoire")
    @Column(name = "message_demande", length = 1000)  // Nom actuel de la colonne
    private String messageDemande;

    @NotNull
    @Future(message = "La date d'échange doit être future")
    @Column(name = "date_proposee")
    private LocalDateTime dateProposee;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_echange")
    private TypeEchange typeEchange;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @OneToMany(mappedBy = "demandeEchange", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateEnvoi ASC")
    private List<Message> messages = new ArrayList<>();

    public DemandeEchange() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    // GETTERS ET SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utilisateur getDemandeur() {
        return demandeur;
    }

    public void setDemandeur(Utilisateur demandeur) {
        this.demandeur = demandeur;
    }

    public Utilisateur getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(Utilisateur destinataire) {
        this.destinataire = destinataire;
    }

    public Annonce getAnnonce() {
        return annonce;
    }

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
        this.typeEchange = TypeEchange.OBJET;
    }

    public Competence getCompetence() {
        return competence;
    }

    public void setCompetence(Competence competence) {
        this.competence = competence;
        this.typeEchange = TypeEchange.COMPETENCE;
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

    public StatutDemande getStatut() {
        return statut;
    }

    public void setStatut(StatutDemande statut) {
        this.statut = statut;
        this.dateModification = LocalDateTime.now();
    }

    public TypeEchange getTypeEchange() {
        return typeEchange;
    }

    public void setTypeEchange(TypeEchange typeEchange) {
        this.typeEchange = typeEchange;
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

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void ajouterMessage(Message message) {
        this.messages.add(message);
        message.setDemandeEchange(this);
    }

    public enum StatutDemande {
        EN_ATTENTE, ACCEPTEE, REFUSEE, ANNULEE, TERMINEE
    }

    public enum TypeEchange {
        OBJET, COMPETENCE
    }
}