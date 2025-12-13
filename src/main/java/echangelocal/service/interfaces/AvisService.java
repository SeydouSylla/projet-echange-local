package echangelocal.service.interfaces;

import echangelocal.dto.AvisDto;
import echangelocal.exception.AvisException;
import echangelocal.model.Avis;
import echangelocal.model.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// Interface définissant les opérations sur les avis
// Respecte le principe d'Interface Segregation (ISP)
public interface AvisService {

    // ============ CRÉATION ET GESTION DES AVIS ============

    // Crée un nouvel avis pour une demande d'échange
    Avis creerAvis(AvisDto avisDto, Utilisateur evaluateur);

    // Modifie un avis existant (dans les 24h)
    Avis modifierAvis(Long avisId, AvisDto avisDto, Utilisateur evaluateur);

    // Supprime (masque) un avis
    void supprimerAvis(Long avisId, Utilisateur utilisateur);

    // ============ RECHERCHE D'AVIS ============

    // Trouve un avis par son ID
    Optional<Avis> trouverParId(Long id);

    // Trouve tous les avis reçus par un utilisateur
    List<Avis> trouverAvisRecus(Utilisateur utilisateur);

    // Trouve tous les avis reçus par un utilisateur (paginé)
    Page<Avis> trouverAvisRecusPagines(Utilisateur utilisateur, Pageable pageable);

    // Trouve tous les avis donnés par un utilisateur
    List<Avis> trouverAvisDonnes(Utilisateur utilisateur);

    // Trouve les derniers avis d'un utilisateur
    List<Avis> trouverDerniersAvis(Utilisateur utilisateur, int limite);

    // Trouve tous les avis publics (paginé)
    Page<Avis> trouverTousLesAvisPublics(Pageable pageable);

    // Trouve tous les avis publics avec filtres
    Page<Avis> trouverAvisPublicsAvecFiltres(Integer noteMin, String recherche, Pageable pageable);

    // ============ STATISTIQUES ET RÉPUTATION ============

    // Calcule la note moyenne d'un utilisateur
    Double calculerNoteMoyenne(Utilisateur utilisateur);

    // Obtient les statistiques complètes d'un utilisateur
    Map<String, Object> obtenirStatistiques(Utilisateur utilisateur);

    // Calcule le taux de satisfaction d'un utilisateur
    Double calculerTauxSatisfaction(Utilisateur utilisateur);

    // Compte le nombre d'avis reçus par un utilisateur
    long compterAvisRecus(Utilisateur utilisateur);

    // ============ VÉRIFICATIONS ============

    // Vérifie si un utilisateur peut laisser un avis pour une demande
    boolean peutLaisserAvis(Long demandeEchangeId, Utilisateur utilisateur);

    // Vérifie si un utilisateur a déjà laissé un avis pour une demande
    boolean aDejaLaisseAvis(Long demandeEchangeId, Utilisateur utilisateur);

    // Vérifie si une demande d'échange peut recevoir des avis
    boolean demandeEchangePeutRecevoirAvis(Long demandeEchangeId);
}
