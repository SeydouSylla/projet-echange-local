package echangelocal.service;

import echangelocal.config.ApplicationProperties;
import echangelocal.dto.InscriptionDto;
import echangelocal.exception.UtilisateurExistantException;
import echangelocal.model.Utilisateur;
import echangelocal.repository.UtilisateurRepository;
import echangelocal.service.impl.UtilisateurServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UtilisateurServiceTest {

    private UtilisateurRepository utilisateurRepository;
    private PasswordEncoder passwordEncoder;
    private ApplicationProperties applicationProperties;
    private UtilisateurServiceImpl utilisateurService;

    @BeforeEach
    void setUp() {
        utilisateurRepository = mock(UtilisateurRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        // ANCIEN : applicationProperties = new ApplicationProperties();
        // NOUVEAU :
        String testUploadDir = "uploads";
        utilisateurService = new UtilisateurServiceImpl(utilisateurRepository, passwordEncoder, testUploadDir);
    }

    @Test
    void inscrireUtilisateur_DoitReussir_QuandEmailNExistePas() {
        // Arrange
        InscriptionDto inscriptionDto = new InscriptionDto(
                "test@example.com",
                "password123",
                "Jean",
                "Dupont",
                "Paris"
        );

        when(utilisateurRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Utilisateur result = utilisateurService.inscrireUtilisateur(inscriptionDto);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Jean", result.getPrenom());
        assertEquals("Dupont", result.getNom());
        assertEquals("Paris", result.getLocalisation());
        assertTrue(passwordEncoder.matches("password123", result.getMotDePasse()));

        verify(utilisateurRepository).existsByEmail("test@example.com");
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    void inscrireUtilisateur_DoitEchouer_QuandEmailExisteDeja() {
        // Arrange
        InscriptionDto inscriptionDto = new InscriptionDto(
                "existant@example.com",
                "password123",
                "Jean",
                "Dupont",
                "Paris"
        );

        when(utilisateurRepository.existsByEmail("existant@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(UtilisateurExistantException.class, () -> {
            utilisateurService.inscrireUtilisateur(inscriptionDto);
        });

        verify(utilisateurRepository).existsByEmail("existant@example.com");
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void verifierMotDePasse_DoitRetournerVrai_QuandMotDePasseCorrect() {
        // Arrange
        String motDePasseClair = "password123";
        String motDePasseCrypte = passwordEncoder.encode(motDePasseClair);

        // Act
        boolean result = utilisateurService.verifierMotDePasse(motDePasseClair, motDePasseCrypte);

        // Assert
        assertTrue(result);
    }

    @Test
    void verifierMotDePasse_DoitRetournerFaux_QuandMotDePasseIncorrect() {
        // Arrange
        String motDePasseClair = "password123";
        String motDePasseCrypte = passwordEncoder.encode("differentPassword");

        // Act
        boolean result = utilisateurService.verifierMotDePasse(motDePasseClair, motDePasseCrypte);

        // Assert
        assertFalse(result);
    }
}