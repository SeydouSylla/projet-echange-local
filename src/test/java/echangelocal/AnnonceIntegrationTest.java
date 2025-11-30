package echangelocal;

import echangelocal.dto.AnnonceDto;
import echangelocal.model.Annonce;
import echangelocal.model.Utilisateur;
import echangelocal.repository.AnnonceRepository;
import echangelocal.repository.UtilisateurRepository;
import echangelocal.service.interfaces.AnnonceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
class AnnonceIntegrationTest {

    @Autowired
    private AnnonceService annonceService;

    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void integrationComplete_CreationEtRechercheAnnonce() throws IOException { // AJOUT: throws IOException
        // Arrange - Créer un utilisateur
        Utilisateur utilisateur = new Utilisateur("test@annonce.com", "password",
                "Test", "Annonce", "Paris");
        Utilisateur utilisateurSauvegarde = utilisateurRepository.save(utilisateur);

        // Arrange - Créer un DTO d'annonce
        AnnonceDto annonceDto = new AnnonceDto();
        annonceDto.setTitre("Perceuse professionnelle");
        annonceDto.setDescription("Perceuse Bosch en parfait état, peu utilisée");
        annonceDto.setCategorie("Outillage");
        annonceDto.setTypeAnnonce(Annonce.TypeAnnonce.PRET);
        annonceDto.setCommentaireEchange("Je recherche des outils de jardinage");

        // Act - Créer l'annonce
        Annonce annonceCree = annonceService.creerAnnonce(annonceDto, utilisateurSauvegarde);

        // Assert - Vérifier la création
        assertNotNull(annonceCree.getId());
        assertEquals("Perceuse professionnelle", annonceCree.getTitre());
        assertEquals(utilisateurSauvegarde.getId(), annonceCree.getCreateur().getId());

        // Act - Rechercher les annonces de l'utilisateur
        List<Annonce> annoncesUtilisateur = annonceService.trouverAnnoncesParUtilisateur(utilisateurSauvegarde);

        // Assert - Vérifier la recherche
        assertFalse(annoncesUtilisateur.isEmpty());
        assertEquals(annonceCree.getId(), annoncesUtilisateur.get(0).getId());
    }

    @Test
    void rechercherAnnonces_ParCategorie_DoitRetournerResultatsFiltres() throws IOException { // AJOUT: throws IOException
        // Arrange - Créer des annonces de test
        Utilisateur utilisateur = utilisateurRepository.save(
                new Utilisateur("recherche@test.com", "password", "Recherche", "Test", "Lyon")
        );

        AnnonceDto annonce1 = new AnnonceDto();
        annonce1.setTitre("Perceuse Bosch");
        annonce1.setDescription("Perceuse professionnelle");
        annonce1.setCategorie("Outillage");
        annonce1.setTypeAnnonce(Annonce.TypeAnnonce.PRET);

        AnnonceDto annonce2 = new AnnonceDto();
        annonce2.setTitre("Tondeuse à gazon");
        annonce2.setDescription("Tondeuse électrique");
        annonce2.setCategorie("Jardinage");
        annonce2.setTypeAnnonce(Annonce.TypeAnnonce.ECHANGE);

        annonceService.creerAnnonce(annonce1, utilisateur);
        annonceService.creerAnnonce(annonce2, utilisateur);

        // Act - Rechercher par catégorie
        var resultats = annonceService.rechercherAnnonces(null, "Outillage",
                PageRequest.of(0, 10));

        // Assert
        assertTrue(resultats.getTotalElements() >= 1);
    }
}