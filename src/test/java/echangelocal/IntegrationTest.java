package echangelocal;

import echangelocal.dto.InscriptionDto;
import echangelocal.model.Utilisateur;
import echangelocal.repository.UtilisateurRepository;
import echangelocal.service.interfaces.UtilisateurService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class IntegrationTest {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    @Commit // <-- valide la transaction au lieu de rollback
    void integrationComplete_InscriptionEtRechercheUtilisateur() {
        // Arrange
        InscriptionDto inscriptionDto = new InscriptionDto(
                "integration@test.com",
                "password123",
                "Integration",
                "Test",
                "Lyon"
        );

        // Act - Inscription
        Utilisateur utilisateurInscrit = utilisateurService.inscrireUtilisateur(inscriptionDto);

        // Assert - Vérification de l'inscription
        assertNotNull(utilisateurInscrit.getId());
        assertEquals("integration@test.com", utilisateurInscrit.getEmail());
        assertEquals("Integration", utilisateurInscrit.getPrenom());
        assertEquals("Test", utilisateurInscrit.getNom());

        // Act - Recherche par email
        Optional<Utilisateur> utilisateurTrouve = utilisateurService.trouverParEmail("integration@test.com");

        // Assert - Vérification de la recherche
        assertTrue(utilisateurTrouve.isPresent());
        assertEquals(utilisateurInscrit.getId(), utilisateurTrouve.get().getId());
        assertEquals("Integration Test", utilisateurTrouve.get().getNomComplet());
    }
}
