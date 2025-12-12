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

@Repository
public interface DemandeEchangeRepository extends JpaRepository<DemandeEchange, Long> {

    // ============ RECHERCHE PAR UTILISATEUR (Listes) ============
    List<DemandeEchange> findByDemandeurOrderByDateCreationDesc(Utilisateur demandeur);

    List<DemandeEchange> findByDestinataireOrderByDateCreationDesc(Utilisateur destinataire);

    // ============ RECHERCHE PAR UTILISATEUR (Pages) ============
    Page<DemandeEchange> findByDemandeur(Utilisateur demandeur, Pageable pageable);

    Page<DemandeEchange> findByDestinataire(Utilisateur destinataire, Pageable pageable);

    Page<DemandeEchange> findByDemandeurAndStatut(Utilisateur demandeur,
                                                  DemandeEchange.StatutDemande statut,
                                                  Pageable pageable);

    Page<DemandeEchange> findByDestinataireAndStatut(Utilisateur destinataire,
                                                     DemandeEchange.StatutDemande statut,
                                                     Pageable pageable);

    // ============ REQUÊTES PERSONNALISÉES ============
    @Query("SELECT d FROM DemandeEchange d WHERE d.destinataire = :utilisateur AND d.statut = 'EN_ATTENTE'")
    List<DemandeEchange> findDemandesEnAttentePourDestinataire(@Param("utilisateur") Utilisateur utilisateur);

    @Query("SELECT d FROM DemandeEchange d WHERE (d.demandeur = :utilisateur OR d.destinataire = :utilisateur) " +
            "AND d.statut = 'ACCEPTEE' ORDER BY d.dateModification DESC")
    List<DemandeEchange> findEchangesActifs(@Param("utilisateur") Utilisateur utilisateur);

    @Query("SELECT COUNT(d) FROM DemandeEchange d WHERE d.destinataire = :utilisateur AND d.statut = 'EN_ATTENTE'")
    long countDemandesEnAttente(@Param("utilisateur") Utilisateur utilisateur);

    @Query("SELECT d FROM DemandeEchange d LEFT JOIN FETCH d.messages WHERE d.id = :id")
    Optional<DemandeEchange> findByIdWithMessages(@Param("id") Long id);

    // ============ RECHERCHE PAR ID ET UTILISATEUR ============
    Optional<DemandeEchange> findByIdAndDestinataire(Long id, Utilisateur destinataire);

    Optional<DemandeEchange> findByIdAndDemandeur(Long id, Utilisateur demandeur);

    // ============ NOUVELLES MÉTHODES POUR LES STATISTIQUES ============

    // Compter les demandes reçues par statut
    @Query("SELECT COUNT(d) FROM DemandeEchange d WHERE d.destinataire = :utilisateur AND d.statut = :statut")
    long countByDestinataireAndStatut(@Param("utilisateur") Utilisateur utilisateur,
                                      @Param("statut") DemandeEchange.StatutDemande statut);

    // Compter toutes les demandes reçues
    @Query("SELECT COUNT(d) FROM DemandeEchange d WHERE d.destinataire = :utilisateur")
    long countByDestinataire(@Param("utilisateur") Utilisateur utilisateur);

    // Compter les demandes envoyées par statut
    @Query("SELECT COUNT(d) FROM DemandeEchange d WHERE d.demandeur = :utilisateur AND d.statut = :statut")
    long countByDemandeurAndStatut(@Param("utilisateur") Utilisateur utilisateur,
                                   @Param("statut") DemandeEchange.StatutDemande statut);

    // Compter toutes les demandes envoyées
    @Query("SELECT COUNT(d) FROM DemandeEchange d WHERE d.demandeur = :utilisateur")
    long countByDemandeur(@Param("utilisateur") Utilisateur utilisateur);
}