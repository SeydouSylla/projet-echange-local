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

public interface DemandeEchangeService {

    // ============ CRÉATION ET GESTION DES DEMANDES ============
    DemandeEchange creerDemandeEchange(DemandeEchangeDto demandeDto, Utilisateur demandeur);

    DemandeEchange accepterDemande(Long demandeId, Utilisateur destinataire);

    DemandeEchange refuserDemande(Long demandeId, Utilisateur destinataire);

    void annulerDemande(Long demandeId, Utilisateur demandeur);

    // ============ MESSAGERIE ============
    Message envoyerMessage(MessageDto messageDto, Utilisateur expediteur);

    // ============ RECHERCHE PAR ID ============
    Optional<DemandeEchange> trouverParId(Long id);

    Optional<DemandeEchange> trouverParIdAvecMessages(Long id);

    // ============ RECHERCHE PAR UTILISATEUR (Listes) ============
    List<DemandeEchange> trouverDemandesEnvoyees(Utilisateur utilisateur);

    List<DemandeEchange> trouverDemandesRecues(Utilisateur utilisateur);

    List<DemandeEchange> trouverEchangesActifs(Utilisateur utilisateur);

    // ============ RECHERCHE PAR UTILISATEUR (Pages) ============
    Page<DemandeEchange> trouverDemandesEnvoyeesPage(Utilisateur utilisateur, Pageable pageable);

    Page<DemandeEchange> trouverDemandesEnvoyeesParStatut(Utilisateur utilisateur,
                                                          DemandeEchange.StatutDemande statut,
                                                          Pageable pageable);

    Page<DemandeEchange> trouverDemandesRecuesPage(Utilisateur utilisateur, Pageable pageable);

    Page<DemandeEchange> trouverDemandesRecuesParStatut(Utilisateur utilisateur,
                                                        DemandeEchange.StatutDemande statut,
                                                        Pageable pageable);

    // ============ COMPTAGE ET VÉRIFICATIONS ============
    long compterDemandesEnAttente(Utilisateur utilisateur);

    boolean estDestinataire(Long demandeId, Utilisateur utilisateur);

    boolean estDemandeur(Long demandeId, Utilisateur utilisateur);

    boolean peutEnvoyerMessage(Long demandeId, Utilisateur utilisateur);

    // ============ NOUVELLES MÉTHODES POUR LES STATISTIQUES ============

    // Compter les demandes reçues par statut
    long compterDemandesRecuesParStatut(Utilisateur utilisateur, DemandeEchange.StatutDemande statut);

    // Compter toutes les demandes reçues
    long compterToutesDemandesRecues(Utilisateur utilisateur);

    // Compter les demandes envoyées par statut
    long compterDemandesEnvoyeesParStatut(Utilisateur utilisateur, DemandeEchange.StatutDemande statut);

    // Compter toutes les demandes envoyées
    long compterToutesDemandesEnvoyees(Utilisateur utilisateur);
}