package echangelocal.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileStorageUtil {

    private FileStorageUtil() {
        // Utilitaire class - pas d'instanciation
    }

    /**
     * Sauvegarde un fichier dans le répertoire spécifié
     */
    public static String sauvegarderFichier(MultipartFile fichier, String repertoireBase) throws IOException {
        // Créer le répertoire s'il n'existe pas
        Path cheminRepertoire = Paths.get(repertoireBase);

        if (!Files.exists(cheminRepertoire)) {
            Files.createDirectories(cheminRepertoire);
            System.out.println("Répertoire créé: " + cheminRepertoire.toAbsolutePath());
        }

        // Générer un nom de fichier unique
        String nomFichierUnique = genererNomFichierUnique(fichier.getOriginalFilename());
        Path cheminComplet = cheminRepertoire.resolve(nomFichierUnique);

        // Copier le fichier
        Files.copy(fichier.getInputStream(), cheminComplet);

        System.out.println("Fichier sauvegardé: " + cheminComplet.toAbsolutePath());

        return nomFichierUnique;
    }

    /**
     * Supprime un fichier (version avec nom de fichier + répertoire)
     */
    public static void supprimerFichier(String nomFichier, String repertoireBase) throws IOException {
        if (nomFichier != null && !nomFichier.trim().isEmpty()) {
            Path cheminFichier = Paths.get(repertoireBase, nomFichier);
            supprimerFichier(cheminFichier);
        }
    }

    /**
     * Supprime un fichier (version avec chemin complet)
     */
    public static void supprimerFichier(Path cheminComplet) throws IOException {
        if (cheminComplet != null && Files.exists(cheminComplet)) {
            Files.delete(cheminComplet);
            System.out.println("Fichier supprimé: " + cheminComplet.toAbsolutePath());
        }
    }

    /**
     * Vérifie si un fichier existe
     */
    public static boolean fichierExiste(Path cheminComplet) {
        return cheminComplet != null && Files.exists(cheminComplet);
    }

    /**
     * Génère un nom de fichier unique avec UUID
     */
    private static String genererNomFichierUnique(String nomOriginal) {
        String extension = "";
        if (nomOriginal != null && nomOriginal.contains(".")) {
            extension = nomOriginal.substring(nomOriginal.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}