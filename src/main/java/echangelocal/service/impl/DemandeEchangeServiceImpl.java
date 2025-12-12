package echangelocal.service.impl;

import echangelocal.dto.DemandeEchangeDto;
import echangelocal.dto.MessageDto;
import echangelocal.exception.DemandeEchangeNonTrouveeException;
import echangelocal.exception.OperationNonAutoriseeException;
import echangelocal.model.*;
import echangelocal.repository.AnnonceRepository;
import echangelocal.repository.CompetenceRepository;
import echangelocal.repository.DemandeEchangeRepository;
import echangelocal.repository.MessageRepository;
import echangelocal.service.interfaces.DemandeEchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DemandeEchangeServiceImpl implements DemandeEchangeService {

    private final DemandeEchangeRepository demandeEchangeRepository;
    private final MessageRepository messageRepository;
    private final AnnonceRepository annonceRepository;
    private final CompetenceRepository competenceRepository;

    @Autowired
    public DemandeEchangeServiceImpl(DemandeEchangeRepository demandeEchangeRepository,
                                     MessageRepository messageRepository,
                                     AnnonceRepository annonceRepository,
                                     CompetenceRepository competenceRepository) {
        this.demandeEchangeRepository = demandeEchangeRepository;
        this.messageRepository = messageRepository;
        this.annonceRepository = annonceRepository;
        this.competenceRepository = competenceRepository;
    }

    // ============ IMPLÉMENTATION DES MÉTHODES ============

    @Override
    public DemandeEchange creerDemandeEchange(DemandeEchangeDto demandeDto, Utilisateur demandeur) {
        validerDemandeEchangeDto(demandeDto);

        DemandeEchange demande = new DemandeEchange();
        demande.setDemandeur(demandeur);
        demande.setPropositionEchange(demandeDto.getPropositionEchange());
        demande.setMessageDemande(demandeDto.getMessageDemande());
        demande.setDateProposee(demandeDto.getDateProposee());

        if (demandeDto.getAnnonceId() != null) {
            Annonce annonce = annonceRepository.findById(demandeDto.getAnnonceId())
                    .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée"));
            validerAnnoncePourDemande(annonce, demandeur);
            demande.setAnnonce(annonce);
            demande.setDestinataire(annonce.getCreateur());
        } else if (demandeDto.getCompetenceId() != null) {
            Competence competence = competenceRepository.findById(demandeDto.getCompetenceId())
                    .orElseThrow(() -> new IllegalArgumentException("Compétence non trouvée"));
            validerCompetencePourDemande(competence, demandeur);
            demande.setCompetence(competence);
            demande.setDestinataire(competence.getCreateur());
        } else {
            throw new IllegalArgumentException("Une annonce ou une compétence doit être spécifiée");
        }

        // CORRECTION: Sauvegarder et flusher immédiatement
        DemandeEchange demandeSaved = demandeEchangeRepository.saveAndFlush(demande);
        return demandeSaved;
    }

    @Override
    public DemandeEchange accepterDemande(Long demandeId, Utilisateur destinataire) {
        DemandeEchange demande = trouverDemandeAvecVerification(demandeId, destinataire);

        if (demande.getStatut() != DemandeEchange.StatutDemande.EN_ATTENTE) {
            throw new OperationNonAutoriseeException("Cette demande ne peut plus être acceptée");
        }

        demande.setStatut(DemandeEchange.StatutDemande.ACCEPTEE);

        // Si c'est une annonce, la marquer comme indisponible temporairement
        if (demande.getAnnonce() != null) {
            demande.getAnnonce().setDisponible(false);
        }
        // Si c'est une compétence, la marquer comme indisponible temporairement
        if (demande.getCompetence() != null) {
            demande.getCompetence().setDisponible(false);
        }

        // CORRECTION: Sauvegarder et flusher
        return demandeEchangeRepository.saveAndFlush(demande);
    }

    @Override
    public DemandeEchange refuserDemande(Long demandeId, Utilisateur destinataire) {
        DemandeEchange demande = trouverDemandeAvecVerification(demandeId, destinataire);

        if (demande.getStatut() != DemandeEchange.StatutDemande.EN_ATTENTE) {
            throw new OperationNonAutoriseeException("Cette demande ne peut plus être refusée");
        }

        demande.setStatut(DemandeEchange.StatutDemande.REFUSEE);
        return demandeEchangeRepository.saveAndFlush(demande);
    }

    @Override
    public void annulerDemande(Long demandeId, Utilisateur demandeur) {
        DemandeEchange demande = demandeEchangeRepository.findByIdAndDemandeur(demandeId, demandeur)
                .orElseThrow(() -> new DemandeEchangeNonTrouveeException("Demande d'échange non trouvée"));

        if (demande.getStatut() != DemandeEchange.StatutDemande.EN_ATTENTE) {
            throw new OperationNonAutoriseeException("Seules les demandes en attente peuvent être annulées");
        }

        demande.setStatut(DemandeEchange.StatutDemande.ANNULEE);
        demandeEchangeRepository.saveAndFlush(demande);
    }

    @Override
    public Message envoyerMessage(MessageDto messageDto, Utilisateur expediteur) {
        validerMessageDto(messageDto);

        DemandeEchange demande = demandeEchangeRepository.findById(messageDto.getDemandeEchangeId())
                .orElseThrow(() -> new DemandeEchangeNonTrouveeException("Demande d'échange non trouvée"));

        if (!peutEnvoyerMessage(demande.getId(), expediteur)) {
            throw new OperationNonAutoriseeException("Vous n'êtes pas autorisé à envoyer des messages dans cette demande");
        }

        Message message = new Message();
        message.setExpediteur(expediteur);
        message.setContenu(messageDto.getContenu());

        // Déterminer le destinataire
        if (expediteur.equals(demande.getDemandeur())) {
            message.setDestinataire(demande.getDestinataire());
        } else {
            message.setDestinataire(demande.getDemandeur());
        }

        message.setDemandeEchange(demande);
        demande.ajouterMessage(message);

        return messageRepository.saveAndFlush(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DemandeEchange> trouverParId(Long id) {
        return demandeEchangeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DemandeEchange> trouverParIdAvecMessages(Long id) {
        return demandeEchangeRepository.findByIdWithMessages(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeEchange> trouverDemandesEnvoyees(Utilisateur utilisateur) {
        return demandeEchangeRepository.findByDemandeurOrderByDateCreationDesc(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeEchange> trouverDemandesRecues(Utilisateur utilisateur) {
        return demandeEchangeRepository.findByDestinataireOrderByDateCreationDesc(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DemandeEchange> trouverDemandesRecuesParStatut(Utilisateur utilisateur,
                                                               DemandeEchange.StatutDemande statut,
                                                               Pageable pageable) {
        return demandeEchangeRepository.findByDestinataireAndStatut(utilisateur, statut, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeEchange> trouverEchangesActifs(Utilisateur utilisateur) {
        return demandeEchangeRepository.findEchangesActifs(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterDemandesEnAttente(Utilisateur utilisateur) {
        return demandeEchangeRepository.countDemandesEnAttente(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estDestinataire(Long demandeId, Utilisateur utilisateur) {
        return demandeEchangeRepository.findByIdAndDestinataire(demandeId, utilisateur).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estDemandeur(Long demandeId, Utilisateur utilisateur) {
        return demandeEchangeRepository.findByIdAndDemandeur(demandeId, utilisateur).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean peutEnvoyerMessage(Long demandeId, Utilisateur utilisateur) {
        Optional<DemandeEchange> demandeOpt = demandeEchangeRepository.findById(demandeId);
        if (demandeOpt.isEmpty()) {
            return false;
        }

        DemandeEchange demande = demandeOpt.get();
        return (demande.getStatut() == DemandeEchange.StatutDemande.ACCEPTEE) &&
                (utilisateur.equals(demande.getDemandeur()) || utilisateur.equals(demande.getDestinataire()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DemandeEchange> trouverDemandesRecuesPage(Utilisateur utilisateur, Pageable pageable) {
        return demandeEchangeRepository.findByDestinataire(utilisateur, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DemandeEchange> trouverDemandesEnvoyeesPage(Utilisateur utilisateur, Pageable pageable) {
        return demandeEchangeRepository.findByDemandeur(utilisateur, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DemandeEchange> trouverDemandesEnvoyeesParStatut(Utilisateur utilisateur,
                                                                 DemandeEchange.StatutDemande statut,
                                                                 Pageable pageable) {
        return demandeEchangeRepository.findByDemandeurAndStatut(utilisateur, statut, pageable);
    }

    // ============ NOUVELLES MÉTHODES POUR LES STATISTIQUES ============

    @Override
    @Transactional(readOnly = true)
    public long compterDemandesRecuesParStatut(Utilisateur utilisateur, DemandeEchange.StatutDemande statut) {
        return demandeEchangeRepository.countByDestinataireAndStatut(utilisateur, statut);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterToutesDemandesRecues(Utilisateur utilisateur) {
        return demandeEchangeRepository.countByDestinataire(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterDemandesEnvoyeesParStatut(Utilisateur utilisateur, DemandeEchange.StatutDemande statut) {
        return demandeEchangeRepository.countByDemandeurAndStatut(utilisateur, statut);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterToutesDemandesEnvoyees(Utilisateur utilisateur) {
        return demandeEchangeRepository.countByDemandeur(utilisateur);
    }

    // ============ MÉTHODES PRIVÉES UTILITAIRES ============

    private void validerDemandeEchangeDto(DemandeEchangeDto demandeDto) {
        if (demandeDto == null) {
            throw new IllegalArgumentException("La demande d'échange ne peut pas être nulle");
        }
        if (demandeDto.getAnnonceId() == null && demandeDto.getCompetenceId() == null) {
            throw new IllegalArgumentException("Une annonce ou une compétence doit être spécifiée");
        }
        if (demandeDto.getDateProposee() == null) {
            throw new IllegalArgumentException("La date d'échange est obligatoire");
        }
    }

    private void validerMessageDto(MessageDto messageDto) {
        if (messageDto == null) {
            throw new IllegalArgumentException("Le message ne peut pas être nul");
        }
        if (messageDto.getContenu() == null || messageDto.getContenu().trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu du message est obligatoire");
        }
    }

    private void validerAnnoncePourDemande(Annonce annonce, Utilisateur demandeur) {
        if (!annonce.isDisponible()) {
            throw new IllegalArgumentException("Cette annonce n'est pas disponible");
        }
        if (annonce.getCreateur().equals(demandeur)) {
            throw new IllegalArgumentException("Vous ne pouvez pas faire une demande sur votre propre annonce");
        }
    }

    private void validerCompetencePourDemande(Competence competence, Utilisateur demandeur) {
        if (!competence.isDisponible()) {
            throw new IllegalArgumentException("Cette compétence n'est pas disponible");
        }
        if (competence.getCreateur().equals(demandeur)) {
            throw new IllegalArgumentException("Vous ne pouvez pas faire une demande sur votre propre compétence");
        }
    }

    private DemandeEchange trouverDemandeAvecVerification(Long demandeId, Utilisateur destinataire) {
        DemandeEchange demande = demandeEchangeRepository.findById(demandeId)
                .orElseThrow(() -> new DemandeEchangeNonTrouveeException("Demande d'échange non trouvée"));

        if (!demande.getDestinataire().equals(destinataire)) {
            throw new OperationNonAutoriseeException("Vous n'êtes pas autorisé à modifier cette demande");
        }

        return demande;
    }
}