package echangelocal.repository;

import echangelocal.model.Competence;
import echangelocal.model.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'accès aux données des compétences.
 * Pattern REPOSITORY - Abstraction de l'accès aux données.
 */
@Repository
public interface CompetenceRepository extends JpaRepository<Competence, Long> {

    // Trouver toutes les compétences d'un utilisateur
    List<Competence> findByCreateurOrderByDateCreationDesc(Utilisateur createur);

    // Trouver les compétences disponibles avec pagination
    Page<Competence> findByDisponibleTrueOrderByDateCreationDesc(Pageable pageable);

    // Rechercher par catégorie
    Page<Competence> findByCategorieAndDisponibleTrueOrderByDateCreationDesc(String categorie, Pageable pageable);

    // Recherche par mots-clés dans le titre et la description
    @Query("SELECT c FROM Competence c WHERE c.disponible = true AND " +
            "(LOWER(c.titre) LIKE LOWER(CONCAT('%', :recherche, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :recherche, '%'))) " +
            "ORDER BY c.dateCreation DESC")
    Page<Competence> rechercherParMotsCles(@Param("recherche") String recherche, Pageable pageable);

    // Recherche par localisation du créateur
    @Query("SELECT c FROM Competence c WHERE c.disponible = true AND " +
            "LOWER(c.createur.localisation) LIKE LOWER(CONCAT('%', :localisation, '%')) " +
            "ORDER BY c.dateCreation DESC")
    Page<Competence> rechercherParLocalisation(@Param("localisation") String localisation, Pageable pageable);

    // Recherche combinée (catégorie + mots-clés)
    @Query("SELECT c FROM Competence c WHERE c.disponible = true AND " +
            "c.categorie = :categorie AND " +
            "(LOWER(c.titre) LIKE LOWER(CONCAT('%', :recherche, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :recherche, '%'))) " +
            "ORDER BY c.dateCreation DESC")
    Page<Competence> rechercherParCategorieEtMotsCles(@Param("categorie") String categorie,
                                                      @Param("recherche") String recherche,
                                                      Pageable pageable);

    // Compter les compétences actives d'un utilisateur
    long countByCreateurAndDisponibleTrue(Utilisateur createur);

    // Trouver une compétence avec son créateur (optimisation)
    @Query("SELECT c FROM Competence c JOIN FETCH c.createur WHERE c.id = :id")
    Optional<Competence> findByIdWithCreateur(@Param("id") Long id);

    // Recherche avancée avec tous les critères
    @Query("SELECT c FROM Competence c WHERE c.disponible = true AND " +
            "(:categorie IS NULL OR c.categorie = :categorie) AND " +
            "(:recherche IS NULL OR LOWER(c.titre) LIKE LOWER(CONCAT('%', :recherche, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :recherche, '%'))) AND " +
            "(:localisation IS NULL OR LOWER(c.createur.localisation) LIKE LOWER(CONCAT('%', :localisation, '%'))) " +
            "ORDER BY c.dateCreation DESC")
    Page<Competence> rechercherAvancee(@Param("recherche") String recherche,
                                       @Param("categorie") String categorie,
                                       @Param("localisation") String localisation,
                                       Pageable pageable);
}