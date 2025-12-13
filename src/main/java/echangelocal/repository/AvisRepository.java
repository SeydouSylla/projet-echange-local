package echangelocal.repository;

import echangelocal.model.Avis;
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
  Repository pour la gestion des avis
  Respecte le principe d'Interface Segregation
 */
@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {

    // ============ RECHERCHE PAR UTILISATEUR ============

    //Trouve tous les avis reçus par un utilisateur
    @Query("SELECT a FROM Avis a WHERE a.evalue = :utilisateur AND a.visible = true ORDER BY a.dateCreation DESC")
    List<Avis> findByEvalue(@Param("utilisateur") Utilisateur utilisateur);

    //Trouve tous les avis reçus par un utilisateur (paginé)
    @Query("SELECT a FROM Avis a WHERE a.evalue = :utilisateur AND a.visible = true ORDER BY a.dateCreation DESC")
    Page<Avis> findByEvalue(@Param("utilisateur") Utilisateur utilisateur, Pageable pageable);

    //Trouve tous les avis donnés par un utilisateur
    @Query("SELECT a FROM Avis a WHERE a.evaluateur = :utilisateur ORDER BY a.dateCreation DESC")
    List<Avis> findByEvaluateur(@Param("utilisateur") Utilisateur utilisateur);

    // ============ RECHERCHE PAR DEMANDE D'ÉCHANGE ============

    //Trouve l'avis laissé par un utilisateur pour une demande d'échange
    Optional<Avis> findByDemandeEchangeAndEvaluateur(DemandeEchange demandeEchange, Utilisateur evaluateur);

    //Trouve tous les avis d'une demande d'échange
    List<Avis> findByDemandeEchange(DemandeEchange demandeEchange);

    //Vérifie si un utilisateur a déjà laissé un avis pour une demande
    boolean existsByDemandeEchangeAndEvaluateur(DemandeEchange demandeEchange, Utilisateur evaluateur);

    // ============ AVIS PUBLICS ============

    //Trouve tous les avis publics triés par date de création
    @Query("SELECT a FROM Avis a WHERE a.visible = true ORDER BY a.dateCreation DESC")
    Page<Avis> findAllVisiblesOrderByDateCreationDesc(Pageable pageable);

    //Trouve les avis publics avec note minimale
    Page<Avis> findByNoteGreaterThanEqualAndVisible(Integer note, boolean visible, Pageable pageable);

    //Trouve les avis publics par recherche dans le commentaire
    Page<Avis> findByCommentaireContainingIgnoreCaseAndVisible(String recherche, boolean visible, Pageable pageable);

    // Trouve les avis publics avec note minimale et recherche
    Page<Avis> findByNoteGreaterThanEqualAndCommentaireContainingIgnoreCaseAndVisible(
            Integer note, String recherche, boolean visible, Pageable pageable);

    // ============ STATISTIQUES ============

    // Calcule la note moyenne d'un utilisateur
    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.evalue = :utilisateur AND a.visible = true")
    Double calculerNoteMoyenne(@Param("utilisateur") Utilisateur utilisateur);

    // Compte le nombre total d'avis reçus par un utilisateur
    @Query("SELECT COUNT(a) FROM Avis a WHERE a.evalue = :utilisateur AND a.visible = true")
    long compterAvisRecus(@Param("utilisateur") Utilisateur utilisateur);

    // Compte le nombre d'avis par note pour un utilisateur
    @Query("SELECT COUNT(a) FROM Avis a WHERE a.evalue = :utilisateur AND a.note = :note AND a.visible = true")
    long compterAvisParNote(@Param("utilisateur") Utilisateur utilisateur, @Param("note") Integer note);

    // Trouve les derniers avis reçus par un utilisateur
    @Query("SELECT a FROM Avis a WHERE a.evalue = :utilisateur AND a.visible = true ORDER BY a.dateCreation DESC")
    List<Avis> findTop5ByEvalue(@Param("utilisateur") Utilisateur utilisateur, Pageable pageable);

    // ============ VÉRIFICATIONS ============

    // Vérifie si une demande d'échange a reçu tous ses avis (2 avis attendus)
    @Query("SELECT COUNT(a) FROM Avis a WHERE a.demandeEchange = :demande")
    long compterAvisParDemande(@Param("demande") DemandeEchange demande);
}