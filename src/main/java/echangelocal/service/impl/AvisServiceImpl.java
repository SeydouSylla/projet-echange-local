package echangelocal.service.impl;

import echangelocal.dto.AvisDto;
import echangelocal.exception.AvisException;
import echangelocal.exception.OperationNonAutoriseeException;
import echangelocal.model.Avis;
import echangelocal.model.DemandeEchange;
import echangelocal.model.Utilisateur;
import echangelocal.repository.AvisRepository;
import echangelocal.repository.DemandeEchangeRepository;
import echangelocal.service.interfaces.AvisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Implémentation du service de gestion des avis
 * Respecte les principes SOLID et l'architecture en couches
 */
@Service
@Transactional
public class AvisServiceImpl implements AvisService {

    private final AvisRepository avisRepository;
    private final DemandeEchangeRepository demandeEchangeRepository;

    // Constructeur injectant les repositories nécessaires à la gestion des avis
    @Autowired
    public AvisServiceImpl(AvisRepository avisRepository,
                           DemandeEchangeRepository demandeEchangeRepository) {
        this.avisRepository = avisRepository;
        this.demandeEchangeRepository = demandeEchangeRepository;
    }

    // ============ CRÉATION ET GESTION DES AVIS ============

    // Crée un nouvel avis après validation des données et des autorisations
    @Override
    public Avis creerAvis(AvisDto avisDto, Utilisateur evaluateur) {
        validerAvisDto(avisDto);

        DemandeEchange demande = demandeEchangeRepository.findById(avisDto.getDemandeEchangeId())
                .orElseThrow(() -> new AvisException("Demande d'échange non trouvée"));

        verifierAutorisationCreerAvis(demande, evaluateur);

        Utilisateur evalue = determinerUtilisateurEvalue(demande, evaluateur);

        Avis avis = new Avis();
        avis.setDemandeEchange(demande);
        avis.setEvaluateur(evaluateur);
        avis.setEvalue(evalue);
        avis.setNote(avisDto.getNote());
        avis.setCommentaire(avisDto.getCommentaire());

        Avis avisSauvegarde = avisRepository.saveAndFlush(avis);

        verifierEtMarquerEchangeTermine(demande);

        return avisSauvegarde;
    }

    // Modifie un avis existant si l'utilisateur est autorisé et dans le délai permis
    @Override
    public Avis modifierAvis(Long avisId, AvisDto avisDto, Utilisateur evaluateur) {
        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new AvisException("Avis non trouvé"));

        if (!avis.getEvaluateur().equals(evaluateur)) {
            throw new OperationNonAutoriseeException("Vous n'êtes pas autorisé à modifier cet avis");
        }

        if (!avis.peutEtreModifie()) {
            throw new AvisException("L'avis ne peut plus être modifié (délai de 24h dépassé)");
        }

        avis.modifier(avisDto.getNote(), avisDto.getCommentaire());

        return avisRepository.saveAndFlush(avis);
    }

    // Supprime logiquement un avis en le rendant invisible
    @Override
    public void supprimerAvis(Long avisId, Utilisateur utilisateur) {
        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new AvisException("Avis non trouvé"));

        if (!avis.getEvaluateur().equals(utilisateur) && !avis.getEvalue().equals(utilisateur)) {
            throw new OperationNonAutoriseeException("Vous n'êtes pas autorisé à supprimer cet avis");
        }

        avis.setVisible(false);
        avisRepository.saveAndFlush(avis);
    }

    // ============ RECHERCHE D'AVIS ============

    // Recherche un avis par son identifiant
    @Override
    @Transactional(readOnly = true)
    public Optional<Avis> trouverParId(Long id) {
        return avisRepository.findById(id);
    }

    // Récupère la liste des avis reçus par un utilisateur
    @Override
    @Transactional(readOnly = true)
    public List<Avis> trouverAvisRecus(Utilisateur utilisateur) {
        return avisRepository.findByEvalue(utilisateur);
    }

    // Récupère les avis reçus par un utilisateur avec pagination
    @Override
    @Transactional(readOnly = true)
    public Page<Avis> trouverAvisRecusPagines(Utilisateur utilisateur, Pageable pageable) {
        return avisRepository.findByEvalue(utilisateur, pageable);
    }

    // Récupère les avis donnés par un utilisateur
    @Override
    @Transactional(readOnly = true)
    public List<Avis> trouverAvisDonnes(Utilisateur utilisateur) {
        return avisRepository.findByEvaluateur(utilisateur);
    }

    // Récupère les derniers avis reçus par un utilisateur selon une limite
    @Override
    @Transactional(readOnly = true)
    public List<Avis> trouverDerniersAvis(Utilisateur utilisateur, int limite) {
        Pageable pageable = PageRequest.of(0, limite);
        return avisRepository.findTop5ByEvalue(utilisateur, pageable);
    }

    // Récupère tous les avis publics avec pagination
    @Override
    @Transactional(readOnly = true)
    public Page<Avis> trouverTousLesAvisPublics(Pageable pageable) {
        return avisRepository.findAllVisiblesOrderByDateCreationDesc(pageable);
    }

    // Recherche des avis publics avec filtres optionnels (note minimale et texte)
    @Override
    @Transactional(readOnly = true)
    public Page<Avis> trouverAvisPublicsAvecFiltres(Integer noteMin, String recherche, Pageable pageable) {
        if (noteMin != null && recherche != null && !recherche.trim().isEmpty()) {
            return avisRepository.findByNoteGreaterThanEqualAndCommentaireContainingIgnoreCaseAndVisible(
                    noteMin, recherche, true, pageable);
        } else if (noteMin != null) {
            return avisRepository.findByNoteGreaterThanEqualAndVisible(noteMin, true, pageable);
        } else if (recherche != null && !recherche.trim().isEmpty()) {
            return avisRepository.findByCommentaireContainingIgnoreCaseAndVisible(recherche, true, pageable);
        } else {
            return avisRepository.findAllVisiblesOrderByDateCreationDesc(pageable);
        }
    }

    // ============ STATISTIQUES ET RÉPUTATION ============

    // Calcule la note moyenne arrondie d'un utilisateur
    @Override
    @Transactional(readOnly = true)
    public Double calculerNoteMoyenne(Utilisateur utilisateur) {
        Double moyenne = avisRepository.calculerNoteMoyenne(utilisateur);
        return moyenne != null ? Math.round(moyenne * 10.0) / 10.0 : 0.0;
    }

    // Génère les statistiques complètes de réputation d'un utilisateur
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenirStatistiques(Utilisateur utilisateur) {
        Map<String, Object> statistiques = new HashMap<>();

        Double noteMoyenne = calculerNoteMoyenne(utilisateur);
        statistiques.put("noteMoyenne", noteMoyenne);

        long totalAvis = avisRepository.compterAvisRecus(utilisateur);
        statistiques.put("totalAvis", totalAvis);

        Map<Integer, Long> repartition = initialiserMapRepartition();
        for (int note = 1; note <= 5; note++) {
            long count = avisRepository.compterAvisParNote(utilisateur, note);
            repartition.put(note, count);
        }
        statistiques.put("repartitionNotes", repartition);

        Double tauxSatisfaction = calculerTauxSatisfaction(utilisateur);
        statistiques.put("tauxSatisfaction", tauxSatisfaction);

        long avisPositifs = repartition.get(4) + repartition.get(5);
        statistiques.put("avisPositifs", avisPositifs);

        long avisNegatifs = repartition.get(1) + repartition.get(2);
        statistiques.put("avisNegatifs", avisNegatifs);

        return statistiques;
    }

    // Calcule le taux de satisfaction basé sur les avis positifs
    @Override
    @Transactional(readOnly = true)
    public Double calculerTauxSatisfaction(Utilisateur utilisateur) {
        long totalAvis = avisRepository.compterAvisRecus(utilisateur);
        if (totalAvis == 0) {
            return 0.0;
        }

        long avisPositifs = avisRepository.compterAvisParNote(utilisateur, 4) +
                avisRepository.compterAvisParNote(utilisateur, 5);

        return Math.round((avisPositifs * 100.0 / totalAvis) * 10.0) / 10.0;
    }

    // Compte le nombre total d'avis reçus par un utilisateur
    @Override
    @Transactional(readOnly = true)
    public long compterAvisRecus(Utilisateur utilisateur) {
        return avisRepository.compterAvisRecus(utilisateur);
    }

    // ============ VÉRIFICATIONS ============

    // Vérifie si un utilisateur est autorisé à laisser un avis pour une demande donnée
    @Override
    @Transactional(readOnly = true)
    public boolean peutLaisserAvis(Long demandeEchangeId, Utilisateur utilisateur) {
        Optional<DemandeEchange> demandeOpt = demandeEchangeRepository.findById(demandeEchangeId);
        if (demandeOpt.isEmpty()) {
            return false;
        }

        DemandeEchange demande = demandeOpt.get();

        if (demande.getStatut() != DemandeEchange.StatutDemande.ACCEPTEE &&
                demande.getStatut() != DemandeEchange.StatutDemande.TERMINEE) {
            return false;
        }

        boolean estParticipant = demande.getDemandeur().equals(utilisateur) ||
                demande.getDestinataire().equals(utilisateur);
        if (!estParticipant) {
            return false;
        }

        return !avisRepository.existsByDemandeEchangeAndEvaluateur(demande, utilisateur);
    }

    // Vérifie si l'utilisateur a déjà laissé un avis pour une demande donnée
    @Override
    @Transactional(readOnly = true)
    public boolean aDejaLaisseAvis(Long demandeEchangeId, Utilisateur utilisateur) {
        Optional<DemandeEchange> demandeOpt = demandeEchangeRepository.findById(demandeEchangeId);
        if (demandeOpt.isEmpty()) {
            return false;
        }

        return avisRepository.existsByDemandeEchangeAndEvaluateur(demandeOpt.get(), utilisateur);
    }

    // Vérifie si une demande d'échange peut recevoir des avis
    @Override
    @Transactional(readOnly = true)
    public boolean demandeEchangePeutRecevoirAvis(Long demandeEchangeId) {
        Optional<DemandeEchange> demandeOpt = demandeEchangeRepository.findById(demandeEchangeId);
        if (demandeOpt.isEmpty()) {
            return false;
        }

        DemandeEchange demande = demandeOpt.get();
        return demande.getStatut() == DemandeEchange.StatutDemande.ACCEPTEE ||
                demande.getStatut() == DemandeEchange.StatutDemande.TERMINEE;
    }

    // ============ MÉTHODES PRIVÉES UTILITAIRES ============

    // Valide les données du DTO avant la création ou modification d'un avis
    private void validerAvisDto(AvisDto avisDto) {
        if (avisDto == null) {
            throw new IllegalArgumentException("Les données de l'avis ne peuvent pas être nulles");
        }
        if (avisDto.getDemandeEchangeId() == null) {
            throw new IllegalArgumentException("L'ID de la demande d'échange est obligatoire");
        }
        if (avisDto.getNote() == null || avisDto.getNote() < 1 || avisDto.getNote() > 5) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }
        if (avisDto.getCommentaire() == null || avisDto.getCommentaire().trim().isEmpty()) {
            throw new IllegalArgumentException("Le commentaire est obligatoire");
        }
        if (avisDto.getCommentaire().length() < 10) {
            throw new IllegalArgumentException("Le commentaire doit contenir au moins 10 caractères");
        }
    }

    // Vérifie que l'utilisateur est autorisé à créer un avis pour cette demande
    private void verifierAutorisationCreerAvis(DemandeEchange demande, Utilisateur evaluateur) {
        if (demande.getStatut() != DemandeEchange.StatutDemande.ACCEPTEE &&
                demande.getStatut() != DemandeEchange.StatutDemande.TERMINEE) {
            throw new AvisException("Vous ne pouvez laisser un avis que pour un échange accepté");
        }

        boolean estParticipant = demande.getDemandeur().equals(evaluateur) ||
                demande.getDestinataire().equals(evaluateur);
        if (!estParticipant) {
            throw new OperationNonAutoriseeException("Vous n'êtes pas participant à cet échange");
        }

        if (avisRepository.existsByDemandeEchangeAndEvaluateur(demande, evaluateur)) {
            throw new AvisException("Vous avez déjà laissé un avis pour cet échange");
        }
    }

    // Détermine quel utilisateur est évalué dans l'échange
    private Utilisateur determinerUtilisateurEvalue(DemandeEchange demande, Utilisateur evaluateur) {
        if (demande.getDemandeur().equals(evaluateur)) {
            return demande.getDestinataire();
        } else {
            return demande.getDemandeur();
        }
    }

    // Marque l'échange comme terminé lorsque les deux avis ont été laissés
    private void verifierEtMarquerEchangeTermine(DemandeEchange demande) {
        long nombreAvis = avisRepository.compterAvisParDemande(demande);

        if (nombreAvis == 2 && demande.getStatut() == DemandeEchange.StatutDemande.ACCEPTEE) {
            demande.setStatut(DemandeEchange.StatutDemande.TERMINEE);
            demandeEchangeRepository.saveAndFlush(demande);
        }
    }

    // Initialise la répartition des notes de 1 à 5 à zéro
    private Map<Integer, Long> initialiserMapRepartition() {
        Map<Integer, Long> repartition = new HashMap<>();
        for (int note = 1; note <= 5; note++) {
            repartition.put(note, 0L);
        }
        return repartition;
    }
}
