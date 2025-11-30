package echangelocal.service.impl;

import echangelocal.config.ApplicationProperties;
import echangelocal.dto.InscriptionDto;
import echangelocal.dto.ProfilDto;
import echangelocal.exception.TelephoneInvalideException;
import echangelocal.exception.UtilisateurExistantException;
import echangelocal.exception.UtilisateurNonTrouveException;
import echangelocal.model.Utilisateur;
import echangelocal.repository.UtilisateurRepository;
import echangelocal.service.interfaces.UtilisateurService;
import echangelocal.util.FileStorageUtil;
import echangelocal.util.TelephoneUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationProperties applicationProperties;

    // Stockage temporaire des codes de vérification (en production, utiliser Redis)
    private final ConcurrentHashMap<Long, String> codesVerification = new ConcurrentHashMap<>();

    @Autowired
    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository,
                                  PasswordEncoder passwordEncoder,
                                  ApplicationProperties applicationProperties) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Utilisateur inscrireUtilisateur(InscriptionDto inscriptionDto) {
        if (utilisateurRepository.existsByEmail(inscriptionDto.getEmail())) {
            throw new UtilisateurExistantException("Un utilisateur avec cet email existe déjà");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(inscriptionDto.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(inscriptionDto.getMotDePasse()));
        utilisateur.setPrenom(inscriptionDto.getPrenom());
        utilisateur.setNom(inscriptionDto.getNom());
        utilisateur.setLocalisation(inscriptionDto.getLocalisation());

        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public Optional<Utilisateur> trouverParEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    @Override
    public Optional<Utilisateur> trouverParId(Long id) {
        return utilisateurRepository.findById(id);
    }

    @Override
    public Utilisateur mettreAJourProfil(Long id, ProfilDto profilDto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNonTrouveException("Utilisateur non trouvé"));

        utilisateur.setPrenom(profilDto.getPrenom());
        utilisateur.setNom(profilDto.getNom());
        utilisateur.setLocalisation(profilDto.getLocalisation());
        utilisateur.setBiographie(profilDto.getBiographie());

        // Validation et mise à jour du téléphone - NE PAS RÉINITIALISER LA VÉRIFICATION SI LE NUMÉRO NE CHANGE PAS
        if (profilDto.getTelephone() != null && !profilDto.getTelephone().trim().isEmpty()) {
            if (!TelephoneUtil.estNumeroTelephoneValide(profilDto.getTelephone())) {
                throw new TelephoneInvalideException("Le format du numéro de téléphone est invalide");
            }

            String nouveauTelephoneFormate = TelephoneUtil.formaterNumeroTelephone(profilDto.getTelephone());
            String ancienTelephone = utilisateur.getTelephone();

            // Vérifier si le numéro a réellement changé
            if (!nouveauTelephoneFormate.equals(ancienTelephone)) {
                utilisateur.setTelephone(nouveauTelephoneFormate);
                utilisateur.setTelephoneVerifie(false); // Réinitialiser seulement si le numéro change
            }
            // Si le numéro est le même, on garde le statut de vérification actuel
        } else {
            // Si on supprime le numéro
            utilisateur.setTelephone(null);
            utilisateur.setTelephoneVerifie(false);
        }

        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public Utilisateur mettreAJourPhotoProfil(Long utilisateurId, MultipartFile fichier) throws IOException {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new UtilisateurNonTrouveException("Utilisateur non trouvé"));

        // Vérifier que le fichier n'est pas vide
        if (fichier.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        // Vérifier le type MIME
        String contentType = fichier.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier doit être une image");
        }

        // Supprimer l'ancienne photo si elle existe
        if (utilisateur.getPhotoProfil() != null) {
            FileStorageUtil.supprimerFichier(utilisateur.getPhotoProfil(), applicationProperties.getUploadDir());
        }

        // Sauvegarder la nouvelle photo
        String nomFichier = FileStorageUtil.sauvegarderFichier(fichier, applicationProperties.getUploadDir());
        utilisateur.setPhotoProfil(nomFichier);

        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public boolean verifierMotDePasse(String motDePasseClair, String motDePasseCrypte) {
        return passwordEncoder.matches(motDePasseClair, motDePasseCrypte);
    }

    @Override
    public String genererCodeVerificationTelephone(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new UtilisateurNonTrouveException("Utilisateur non trouvé"));

        if (utilisateur.getTelephone() == null) {
            throw new TelephoneInvalideException("Aucun numéro de téléphone à vérifier");
        }

        // Générer un code à 6 chiffres
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
        codesVerification.put(utilisateurId, code);

        // En production, envoyer le code par SMS via un service externe
        System.out.println("Code de vérification pour " + utilisateur.getTelephone() + ": " + code);

        return code;
    }

    @Override
    public void verifierTelephone(Long utilisateurId, String codeVerification) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new UtilisateurNonTrouveException("Utilisateur non trouvé"));

        String codeStocke = codesVerification.get(utilisateurId);

        if (codeStocke == null || !codeStocke.equals(codeVerification)) {
            throw new TelephoneInvalideException("Code de vérification invalide");
        }

        utilisateur.setTelephoneVerifie(true);
        utilisateurRepository.save(utilisateur);

        // Nettoyer le code après utilisation
        codesVerification.remove(utilisateurId);
    }
}