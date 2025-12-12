package echangelocal.repository;

import echangelocal.model.DemandeEchange;
import echangelocal.model.Message;
import echangelocal.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByDemandeEchangeOrderByDateEnvoiAsc(DemandeEchange demandeEchange);

    @Query("SELECT m FROM Message m WHERE m.demandeEchange = :demande AND m.destinataire = :utilisateur AND m.lu = false")
    List<Message> findMessagesNonLus(@Param("demande") DemandeEchange demande,
                                     @Param("utilisateur") Utilisateur utilisateur);

    long countByDestinataireAndLuFalse(Utilisateur destinataire);
}