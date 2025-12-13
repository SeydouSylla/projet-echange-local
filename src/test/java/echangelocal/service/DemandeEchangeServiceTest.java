package echangelocal.service;

import echangelocal.dto.DemandeEchangeDto;
import echangelocal.dto.MessageDto;
import echangelocal.exception.DemandeEchangeNonTrouveeException;
import echangelocal.exception.OperationNonAutoriseeException;
import echangelocal.model.*;
import echangelocal.repository.*;
import echangelocal.service.impl.DemandeEchangeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DemandeEchangeServiceTest {

    private DemandeEchangeRepository demandeEchangeRepository;
    private MessageRepository messageRepository;
    private AnnonceRepository annonceRepository;
    private CompetenceRepository competenceRepository;
    private DemandeEchangeServiceImpl demandeEchangeService;

    private Utilisateur demandeur;
    private Utilisateur destinataire;
    private Annonce annonce;
    private Competence competence;
    private DemandeEchange demande;

    @BeforeEach
    void setUp() {
        demandeEchangeRepository = mock(DemandeEchangeRepository.class);
        messageRepository = mock(MessageRepository.class);
        annonceRepository = mock(AnnonceRepository.class);
        competenceRepository = mock(CompetenceRepository.class);

        demandeEchangeService = new DemandeEchangeServiceImpl(
                demandeEchangeRepository, messageRepository, annonceRepository, competenceRepository
        );

        // Création des utilisateurs
        demandeur = new Utilisateur();
        demandeur.setId(1L);
        demandeur.setEmail("demandeur@test.com");
        demandeur.setPrenom("Jean");
        demandeur.setNom("Demandeur");

        destinataire = new Utilisateur();
        destinataire.setId(2L);
        destinataire.setEmail("destinataire@test.com");
        destinataire.setPrenom("Marie");
        destinataire.setNom("Destinataire");

        // Création d'une annonce
        annonce = new Annonce();
        annonce.setId(1L);
        annonce.setTitre("Perceuse Bosch");
        annonce.setCreateur(destinataire);
        annonce.setDisponible(true);

        // Création d'une compétence
        competence = new Competence();
        competence.setId(1L);
        competence.setTitre("Cours de guitare");
        competence.setCreateur(destinataire);
        competence.setDisponible(true);

        // Création d'une demande d'échange
        demande = new DemandeEchange();
        demande.setId(1L);
        demande.setDemandeur(demandeur);
        demande.setDestinataire(destinataire);
        demande.setAnnonce(annonce);
        demande.setPropositionEchange("Je propose une tondeuse en échange");
        demande.setMessageDemande("Bonjour, je suis intéressé par votre perceuse");
        demande.setDateProposee(LocalDateTime.now().plusDays(2));
        demande.setStatut(DemandeEchange.StatutDemande.EN_ATTENTE);
    }

    @Test
    void creerDemandeEchange_DoitReussir_PourAnnonce() {
        // Arrange
        DemandeEchangeDto dto = new DemandeEchangeDto();
        dto.setAnnonceId(1L);
        dto.setPropositionEchange("Je propose une tondeuse");
        dto.setMessageDemande("Bonjour, je suis intéressé");
        dto.setDateProposee(LocalDateTime.now().plusDays(2));

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));
        when(demandeEchangeRepository.save(any(DemandeEchange.class))).thenAnswer(invocation -> {
            DemandeEchange demandeArg = invocation.getArgument(0);
            demandeArg.setId(1L); // Simuler l'ID généré
            return demandeArg;
        });
        when(demandeEchangeRepository.saveAndFlush(any(DemandeEchange.class))).thenAnswer(invocation -> {
            DemandeEchange demandeArg = invocation.getArgument(0);
            demandeArg.setId(1L);
            return demandeArg;
        });

        // Act
        DemandeEchange result = demandeEchangeService.creerDemandeEchange(dto, demandeur);

        // Assert
        assertNotNull(result);
        assertEquals(demandeur, result.getDemandeur());
        assertEquals(destinataire, result.getDestinataire());
        assertEquals(annonce, result.getAnnonce());
        assertEquals(DemandeEchange.StatutDemande.EN_ATTENTE, result.getStatut());
        verify(demandeEchangeRepository, times(1)).saveAndFlush(any(DemandeEchange.class));
    }

    @Test
    void creerDemandeEchange_DoitEchouer_QuandAnnonceIndisponible() {
        // Arrange
        annonce.setDisponible(false);
        DemandeEchangeDto dto = new DemandeEchangeDto();
        dto.setAnnonceId(1L);
        dto.setDateProposee(LocalDateTime.now().plusDays(2));

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            demandeEchangeService.creerDemandeEchange(dto, demandeur);
        });
    }

    @Test
    void accepterDemande_DoitReussir_QuandDestinataireAutorise() {
        // Arrange
        DemandeEchange demandeAcceptee = new DemandeEchange();
        demandeAcceptee.setId(1L);
        demandeAcceptee.setDemandeur(demandeur);
        demandeAcceptee.setDestinataire(destinataire);
        demandeAcceptee.setAnnonce(annonce);
        demandeAcceptee.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);

        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(demandeEchangeRepository.saveAndFlush(any(DemandeEchange.class))).thenReturn(demandeAcceptee);

        // Act
        DemandeEchange result = demandeEchangeService.accepterDemande(1L, destinataire);

        // Assert
        assertNotNull(result);
        assertEquals(DemandeEchange.StatutDemande.ACCEPTEE, result.getStatut());
        verify(demandeEchangeRepository, times(1)).saveAndFlush(any(DemandeEchange.class));
    }

    @Test
    void accepterDemande_DoitEchouer_QuandPasDestinataire() {
        // Arrange
        Utilisateur autreUtilisateur = new Utilisateur();
        autreUtilisateur.setId(3L);

        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demande));

        // Act & Assert
        assertThrows(OperationNonAutoriseeException.class, () -> {
            demandeEchangeService.accepterDemande(1L, autreUtilisateur);
        });
    }

    @Test
    void envoyerMessage_DoitReussir_QuandEchangeAccepte() {
        // Arrange
        demande.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);
        MessageDto messageDto = new MessageDto();
        messageDto.setDemandeEchangeId(1L);
        messageDto.setContenu("Bonjour, on se voit à 14h ?");

        Message message = new Message();
        message.setId(1L);
        message.setContenu("Bonjour, on se voit à 14h ?");
        message.setExpediteur(demandeur);
        message.setDestinataire(destinataire);
        message.setDemandeEchange(demande);

        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageRepository.saveAndFlush(any(Message.class))).thenReturn(message);

        // Act
        Message result = demandeEchangeService.envoyerMessage(messageDto, demandeur);

        // Assert
        assertNotNull(result);
        assertEquals("Bonjour, on se voit à 14h ?", result.getContenu());
        verify(messageRepository, times(1)).saveAndFlush(any(Message.class));
    }

    @Test
    void trouverEchangesActifs_DoitRetournerListe() {
        // Arrange
        demande.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);
        when(demandeEchangeRepository.findEchangesActifs(demandeur))
                .thenReturn(Arrays.asList(demande));

        // Act
        List<DemandeEchange> result = demandeEchangeService.trouverEchangesActifs(demandeur);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(DemandeEchange.StatutDemande.ACCEPTEE, result.get(0).getStatut());
    }

    @Test
    void trouverDemandesRecues_DoitRetournerListe() {
        // Arrange
        when(demandeEchangeRepository.findByDestinataireOrderByDateCreationDesc(destinataire))
                .thenReturn(Arrays.asList(demande));

        // Act
        List<DemandeEchange> result = demandeEchangeService.trouverDemandesRecues(destinataire);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(destinataire, result.get(0).getDestinataire());
    }

    @Test
    void compterDemandesEnAttente_DoitRetournerNombre() {
        // Arrange
        when(demandeEchangeRepository.countDemandesEnAttente(destinataire))
                .thenReturn(5L);

        // Act
        long result = demandeEchangeService.compterDemandesEnAttente(destinataire);

        // Assert
        assertEquals(5L, result);
    }

    @Test
    void estDestinataire_DoitRetournerVrai_QuandUtilisateurEstDestinataire() {
        // Arrange
        when(demandeEchangeRepository.findByIdAndDestinataire(1L, destinataire))
                .thenReturn(Optional.of(demande));

        // Act
        boolean result = demandeEchangeService.estDestinataire(1L, destinataire);

        // Assert
        assertTrue(result);
    }

    @Test
    void estDestinataire_DoitRetournerFaux_QuandUtilisateurNestPasDestinataire() {
        // Arrange
        Utilisateur autreUtilisateur = new Utilisateur();
        autreUtilisateur.setId(3L);
        when(demandeEchangeRepository.findByIdAndDestinataire(1L, autreUtilisateur))
                .thenReturn(Optional.empty());

        // Act
        boolean result = demandeEchangeService.estDestinataire(1L, autreUtilisateur);

        // Assert
        assertFalse(result);
    }

    @Test
    void peutEnvoyerMessage_DoitRetournerVrai_QuandEchangeAccepteEtUtilisateurEstPartie() {
        // Arrange
        demande.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);
        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demande));

        // Act
        boolean result = demandeEchangeService.peutEnvoyerMessage(1L, demandeur);

        // Assert
        assertTrue(result);
    }

    @Test
    void peutEnvoyerMessage_DoitRetournerFaux_QuandEchangePasAccepte() {
        // Arrange
        demande.setStatut(DemandeEchange.StatutDemande.EN_ATTENTE);
        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demande));

        // Act
        boolean result = demandeEchangeService.peutEnvoyerMessage(1L, demandeur);

        // Assert
        assertFalse(result);
    }

    @Test
    void refuserDemande_DoitReussir_QuandDestinataireAutorise() {
        // Arrange
        DemandeEchange demandeRefusee = new DemandeEchange();
        demandeRefusee.setId(1L);
        demandeRefusee.setStatut(DemandeEchange.StatutDemande.REFUSEE);

        when(demandeEchangeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(demandeEchangeRepository.saveAndFlush(any(DemandeEchange.class))).thenReturn(demandeRefusee);

        // Act
        DemandeEchange result = demandeEchangeService.refuserDemande(1L, destinataire);

        // Assert
        assertNotNull(result);
        assertEquals(DemandeEchange.StatutDemande.REFUSEE, result.getStatut());
    }

    @Test
    void annulerDemande_DoitReussir_QuandDemandeurAutorise() {
        // Arrange
        when(demandeEchangeRepository.findByIdAndDemandeur(1L, demandeur))
                .thenReturn(Optional.of(demande));

        // Act
        assertDoesNotThrow(() -> demandeEchangeService.annulerDemande(1L, demandeur));

        // Assert
        verify(demandeEchangeRepository, times(1)).saveAndFlush(any(DemandeEchange.class));
    }
}