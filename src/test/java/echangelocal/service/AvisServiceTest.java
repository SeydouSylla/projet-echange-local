package echangelocal.service;

import echangelocal.dto.AvisDto;
import echangelocal.exception.AvisException;
import echangelocal.exception.OperationNonAutoriseeException;
import echangelocal.model.*;
import echangelocal.repository.AvisRepository;
import echangelocal.repository.DemandeEchangeRepository;
import echangelocal.service.impl.AvisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le service d'avis
 * Respecte les bonnes pratiques de test (AAA pattern)
 */
class AvisServiceTest {

    private AvisRepository avisRepository;
    private DemandeEchangeRepository demandeEchangeRepository;
    private AvisServiceImpl avisService;

    private Utilisateur demandeur;
    private Utilisateur destinataire;
    private DemandeEchange demandeAcceptee;
    private Avis avis;

    @BeforeEach
    void setUp() {
        // Mocks
        avisRepository = mock(AvisRepository.class);
        demandeEchangeRepository = mock(DemandeEchangeRepository.class);

        avisService = new AvisServiceImpl(avisRepository, demandeEchangeRepository);

        // Données de test
        demandeur = new Utilisateur();
        demandeur.setId(1L);
        demandeur.setEmail("demandeur@test.com");
        demandeur.setPrenom("Jean");
        demandeur.setNom("Dupont");

        destinataire = new Utilisateur();
        destinataire.setId(2L);
        destinataire.setEmail("destinataire@test.com");
        destinataire.setPrenom("Marie");
        destinataire.setNom("Martin");

        Annonce annonce = new Annonce();
        annonce.setId(1L);
        annonce.setTitre("Perceuse");
        annonce.setCreateur(destinataire);

        demandeAcceptee = new DemandeEchange();
        demandeAcceptee.setId(1L);
        demandeAcceptee.setDemandeur(demandeur);
        demandeAcceptee.setDestinataire(destinataire);
        demandeAcceptee.setAnnonce(annonce);
        demandeAcceptee.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);

        avis = new Avis();
        avis.setId(1L);
        avis.setDemandeEchange(demandeAcceptee);
        avis.setEvaluateur(demandeur);
        avis.setEvalue(destinataire);
        avis.setNote(5);
        avis.setCommentaire("Excellent échange, personne ponctuelle et sympathique !");
    }

    @Test
    void creerAvis_DoitReussir_QuandDonneesValides() {
        // Arrange
        AvisDto avisDto = new AvisDto();
        avisDto.setDemandeEchangeId(1L);
        avisDto.setNote(5);
        avisDto.setCommentaire("Excellent échange, très satisfait !");

        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demandeAcceptee));
        when(avisRepository.existsByDemandeEchangeAndEvaluateur(demandeAcceptee, demandeur)).thenReturn(false);
        when(avisRepository.saveAndFlush(any(Avis.class))).thenReturn(avis);
        when(avisRepository.compterAvisParDemande(demandeAcceptee)).thenReturn(1L);

        // Act
        Avis result = avisService.creerAvis(avisDto, demandeur);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getNote());
        verify(avisRepository).saveAndFlush(any(Avis.class));
    }

    @Test
    void creerAvis_DoitEchouer_QuandDemandeNonAcceptee() {
        // Arrange
        demandeAcceptee.setStatut(DemandeEchange.StatutDemande.EN_ATTENTE);
        AvisDto avisDto = new AvisDto();
        avisDto.setDemandeEchangeId(1L);
        avisDto.setNote(5);
        avisDto.setCommentaire("Excellent échange");

        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demandeAcceptee));

        // Act & Assert
        assertThrows(AvisException.class, () -> {
            avisService.creerAvis(avisDto, demandeur);
        });
    }

    @Test
    void creerAvis_DoitEchouer_QuandAvisDejaExiste() {
        // Arrange
        AvisDto avisDto = new AvisDto();
        avisDto.setDemandeEchangeId(1L);
        avisDto.setNote(5);
        avisDto.setCommentaire("Excellent échange");

        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demandeAcceptee));
        when(avisRepository.existsByDemandeEchangeAndEvaluateur(demandeAcceptee, demandeur)).thenReturn(true);

        // Act & Assert
        assertThrows(AvisException.class, () -> {
            avisService.creerAvis(avisDto, demandeur);
        });
    }

    @Test
    void creerAvis_DoitEchouer_QuandUtilisateurNonParticipant() {
        // Arrange
        Utilisateur intrus = new Utilisateur();
        intrus.setId(3L);

        AvisDto avisDto = new AvisDto();
        avisDto.setDemandeEchangeId(1L);
        avisDto.setNote(5);
        avisDto.setCommentaire("Excellent échange");

        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demandeAcceptee));

        // Act & Assert
        assertThrows(OperationNonAutoriseeException.class, () -> {
            avisService.creerAvis(avisDto, intrus);
        });
    }

    @Test
    void calculerNoteMoyenne_DoitRetournerMoyenneCorrecte() {
        // Arrange
        when(avisRepository.calculerNoteMoyenne(destinataire)).thenReturn(4.5);

        // Act
        Double moyenne = avisService.calculerNoteMoyenne(destinataire);

        // Assert
        assertEquals(4.5, moyenne);
    }

    @Test
    void calculerNoteMoyenne_DoitRetournerZero_QuandAucunAvis() {
        // Arrange
        when(avisRepository.calculerNoteMoyenne(destinataire)).thenReturn(null);

        // Act
        Double moyenne = avisService.calculerNoteMoyenne(destinataire);

        // Assert
        assertEquals(0.0, moyenne);
    }

    @Test
    void obtenirStatistiques_DoitRetournerStatistiquesCompletes() {
        // Arrange
        when(avisRepository.calculerNoteMoyenne(destinataire)).thenReturn(4.5);
        when(avisRepository.compterAvisRecus(destinataire)).thenReturn(10L);
        when(avisRepository.compterAvisParNote(destinataire, 1)).thenReturn(0L);
        when(avisRepository.compterAvisParNote(destinataire, 2)).thenReturn(1L);
        when(avisRepository.compterAvisParNote(destinataire, 3)).thenReturn(2L);
        when(avisRepository.compterAvisParNote(destinataire, 4)).thenReturn(3L);
        when(avisRepository.compterAvisParNote(destinataire, 5)).thenReturn(4L);

        // Act
        Map<String, Object> stats = avisService.obtenirStatistiques(destinataire);

        // Assert
        assertNotNull(stats);
        assertEquals(4.5, stats.get("noteMoyenne"));
        assertEquals(10L, stats.get("totalAvis"));
        assertEquals(70.0, stats.get("tauxSatisfaction")); // (3+4)/10 * 100 = 70%
    }

    @Test
    void peutLaisserAvis_DoitRetournerTrue_QuandConditionsRemplies() {
        // Arrange
        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demandeAcceptee));
        when(avisRepository.existsByDemandeEchangeAndEvaluateur(demandeAcceptee, demandeur)).thenReturn(false);

        // Act
        boolean result = avisService.peutLaisserAvis(1L, demandeur);

        // Assert
        assertTrue(result);
    }

    @Test
    void peutLaisserAvis_DoitRetournerFalse_QuandDejaLaisseAvis() {
        // Arrange
        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demandeAcceptee));
        when(avisRepository.existsByDemandeEchangeAndEvaluateur(demandeAcceptee, demandeur)).thenReturn(true);

        // Act
        boolean result = avisService.peutLaisserAvis(1L, demandeur);

        // Assert
        assertFalse(result);
    }

    @Test
    void modifierAvis_DoitReussir_DansLes24Heures() {
        // Arrange
        AvisDto avisDto = new AvisDto();
        avisDto.setNote(4);
        avisDto.setCommentaire("Commentaire modifié");

        when(avisRepository.findById(1L)).thenReturn(Optional.of(avis));
        when(avisRepository.saveAndFlush(any(Avis.class))).thenReturn(avis);

        // Act
        Avis result = avisService.modifierAvis(1L, avisDto, demandeur);

        // Assert
        assertNotNull(result);
        verify(avisRepository).saveAndFlush(any(Avis.class));
    }

    @Test
    void modifierAvis_DoitEchouer_QuandNonAuteur() {
        // Arrange
        AvisDto avisDto = new AvisDto();
        avisDto.setNote(4);
        avisDto.setCommentaire("Modification non autorisée");

        when(avisRepository.findById(1L)).thenReturn(Optional.of(avis));

        // Act & Assert
        assertThrows(OperationNonAutoriseeException.class, () -> {
            avisService.modifierAvis(1L, avisDto, destinataire);
        });
    }

    @Test
    void trouverAvisRecus_DoitRetournerListeAvis() {
        // Arrange
        List<Avis> avisListe = Arrays.asList(avis);
        when(avisRepository.findByEvalue(destinataire)).thenReturn(avisListe);

        // Act
        List<Avis> result = avisService.trouverAvisRecus(destinataire);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void calculerTauxSatisfaction_DoitRetournerTauxCorrect() {
        // Arrange
        when(avisRepository.compterAvisRecus(destinataire)).thenReturn(10L);
        when(avisRepository.compterAvisParNote(destinataire, 4)).thenReturn(3L);
        when(avisRepository.compterAvisParNote(destinataire, 5)).thenReturn(4L);

        // Act
        Double taux = avisService.calculerTauxSatisfaction(destinataire);

        // Assert
        assertEquals(70.0, taux); // (3+4)/10 * 100 = 70%
    }
}