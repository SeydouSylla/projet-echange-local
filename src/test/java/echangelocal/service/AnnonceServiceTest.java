package echangelocal.service;

import echangelocal.config.ApplicationProperties;
import echangelocal.dto.AnnonceDto;
import echangelocal.exception.AnnonceNonTrouveeException;
import echangelocal.exception.OperationNonAutoriseeException;
import echangelocal.model.Annonce;
import echangelocal.model.Utilisateur;
import echangelocal.repository.AnnonceRepository;
import echangelocal.service.impl.AnnonceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnonceServiceTest {

    private AnnonceRepository annonceRepository;
    private ApplicationProperties applicationProperties;
    private AnnonceServiceImpl annonceService;

    private Utilisateur utilisateur;
    private Annonce annonce;

    @BeforeEach
    void setUp() {
        annonceRepository = mock(AnnonceRepository.class);
        applicationProperties = new ApplicationProperties();
        annonceService = new AnnonceServiceImpl(annonceRepository, applicationProperties);

        // Création d'un utilisateur de test
        utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setEmail("test@example.com");
        utilisateur.setPrenom("Jean");
        utilisateur.setNom("Dupont");

        // Création d'une annonce de test
        annonce = new Annonce();
        annonce.setId(1L);
        annonce.setTitre("Perceuse Bosch");
        annonce.setDescription("Perceuse Bosch en parfait état");
        annonce.setCategorie("Outillage");
        annonce.setTypeAnnonce(Annonce.TypeAnnonce.PRET);
        annonce.setCreateur(utilisateur);
    }

    @Test
    void creerAnnonce_DoitReussir_QuandDonneesValides() throws IOException { // AJOUT: throws IOException
        // Arrange
        AnnonceDto annonceDto = new AnnonceDto();
        annonceDto.setTitre("Perceuse Bosch");
        annonceDto.setDescription("Perceuse Bosch en parfait état");
        annonceDto.setCategorie("Outillage");
        annonceDto.setTypeAnnonce(Annonce.TypeAnnonce.PRET);

        when(annonceRepository.save(any(Annonce.class))).thenReturn(annonce);

        // Act
        Annonce result = annonceService.creerAnnonce(annonceDto, utilisateur);

        // Assert
        assertNotNull(result);
        assertEquals("Perceuse Bosch", result.getTitre());
        assertEquals(utilisateur, result.getCreateur());
        verify(annonceRepository).save(any(Annonce.class));
    }

    @Test
    void trouverParId_DoitRetournerAnnonce_QuandAnnonceExiste() {
        // Arrange
        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        // Act
        Optional<Annonce> result = annonceService.trouverParId(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(annonce.getId(), result.get().getId());
    }

    @Test
    void trouverParId_DoitRetournerVide_QuandAnnonceNExistePas() {
        // Arrange
        when(annonceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Annonce> result = annonceService.trouverParId(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void estProprietaireAnnonce_DoitRetournerVrai_QuandUtilisateurEstProprietaire() {
        // Arrange
        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        // Act
        boolean result = annonceService.estProprietaireAnnonce(1L, utilisateur);

        // Assert
        assertTrue(result);
    }

    @Test
    void estProprietaireAnnonce_DoitRetournerFaux_QuandUtilisateurNEstPasProprietaire() {
        // Arrange
        Utilisateur autreUtilisateur = new Utilisateur();
        autreUtilisateur.setId(2L);

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        // Act
        boolean result = annonceService.estProprietaireAnnonce(1L, autreUtilisateur);

        // Assert
        assertFalse(result);
    }

    @Test
    void supprimerAnnonce_DoitReussir_QuandUtilisateurEstProprietaire() {
        // Arrange
        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        // Act
        annonceService.supprimerAnnonce(1L, utilisateur);

        // Assert
        verify(annonceRepository).delete(annonce);
    }

    @Test
    void supprimerAnnonce_DoitEchouer_QuandUtilisateurNEstPasProprietaire() {
        // Arrange
        Utilisateur autreUtilisateur = new Utilisateur();
        autreUtilisateur.setId(2L);

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        // Act & Assert
        assertThrows(OperationNonAutoriseeException.class, () -> {
            annonceService.supprimerAnnonce(1L, autreUtilisateur);
        });
    }

    @Test
    void rechercherAnnonces_DoitRetournerAnnoncesFiltrees() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Annonce> pageAnnonces = new PageImpl<>(Arrays.asList(annonce));

        when(annonceRepository.rechercherParMotsCles("perceuse", pageable))
                .thenReturn(pageAnnonces);

        // Act
        Page result = annonceService.rechercherAnnonces("perceuse", null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getCategoriesPopulaires_DoitRetournerListeCategories() {
        // Act
        List<String> categories = annonceService.getCategoriesPopulaires();

        // Assert
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
        assertTrue(categories.contains("Outillage"));
    }
}