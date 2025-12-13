package echangelocal.service.interfaces;

import echangelocal.dto.DemandeEchangeDto;
import echangelocal.dto.MessageDto;
import echangelocal.model.DemandeEchange;
import echangelocal.model.Message;
import echangelocal.model.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

// Interface définissant les opérations sur les demandes d'échange
// Respecte le principe d'Interface Segregation (ISP)
public interface DemandeEchangeService {

    // ============ CRÉATION ET GESTION DES DEMANDES ============

    // Crée une nouvelle demande d'échange
    DemandeEchange creerDemandeEchange(DemandeEchangeDto demandeDto, Utilisateur demandeur);

    // Accepte une demande d'échange
    DemandeEchange accepterDemande(Long demandeId, Utilisateur destinataire);

    // Refuse une demande d'échange
    DemandeEchange refuserDemande(Long demandeId, Utilisateur destinataire);

    // Annule une demande d'échange (par le demandeur)
    void annulerDemande(Long demandeId, Utilisateur demandeur);

    // ============ MESSAGERIE ============

    // Envoie un message dans une demande d'échange
    Message envoyerMessage(MessageDto messageDto, Utilisateur expediteur);

    // ============ RECHERCHE DE DEMANDES ============

    // Trouve une demande par son ID
    Optional<DemandeEchange> trouverParId(Long id);

    // Trouve une demande par son ID avec ses messages
    Optional<DemandeEchange> trouverParIdAvecMessages(Long id);

    // Trouve toutes les demandes envoyées par un utilisateur
    List<DemandeEchange> trouverDemandesEnvoyees(Utilisateur utilisateur);

    // Trouve toutes les demandes reçues par un utilisateur
    List<DemandeEchange> trouverDemandesRecues(Utilisateur utilisateur);

    // Trouve toutes les demandes reçues paginées
    Page<DemandeEchange> trouverDemandesRecuesPage(Utilisateur utilisateur, Pageable pageable);

    // Trouve toutes les demandes envoyées paginées
    Page<DemandeEchange> trouverDemandesEnvoyeesPage(Utilisateur utilisateur, Pageable pageable);

    // Trouve les demandes reçues par statut
    Page<DemandeEchange> trouverDemandesRecuesParStatut(Utilisateur utilisateur,
                                                        DemandeEchange.StatutDemande statut,
                                                        Pageable pageable);

    // Trouve les demandes envoyées par statut
    Page<DemandeEchange> trouverDemandesEnvoyeesParStatut(Utilisateur utilisateur,
                                                          DemandeEchange.StatutDemande statut,
                                                          Pageable pageable);

    // Trouve les échanges actifs (statut ACCEPTEE)
    List<DemandeEchange> trouverEchangesActifs(Utilisateur utilisateur);

    // Trouve les échanges terminés (statut TERMINEE)
    List<DemandeEchange> trouverEchangesTermines(Utilisateur utilisateur);

    // Trouve les échanges terminés paginés
    Page<DemandeEchange> trouverEchangesTerminesPage(Utilisateur utilisateur, Pageable pageable);

    // ============ STATISTIQUES ============

    // Compte le nombre de demandes en attente pour un utilisateur (reçues)
    long compterDemandesEnAttente(Utilisateur utilisateur);

    // Compte les demandes reçues par statut
    long compterDemandesRecuesParStatut(Utilisateur utilisateur, DemandeEchange.StatutDemande statut);

    // Compte toutes les demandes reçues
    long compterToutesDemandesRecues(Utilisateur utilisateur);

    // Compte les demandes envoyées par statut
    long compterDemandesEnvoyeesParStatut(Utilisateur utilisateur, DemandeEchange.StatutDemande statut);

    // Compte toutes les demandes envoyées
    long compterToutesDemandesEnvoyees(Utilisateur utilisateur);

    // ============ VÉRIFICATIONS ============

    // Vérifie si un utilisateur est le destinataire d'une demande
    boolean estDestinataire(Long demandeId, Utilisateur utilisateur);

    // Vérifie si un utilisateur est le demandeur d'une demande
    boolean estDemandeur(Long demandeId, Utilisateur utilisateur);

    // Vérifie si un utilisateur peut envoyer des messages dans une demande
    boolean peutEnvoyerMessage(Long demandeId, Utilisateur utilisateur);

    // Vérifie si un utilisateur peut consulter l'historique d'une demande
    boolean peutConsulterHistorique(Long demandeId, Utilisateur utilisateur);
}
