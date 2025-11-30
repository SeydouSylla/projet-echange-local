package echangelocal.repository;

import echangelocal.model.Annonce;
import echangelocal.model.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long> {

    // Trouver toutes les annonces d'un utilisateur
    List<Annonce> findByCreateurOrderByDateCreationDesc(Utilisateur createur);

    // Trouver les annonces disponibles avec pagination
    Page<Annonce> findByDisponibleTrueOrderByDateCreationDesc(Pageable pageable);

    // Rechercher par catégorie
    Page<Annonce> findByCategorieAndDisponibleTrueOrderByDateCreationDesc(String categorie, Pageable pageable);

    // Recherche par mots-clés dans le titre et la description
    @Query("SELECT a FROM Annonce a WHERE a.disponible = true AND " +
            "(LOWER(a.titre) LIKE LOWER(CONCAT('%', :recherche, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :recherche, '%'))) " +
            "ORDER BY a.dateCreation DESC")
    Page<Annonce> rechercherParMotsCles(@Param("recherche") String recherche, Pageable pageable);

    // Recherche combinée (catégorie + mots-clés)
    @Query("SELECT a FROM Annonce a WHERE a.disponible = true AND " +
            "a.categorie = :categorie AND " +
            "(LOWER(a.titre) LIKE LOWER(CONCAT('%', :recherche, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :recherche, '%'))) " +
            "ORDER BY a.dateCreation DESC")
    Page<Annonce> rechercherParCategorieEtMotsCles(@Param("categorie") String categorie,
                                                   @Param("recherche") String recherche,
                                                   Pageable pageable);

    // Compter les annonces actives d'un utilisateur
    long countByCreateurAndDisponibleTrue(Utilisateur createur);

    // Trouver une annonce avec son créateur (pour optimisation)
    @Query("SELECT a FROM Annonce a JOIN FETCH a.createur WHERE a.id = :id")
    Optional<Annonce> findByIdWithCreateur(@Param("id") Long id);
}