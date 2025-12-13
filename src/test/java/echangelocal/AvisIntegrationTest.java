package echangelocal;

import echangelocal.dto.AvisDto;
import echangelocal.model.*;
import echangelocal.repository.*;
import echangelocal.service.interfaces.AvisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
  Tests d'intégration pour la fonctionnalité 5 : Système d'avis et de réputation
  Respecte les bonnes pratiques de test (AAA pattern)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Commit
class AvisIntegrationTest {

    @Autowired
    private AvisService avisService;

    @Autowired
    private AvisRepository avisRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private DemandeEchangeRepository demandeEchangeRepository;

    // ============ TESTS DE CRÉATION D'AVIS ============

    @Test
    void creerAvis_DoitFonctionner_QuandDonneesValides() {
        // ---------- ARRANGE ----------
        // Créer deux utilisateurs
        Utilisateur demandeur = new Utilisateur(
                "demandeur@avis.com",
                "password",
                "Jean",
                "Dupont",
                "Paris"
        );
        demandeur.setTelephoneVerifie(true);
        Utilisateur savedDemandeur = utilisateurRepository.save(demandeur);

        Utilisateur destinataire = new Utilisateur(
                "destinataire@avis.com",
                "password",
                "Marie",
                "Martin",
                "Paris"
        );
        destinataire.setTelephoneVerifie(true);
        Utilisateur savedDestinataire = utilisateurRepository.save(destinataire);

        // Créer une annonce
        Annonce annonce = new Annonce();
        annonce.setTitre("Perceuse");
        annonce.setDescription("Perceuse électrique en bon état");
        annonce.setTypeAnnonce(Annonce.TypeAnnonce.PRET);
        annonce.setCategorie("Bricolage");
        annonce.setCreateur(savedDestinataire);
        annonce.setDisponible(true);
        Annonce savedAnnonce = annonceRepository.save(annonce);

        // Créer une demande d'échange acceptée
        DemandeEchange demande = new DemandeEchange();
        demande.setDemandeur(savedDemandeur);
        demande.setDestinataire(savedDestinataire);
        demande.setAnnonce(savedAnnonce);
        demande.setPropositionEchange("Je propose ma scie sauteuse");
        demande.setMessageDemande("Bonjour, j'ai besoin de votre perceuse");
        demande.setDateProposee(LocalDateTime.now().plusDays(1));
        demande.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);
        DemandeEchange savedDemande = demandeEchangeRepository.save(demande);

        // Créer le DTO d'avis
        AvisDto avisDto = new AvisDto();
        avisDto.setDemandeEchangeId(savedDemande.getId());
        avisDto.setNote(5);
        avisDto.setCommentaire("Excellent échange, personne très ponctuelle et sympathique !");

        // ---------- ACT ----------
        Avis avisCreé = avisService.creerAvis(avisDto, savedDemandeur);

        // ---------- ASSERT ----------
        assertNotNull(avisCreé.getId());
        assertEquals(5, avisCreé.getNote());
        assertEquals("Excellent échange, personne très ponctuelle et sympathique !",
                avisCreé.getCommentaire());
        assertEquals(savedDemandeur.getId(), avisCreé.getEvaluateur().getId());
        assertEquals(savedDestinataire.getId(), avisCreé.getEvalue().getId());
        assertTrue(avisCreé.isVisible());
    }


    // ============ TESTS DE STATISTIQUES ============

    @Test
    void calculerNoteMoyenne_DoitRetournerMoyenneCorrecte() {
        // ---------- ARRANGE ----------
        Utilisateur evaluateur = new Utilisateur(
                "evaluateur@stat.com",
                "password",
                "Évaluateur",
                "Test",
                "Marseille"
        );
        evaluateur.setTelephoneVerifie(true);
        Utilisateur savedEvaluateur = utilisateurRepository.save(evaluateur);

        Utilisateur evalue = new Utilisateur(
                "evalue@stat.com",
                "password",
                "Évalué",
                "Test",
                "Marseille"
        );
        evalue.setTelephoneVerifie(true);
        Utilisateur savedEvalue = utilisateurRepository.save(evalue);

        // Créer plusieurs avis
        for (int i = 0; i < 3; i++) {
            Annonce annonce = new Annonce();
            annonce.setTitre("Objet " + i);
            annonce.setDescription("Description " + i);
            annonce.setTypeAnnonce(Annonce.TypeAnnonce.PRET);
            annonce.setCategorie("Test");
            annonce.setCreateur(savedEvalue);
            annonce.setDisponible(true);
            Annonce savedAnnonce = annonceRepository.save(annonce);

            DemandeEchange demande = new DemandeEchange();
            demande.setDemandeur(savedEvaluateur);
            demande.setDestinataire(savedEvalue);
            demande.setAnnonce(savedAnnonce);
            demande.setPropositionEchange("Prop " + i);
            demande.setMessageDemande("Msg " + i);
            demande.setDateProposee(LocalDateTime.now().plusDays(1));
            demande.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);
            DemandeEchange savedDemande = demandeEchangeRepository.save(demande);

            AvisDto avisDto = new AvisDto();
            avisDto.setDemandeEchangeId(savedDemande.getId());
            avisDto.setNote(i + 3); // Notes: 3, 4, 5
            avisDto.setCommentaire("Commentaire " + i);
            avisService.creerAvis(avisDto, savedEvaluateur);
        }

        // ---------- ACT ----------
        Double noteMoyenne = avisService.calculerNoteMoyenne(savedEvalue);

        // ---------- ASSERT ----------
        assertEquals(4.0, noteMoyenne, 0.1); // Moyenne de 3, 4, 5 = 4.0
    }

    @Test
    void obtenirStatistiques_DoitRetournerStatistiquesCompletes() {
        // ---------- ARRANGE ----------
        Utilisateur evaluateur1 = new Utilisateur(
                "eval1@stats.com",
                "password",
                "Eval1",
                "Test",
                "Lille"
        );
        evaluateur1.setTelephoneVerifie(true);
        Utilisateur savedEvaluateur1 = utilisateurRepository.save(evaluateur1);

        Utilisateur evaluateur2 = new Utilisateur(
                "eval2@stats.com",
                "password",
                "Eval2",
                "Test",
                "Lille"
        );
        evaluateur2.setTelephoneVerifie(true);
        Utilisateur savedEvaluateur2 = utilisateurRepository.save(evaluateur2);

        Utilisateur evalue = new Utilisateur(
                "evalue2@stats.com",
                "password",
                "EvaluéStats",
                "Test",
                "Lille"
        );
        evalue.setTelephoneVerifie(true);
        Utilisateur savedEvalue = utilisateurRepository.save(evalue);

        // Créer 5 avis avec notes différentes
        int[] notes = {5, 5, 4, 3, 2};
        Utilisateur[] evaluateurs = {savedEvaluateur1, savedEvaluateur2, savedEvaluateur1, savedEvaluateur2, savedEvaluateur1};

        for (int i = 0; i < notes.length; i++) {
            Annonce annonce = new Annonce();
            annonce.setTitre("Objet Stats " + i);
            annonce.setDescription("Desc " + i);
            annonce.setTypeAnnonce(Annonce.TypeAnnonce.PRET);
            annonce.setCategorie("Test");
            annonce.setCreateur(savedEvalue);
            annonce.setDisponible(true);
            Annonce savedAnnonce = annonceRepository.save(annonce);

            DemandeEchange demande = new DemandeEchange();
            demande.setDemandeur(evaluateurs[i]);
            demande.setDestinataire(savedEvalue);
            demande.setAnnonce(savedAnnonce);
            demande.setPropositionEchange("Prop " + i);
            demande.setMessageDemande("Msg " + i);
            demande.setDateProposee(LocalDateTime.now().plusDays(1));
            demande.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);
            DemandeEchange savedDemande = demandeEchangeRepository.save(demande);

            AvisDto avisDto = new AvisDto();
            avisDto.setDemandeEchangeId(savedDemande.getId());
            avisDto.setNote(notes[i]);
            avisDto.setCommentaire("Commentaire " + i);
            avisService.creerAvis(avisDto, evaluateurs[i]);
        }

        // ---------- ACT ----------
        Map<String, Object> statistiques = avisService.obtenirStatistiques(savedEvalue);

        // ---------- ASSERT ----------
        assertNotNull(statistiques);
        assertEquals(5L, statistiques.get("totalAvis"));

        Double noteMoyenne = (Double) statistiques.get("noteMoyenne");
        assertEquals(3.8, noteMoyenne, 0.1); // (5+5+4+3+2)/5 = 3.8

        @SuppressWarnings("unchecked")
        Map<Integer, Long> repartition = (Map<Integer, Long>) statistiques.get("repartitionNotes");
        assertEquals(2L, repartition.get(5)); // Deux notes de 5
        assertEquals(1L, repartition.get(4)); // Une note de 4
        assertEquals(1L, repartition.get(3)); // Une note de 3
        assertEquals(1L, repartition.get(2)); // Une note de 2
        assertEquals(0L, repartition.get(1)); // Aucune note de 1
    }

    @Test
    void calculerTauxSatisfaction_DoitRetournerPourcentageCorrect() {
        // ---------- ARRANGE ----------
        Utilisateur evaluateur = new Utilisateur(
                "eval@satisfaction.com",
                "password",
                "Éval",
                "Satisfaction",
                "Bordeaux"
        );
        evaluateur.setTelephoneVerifie(true);
        Utilisateur savedEvaluateur = utilisateurRepository.save(evaluateur);

        Utilisateur evalue = new Utilisateur(
                "evalue@satisfaction.com",
                "password",
                "Évalué",
                "Satisfaction",
                "Bordeaux"
        );
        evalue.setTelephoneVerifie(true);
        Utilisateur savedEvalue = utilisateurRepository.save(evalue);

        // Créer 10 avis : 7 positifs (4-5 étoiles), 3 négatifs (1-3 étoiles)
        int[] notes = {5, 5, 5, 4, 4, 4, 4, 3, 2, 1};

        for (int i = 0; i < notes.length; i++) {
            Annonce annonce = new Annonce();
            annonce.setTitre("Objet Satisfaction " + i);
            annonce.setDescription("Description " + i);
            annonce.setTypeAnnonce(Annonce.TypeAnnonce.PRET);
            annonce.setCategorie("Test");
            annonce.setCreateur(savedEvalue);
            annonce.setDisponible(true);
            Annonce savedAnnonce = annonceRepository.save(annonce);

            DemandeEchange demande = new DemandeEchange();
            demande.setDemandeur(savedEvaluateur);
            demande.setDestinataire(savedEvalue);
            demande.setAnnonce(savedAnnonce);
            demande.setPropositionEchange("Prop " + i);
            demande.setMessageDemande("Message " + i);
            demande.setDateProposee(LocalDateTime.now().plusDays(1));
            demande.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);
            DemandeEchange savedDemande = demandeEchangeRepository.save(demande);

            AvisDto avisDto = new AvisDto();
            avisDto.setDemandeEchangeId(savedDemande.getId());
            avisDto.setNote(notes[i]);
            avisDto.setCommentaire("Commentaire satisfaction " + i);
            avisService.creerAvis(avisDto, savedEvaluateur);
        }

        // ---------- ACT ----------
        Double tauxSatisfaction = avisService.calculerTauxSatisfaction(savedEvalue);

        // ---------- ASSERT ----------
        // 7 avis positifs (4-5 étoiles) sur 10 = 70%
        assertEquals(70.0, tauxSatisfaction, 0.1);
    }

    // ============ TESTS DE VÉRIFICATIONS ============

    @Test
    void peutLaisserAvis_DoitRetournerTrue_QuandConditionsRemplies() {
        // ---------- ARRANGE ----------
        Utilisateur demandeur = new Utilisateur(
                "demandeur@verif.com",
                "password",
                "Demandeur",
                "Verif",
                "Toulouse"
        );
        demandeur.setTelephoneVerifie(true);
        Utilisateur savedDemandeur = utilisateurRepository.save(demandeur);

        Utilisateur destinataire = new Utilisateur(
                "destinataire@verif.com",
                "password",
                "Destinataire",
                "Verif",
                "Toulouse"
        );
        destinataire.setTelephoneVerifie(true);
        Utilisateur savedDestinataire = utilisateurRepository.save(destinataire);

        Annonce annonce = new Annonce();
        annonce.setTitre("Objet Vérification");
        annonce.setDescription("Description vérif");
        annonce.setTypeAnnonce(Annonce.TypeAnnonce.PRET);
        annonce.setCategorie("Test");
        annonce.setCreateur(savedDestinataire);
        annonce.setDisponible(true);
        Annonce savedAnnonce = annonceRepository.save(annonce);

        DemandeEchange demande = new DemandeEchange();
        demande.setDemandeur(savedDemandeur);
        demande.setDestinataire(savedDestinataire);
        demande.setAnnonce(savedAnnonce);
        demande.setPropositionEchange("Proposition vérif");
        demande.setMessageDemande("Message vérif");
        demande.setDateProposee(LocalDateTime.now().plusDays(1));
        demande.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);
        DemandeEchange savedDemande = demandeEchangeRepository.save(demande);

        // ---------- ACT ----------
        boolean peutLaisserAvis = avisService.peutLaisserAvis(savedDemande.getId(), savedDemandeur);

        // ---------- ASSERT ----------
        assertTrue(peutLaisserAvis);
    }

    @Test
    void peutLaisserAvis_DoitRetournerFalse_QuandDejaLaisseAvis() {
        // ---------- ARRANGE ----------
        Utilisateur demandeur = new Utilisateur(
                "demandeur@dejalaisse.com",
                "password",
                "Demandeur",
                "Déjà",
                "Nantes"
        );
        demandeur.setTelephoneVerifie(true);
        Utilisateur savedDemandeur = utilisateurRepository.save(demandeur);

        Utilisateur destinataire = new Utilisateur(
                "destinataire@dejalaisse.com",
                "password",
                "Destinataire",
                "Déjà",
                "Nantes"
        );
        destinataire.setTelephoneVerifie(true);
        Utilisateur savedDestinataire = utilisateurRepository.save(destinataire);

        Annonce annonce = new Annonce();
        annonce.setTitre("Objet Déjà");
        annonce.setDescription("Description déjà");
        annonce.setTypeAnnonce(Annonce.TypeAnnonce.PRET);
        annonce.setCategorie("Test");
        annonce.setCreateur(savedDestinataire);
        annonce.setDisponible(true);
        Annonce savedAnnonce = annonceRepository.save(annonce);

        DemandeEchange demande = new DemandeEchange();
        demande.setDemandeur(savedDemandeur);
        demande.setDestinataire(savedDestinataire);
        demande.setAnnonce(savedAnnonce);
        demande.setPropositionEchange("Proposition déjà");
        demande.setMessageDemande("Message déjà");
        demande.setDateProposee(LocalDateTime.now().plusDays(1));
        demande.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);
        DemandeEchange savedDemande = demandeEchangeRepository.save(demande);

        // Créer un avis
        AvisDto avisDto = new AvisDto();
        avisDto.setDemandeEchangeId(savedDemande.getId());
        avisDto.setNote(4);
        avisDto.setCommentaire("Premier avis déjà laissé");
        avisService.creerAvis(avisDto, savedDemandeur);

        // ---------- ACT ----------
        boolean peutLaisserAvis = avisService.peutLaisserAvis(savedDemande.getId(), savedDemandeur);

        // ---------- ASSERT ----------
        assertFalse(peutLaisserAvis);
    }

    // ============ TESTS DE CONSULTATION ============

    @Test
    void trouverAvisRecus_DoitRetournerListeAvis() {
        // ---------- ARRANGE ----------
        Utilisateur evaluateur = new Utilisateur(
                "eval@consultation.com",
                "password",
                "Évaluateur",
                "Consultation",
                "Strasbourg"
        );
        evaluateur.setTelephoneVerifie(true);
        Utilisateur savedEvaluateur = utilisateurRepository.save(evaluateur);

        Utilisateur evalue = new Utilisateur(
                "evalue@consultation.com",
                "password",
                "Évalué",
                "Consultation",
                "Strasbourg"
        );
        evalue.setTelephoneVerifie(true);
        Utilisateur savedEvalue = utilisateurRepository.save(evalue);

        // Créer 3 avis
        for (int i = 0; i < 3; i++) {
            Annonce annonce = new Annonce();
            annonce.setTitre("Objet Consultation " + i);
            annonce.setDescription("Description " + i);
            annonce.setTypeAnnonce(Annonce.TypeAnnonce.PRET);
            annonce.setCategorie("Test");
            annonce.setCreateur(savedEvalue);
            annonce.setDisponible(true);
            Annonce savedAnnonce = annonceRepository.save(annonce);

            DemandeEchange demande = new DemandeEchange();
            demande.setDemandeur(savedEvaluateur);
            demande.setDestinataire(savedEvalue);
            demande.setAnnonce(savedAnnonce);
            demande.setPropositionEchange("Prop " + i);
            demande.setMessageDemande("Msg " + i);
            demande.setDateProposee(LocalDateTime.now().plusDays(1));
            demande.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);
            DemandeEchange savedDemande = demandeEchangeRepository.save(demande);

            AvisDto avisDto = new AvisDto();
            avisDto.setDemandeEchangeId(savedDemande.getId());
            avisDto.setNote(4 + i % 2); // Notes 4 et 5
            avisDto.setCommentaire("Commentaire consultation " + i);
            avisService.creerAvis(avisDto, savedEvaluateur);
        }

        // ---------- ACT ----------
        List<Avis> avisRecus = avisService.trouverAvisRecus(savedEvalue);

        // ---------- ASSERT ----------
        assertNotNull(avisRecus);
        assertEquals(3, avisRecus.size());

        // Vérifier que tous les avis sont bien pour l'utilisateur évalué
        final Long evalueId = savedEvalue.getId(); // Variable finale pour le lambda
        avisRecus.forEach(avis -> {
            assertEquals(evalueId, avis.getEvalue().getId());
        });
    }
}