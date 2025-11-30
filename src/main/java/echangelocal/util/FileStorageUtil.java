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

    public static String sauvegarderFichier(MultipartFile fichier, String repertoireBase) throws IOException {
        Path cheminRepertoire = Paths.get(repertoireBase);

        if (!Files.exists(cheminRepertoire)) {
            Files.createDirectories(cheminRepertoire);
        }

        String nomFichierUnique = genererNomFichierUnique(fichier.getOriginalFilename());
        Path cheminComplet = cheminRepertoire.resolve(nomFichierUnique);

        Files.copy(fichier.getInputStream(), cheminComplet);

        return nomFichierUnique;
    }

    public static void supprimerFichier(String nomFichier, String repertoireBase) throws IOException {
        if (nomFichier != null) {
            Path cheminFichier = Paths.get(repertoireBase, nomFichier);
            if (Files.exists(cheminFichier)) {
                Files.delete(cheminFichier);
            }
        }
    }

    private static String genererNomFichierUnique(String nomOriginal) {
        String extension = "";
        if (nomOriginal != null && nomOriginal.contains(".")) {
            extension = nomOriginal.substring(nomOriginal.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}