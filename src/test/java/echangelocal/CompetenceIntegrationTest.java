package echangelocal;

import echangelocal.dto.CompetenceDto;
import echangelocal.model.Competence;
import echangelocal.model.Utilisateur;
import echangelocal.repository.CompetenceRepository;
import echangelocal.repository.UtilisateurRepository;
import echangelocal.service.interfaces.CompetenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Commit;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")

// Lance chaque test dans une transaction Spring
// MAIS la transaction sera COMMITée à la fin (et non rollbackée)
@Transactional
@Commit
class CompetenceIntegrationTest {

    @Autowired
    private CompetenceService competenceService;

    @Autowired
    private CompetenceRepository competenceRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void creerEtRecupererCompetence_DoitFonctionner() {
        // ---------- ARRANGE ----------
        Utilisateur utilisateur = new Utilisateur(
                "test@competence.com",
                "password",
                "Test",
                "Competence",
                "Paris"
        );
        utilisateur.setTelephoneVerifie(true);

        // Sauvegarde réelle en base (commitée)
        Utilisateur utilisateurSauvegarde = utilisateurRepository.save(utilisateur);

        CompetenceDto competenceDto = new CompetenceDto();
        competenceDto.setTitre("Cours de guitare débutant");
        competenceDto.setDescription("Cours de guitare pour débutants");
        competenceDto.setCategorie("Musique");
        competenceDto.setDisponibilites(Arrays.asList("Week-ends"));
        competenceDto.setDisponible(true);

        // ---------- ACT ----------
        Competence competenceCree =
                competenceService.creerCompetence(competenceDto, utilisateurSauvegarde);

        // ---------- ASSERT ----------
        assertNotNull(competenceCree.getId());
        assertEquals("Cours de guitare débutant", competenceCree.getTitre());
        assertEquals(utilisateurSauvegarde.getId(),
                competenceCree.getCreateur().getId());
    }

    @Test
    void trouverCompetencesParUtilisateur_DoitRetournerListe() {
        // ---------- ARRANGE ----------
        Utilisateur utilisateur = new Utilisateur(
                "liste@test.com",
                "password",
                "Liste",
                "Test",
                "Lyon"
        );
        utilisateur.setTelephoneVerifie(true);

        Utilisateur utilisateurSauvegarde = utilisateurRepository.save(utilisateur);

        CompetenceDto competenceDto = new CompetenceDto();
        competenceDto.setTitre("Cours test");
        competenceDto.setDescription("Description test");
        competenceDto.setCategorie("Musique");
        competenceDto.setDisponibilites(Arrays.asList("Week-ends"));
        competenceDto.setDisponible(true);

        competenceService.creerCompetence(competenceDto, utilisateurSauvegarde);

        // ---------- ACT ----------
        List<Competence> result =
                competenceService.trouverCompetencesParUtilisateur(utilisateurSauvegarde);

        // ---------- ASSERT ----------
        assertFalse(result.isEmpty());
        assertEquals("Cours test", result.get(0).getTitre());
    }

    @Test
    void estProprietaireCompetence_DoitRetournerVrai() {
        // ---------- ARRANGE ----------
        Utilisateur utilisateur = new Utilisateur(
                "proprio@test.com",
                "password",
                "Proprio",
                "Test",
                "Paris"
        );
        utilisateur.setTelephoneVerifie(true);

        Utilisateur utilisateurSauvegarde = utilisateurRepository.save(utilisateur);

        CompetenceDto competenceDto = new CompetenceDto();
        competenceDto.setTitre("Test propriétaire");
        competenceDto.setDescription("Test");
        competenceDto.setCategorie("Musique");
        competenceDto.setDisponibilites(Arrays.asList("Week-ends"));
        competenceDto.setDisponible(true);

        Competence competenceCree =
                competenceService.creerCompetence(competenceDto, utilisateurSauvegarde);

        // ---------- ACT ----------
        boolean estProprietaire =
                competenceService.estProprietaireCompetence(
                        competenceCree.getId(),
                        utilisateurSauvegarde
                );

        // ---------- ASSERT ----------
        assertTrue(estProprietaire);
    }

    @Test
    void supprimerCompetence_DoitFonctionner() {
        // ---------- ARRANGE ----------
        Utilisateur utilisateur = new Utilisateur(
                "supprimer@test.com",
                "password",
                "Supprimer",
                "Test",
                "Lille"
        );
        utilisateur.setTelephoneVerifie(true);

        Utilisateur utilisateurSauvegarde = utilisateurRepository.save(utilisateur);

        CompetenceDto competenceDto = new CompetenceDto();
        competenceDto.setTitre("Compétence à supprimer");
        competenceDto.setDescription("Test suppression");
        competenceDto.setCategorie("Musique");
        competenceDto.setDisponibilites(Arrays.asList("Week-ends"));
        competenceDto.setDisponible(true);

        Competence competenceCree =
                competenceService.creerCompetence(competenceDto, utilisateurSauvegarde);

        Long competenceId = competenceCree.getId();

        // ---------- ACT ----------
        competenceService.supprimerCompetence(competenceId, utilisateurSauvegarde);

        // ---------- ASSERT ----------
        assertTrue(competenceService.trouverParId(competenceId).isEmpty());
    }
}
