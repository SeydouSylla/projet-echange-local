package echangelocal.repository;

import echangelocal.model.DemandeEchange;
import echangelocal.model.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
  Repository pour la gestion des demandes d'échange
  Respecte le principe d'Interface Segregation
 */
@Repository
public interface DemandeEchangeRepository extends JpaRepository<DemandeEchange, Long> {

    // ============ RECHERCHE PAR UTILISATEUR ============

    // Trouve toutes les demandes envoyées par un utilisateur
    List<DemandeEchange> findByDemandeurOrderByDateCreationDesc(Utilisateur demandeur);

    // Trouve toutes les demandes reçues par un utilisateur
    List<DemandeEchange> findByDestinataireOrderByDateCreationDesc(Utilisateur destinataire);

    // Trouve une demande par ID et demandeur
    Optional<DemandeEchange> findByIdAndDemandeur(Long id, Utilisateur demandeur);

    // Trouve une demande par ID et destinataire
    Optional<DemandeEchange> findByIdAndDestinataire(Long id, Utilisateur destinataire);

    // ============ RECHERCHE AVEC MESSAGES ============

    // Trouve une demande avec ses messages
    @Query("SELECT d FROM DemandeEchange d LEFT JOIN FETCH d.messages WHERE d.id = :id")
    Optional<DemandeEchange> findByIdWithMessages(@Param("id") Long id);

    // ============ PAGINATION ============

    // Trouve les demandes reçues paginées
    Page<DemandeEchange> findByDestinataire(Utilisateur destinataire, Pageable pageable);

    // Trouve les demandes envoyées paginées
    Page<DemandeEchange> findByDemandeur(Utilisateur demandeur, Pageable pageable);

    // Trouve les demandes reçues par statut
    Page<DemandeEchange> findByDestinataireAndStatut(Utilisateur destinataire,
                                                     DemandeEchange.StatutDemande statut,
                                                     Pageable pageable);

    // Trouve les demandes envoyées par statut
    Page<DemandeEchange> findByDemandeurAndStatut(Utilisateur demandeur,
                                                  DemandeEchange.StatutDemande statut,
                                                  Pageable pageable);

    // ============ ÉCHANGES ACTIFS ET TERMINÉS ============

    // Trouve tous les échanges actifs d'un utilisateur (ACCEPTEE)
    @Query("SELECT d FROM DemandeEchange d WHERE (d.demandeur = :utilisateur OR d.destinataire = :utilisateur) " +
            "AND d.statut = 'ACCEPTEE' ORDER BY d.dateCreation DESC")
    List<DemandeEchange> findEchangesActifs(@Param("utilisateur") Utilisateur utilisateur);

    // Trouve tous les échanges terminés d'un utilisateur (TERMINEE)
    @Query("SELECT d FROM DemandeEchange d WHERE (d.demandeur = :utilisateur OR d.destinataire = :utilisateur) " +
            "AND d.statut = 'TERMINEE' ORDER BY d.dateCreation DESC")
    List<DemandeEchange> findEchangesTermines(@Param("utilisateur") Utilisateur utilisateur);

    // Trouve tous les échanges terminés d'un utilisateur paginés
    @Query("SELECT d FROM DemandeEchange d WHERE (d.demandeur = :utilisateur OR d.destinataire = :utilisateur) " +
            "AND d.statut = 'TERMINEE' ORDER BY d.dateCreation DESC")
    Page<DemandeEchange> findEchangesTerminesPage(@Param("utilisateur") Utilisateur utilisateur, Pageable pageable);

    // ============ STATISTIQUES ============

    // Compte le nombre de demandes en attente reçues
    @Query("SELECT COUNT(d) FROM DemandeEchange d WHERE d.destinataire = :utilisateur AND d.statut = 'EN_ATTENTE'")
    long countDemandesEnAttente(@Param("utilisateur") Utilisateur utilisateur);

    // Compte les demandes reçues par statut
    long countByDestinataireAndStatut(Utilisateur destinataire, DemandeEchange.StatutDemande statut);

    // Compte toutes les demandes reçues
    long countByDestinataire(Utilisateur destinataire);

    // Compte les demandes envoyées par statut
    long countByDemandeurAndStatut(Utilisateur demandeur, DemandeEchange.StatutDemande statut);

    // Compte toutes les demandes envoyées
    long countByDemandeur(Utilisateur demandeur);
}