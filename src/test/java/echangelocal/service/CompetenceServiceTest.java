package echangelocal.service;

import echangelocal.dto.CompetenceDto;
import echangelocal.exception.CompetenceNonTrouveeException;
import echangelocal.exception.OperationNonAutoriseeException;
import echangelocal.model.Competence;
import echangelocal.model.Utilisateur;
import echangelocal.repository.CompetenceRepository;
import echangelocal.service.impl.CompetenceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CompetenceServiceTest {

    private CompetenceRepository competenceRepository;
    private CompetenceServiceImpl competenceService;

    private Utilisateur utilisateur;
    private Competence competence;

    @BeforeEach
    void setUp() {
        competenceRepository = mock(CompetenceRepository.class);
        competenceService = new CompetenceServiceImpl(competenceRepository);

        // Création d'un utilisateur de test
        utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setEmail("test@example.com");
        utilisateur.setPrenom("Jean");
        utilisateur.setNom("Dupont");
        utilisateur.setLocalisation("Paris");

        // Création d'une compétence de test
        competence = new Competence();
        competence.setId(1L);
        competence.setTitre("Cours de guitare");
        competence.setDescription("Cours de guitare pour débutants");
        competence.setCategorie("Musique");
        competence.setDisponibilites(Arrays.asList("Week-end", "Soirées"));
        competence.setCreateur(utilisateur);
        competence.setDisponible(true);
    }

    @Test
    void creerCompetence_DoitReussir_QuandDonneesValides() {
        // Arrange
        CompetenceDto competenceDto = new CompetenceDto();
        competenceDto.setTitre("Cours de guitare");
        competenceDto.setDescription("Cours de guitare pour débutants");
        competenceDto.setCategorie("Musique");
        competenceDto.setDisponibilites(Arrays.asList("Week-end", "Soirées"));
        competenceDto.setDisponible(true);

        when(competenceRepository.save(any(Competence.class))).thenReturn(competence);

        // Act
        Competence result = competenceService.creerCompetence(competenceDto, utilisateur);

        // Assert
        assertNotNull(result);
        assertEquals("Cours de guitare", result.getTitre());
        assertEquals(utilisateur, result.getCreateur());
        assertEquals(2, result.getDisponibilites().size());
        verify(competenceRepository).save(any(Competence.class));
    }

    @Test
    void trouverParId_DoitRetournerCompetence_QuandCompetenceExiste() {
        // Arrange
        when(competenceRepository.findById(1L)).thenReturn(Optional.of(competence));

        // Act
        Optional<Competence> result = competenceService.trouverParId(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(competence.getId(), result.get().getId());
        assertEquals("Cours de guitare", result.get().getTitre());
    }

    @Test
    void trouverParId_DoitRetournerVide_QuandCompetenceNExistePas() {
        // Arrange
        when(competenceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Competence> result = competenceService.trouverParId(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void estProprietaireCompetence_DoitRetournerVrai_QuandUtilisateurEstProprietaire() {
        // Arrange
        when(competenceRepository.findById(1L)).thenReturn(Optional.of(competence));

        // Act
        boolean result = competenceService.estProprietaireCompetence(1L, utilisateur);

        // Assert
        assertTrue(result);
    }

    @Test
    void estProprietaireCompetence_DoitRetournerFaux_QuandUtilisateurNEstPasProprietaire() {
        // Arrange
        Utilisateur autreUtilisateur = new Utilisateur();
        autreUtilisateur.setId(2L);

        when(competenceRepository.findById(1L)).thenReturn(Optional.of(competence));

        // Act
        boolean result = competenceService.estProprietaireCompetence(1L, autreUtilisateur);

        // Assert
        assertFalse(result);
    }

    @Test
    void supprimerCompetence_DoitReussir_QuandUtilisateurEstProprietaire() {
        // Arrange
        when(competenceRepository.findById(1L)).thenReturn(Optional.of(competence));

        // Act
        competenceService.supprimerCompetence(1L, utilisateur);

        // Assert
        verify(competenceRepository).delete(competence);
    }

    @Test
    void supprimerCompetence_DoitEchouer_QuandUtilisateurNEstPasProprietaire() {
        // Arrange
        Utilisateur autreUtilisateur = new Utilisateur();
        autreUtilisateur.setId(2L);

        when(competenceRepository.findById(1L)).thenReturn(Optional.of(competence));

        // Act & Assert
        assertThrows(OperationNonAutoriseeException.class, () -> {
            competenceService.supprimerCompetence(1L, autreUtilisateur);
        });
    }

    @Test
    void supprimerCompetence_DoitEchouer_QuandCompetenceNExistePas() {
        // Arrange
        when(competenceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CompetenceNonTrouveeException.class, () -> {
            competenceService.supprimerCompetence(999L, utilisateur);
        });
    }

    @Test
    void trouverCompetencesParUtilisateur_DoitRetournerListeCompetences() {
        // Arrange
        List<Competence> competences = Arrays.asList(competence);
        when(competenceRepository.findByCreateurOrderByDateCreationDesc(utilisateur))
                .thenReturn(competences);

        // Act
        List<Competence> result = competenceService.trouverCompetencesParUtilisateur(utilisateur);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(competence.getId(), result.get(0).getId());
    }

    @Test
    void trouverCompetencesDisponibles_DoitRetournerPageCompetences() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Competence> pageCompetences = new PageImpl<>(Arrays.asList(competence));
        when(competenceRepository.findByDisponibleTrueOrderByDateCreationDesc(pageable))
                .thenReturn(pageCompetences);

        // Act
        var result = competenceService.trouverCompetencesDisponibles(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void rechercherCompetences_DoitRetournerCompetencesFiltreesParTitre() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Competence> pageCompetences = new PageImpl<>(Arrays.asList(competence));

        when(competenceRepository.rechercherAvancee("guitare", null, null, pageable))
                .thenReturn(pageCompetences);

        // Act
        var result = competenceService.rechercherCompetences("guitare", null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void rechercherCompetences_DoitRetournerCompetencesFiltreesParCategorie() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Competence> pageCompetences = new PageImpl<>(Arrays.asList(competence));

        when(competenceRepository.rechercherAvancee(null, "Musique", null, pageable))
                .thenReturn(pageCompetences);

        // Act
        var result = competenceService.rechercherCompetences(null, "Musique", null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void rechercherCompetences_DoitRetournerCompetencesFiltreesParLocalisation() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Competence> pageCompetences = new PageImpl<>(Arrays.asList(competence));

        when(competenceRepository.rechercherAvancee(null, null, "Paris", pageable))
                .thenReturn(pageCompetences);

        // Act
        var result = competenceService.rechercherCompetences(null, null, "Paris", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getCategoriesPopulaires_DoitRetournerListeCategories() {
        // Act
        List<String> categories = competenceService.getCategoriesPopulaires();

        // Assert
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
        assertTrue(categories.contains("Musique"));
        assertTrue(categories.contains("Jardinage"));
        assertTrue(categories.contains("Bricolage"));
        assertTrue(categories.contains("Cuisine"));
        assertTrue(categories.contains("Informatique"));
    }

    @Test
    void getDisponibilitesPredefinies_DoitRetournerListeDisponibilites() {
        // Act
        List<String> disponibilites = competenceService.getDisponibilitesPredefinies();

        // Assert
        assertNotNull(disponibilites);
        assertFalse(disponibilites.isEmpty());
        assertTrue(disponibilites.contains("Week-ends"));
        assertTrue(disponibilites.contains("Soirées"));
        assertTrue(disponibilites.contains("Journées"));
    }

    @Test
    void modifierCompetence_DoitReussir_QuandUtilisateurEstProprietaire() {
        // Arrange
        CompetenceDto competenceDto = new CompetenceDto();
        competenceDto.setTitre("Cours de guitare modifié");
        competenceDto.setDescription("Nouvelle description");
        competenceDto.setCategorie("Musique");
        competenceDto.setDisponibilites(Arrays.asList("Week-end"));
        competenceDto.setDisponible(true);

        when(competenceRepository.findById(1L)).thenReturn(Optional.of(competence));
        when(competenceRepository.save(any(Competence.class))).thenReturn(competence);

        // Act
        Competence result = competenceService.modifierCompetence(1L, competenceDto, utilisateur);

        // Assert
        assertNotNull(result);
        verify(competenceRepository).save(any(Competence.class));
    }

    @Test
    void modifierCompetence_DoitEchouer_QuandUtilisateurNEstPasProprietaire() {
        // Arrange
        CompetenceDto competenceDto = new CompetenceDto();
        competenceDto.setTitre("Cours de guitare modifié");

        Utilisateur autreUtilisateur = new Utilisateur();
        autreUtilisateur.setId(2L);

        when(competenceRepository.findById(1L)).thenReturn(Optional.of(competence));

        // Act & Assert
        assertThrows(OperationNonAutoriseeException.class, () -> {
            competenceService.modifierCompetence(1L, competenceDto, autreUtilisateur);
        });
    }
}