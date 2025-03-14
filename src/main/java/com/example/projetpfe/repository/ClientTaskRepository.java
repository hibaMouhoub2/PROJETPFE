package com.example.projetpfe.repository;

import com.example.projetpfe.entity.ClientTask;
import com.example.projetpfe.entity.ClientTaskStatus;
import com.example.projetpfe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClientTaskRepository extends JpaRepository<ClientTask, Long> {

    // Trouver les tâches d'un utilisateur
    List<ClientTask> findByAssignedUser(User user);

    // Trouver les tâches par statut
    List<ClientTask> findByAssignedUserAndStatus(User user, ClientTaskStatus status);

    // Trouver les tâches planifiées pour une période
    List<ClientTask> findByAssignedUserAndScheduledDateBetween(
            User user, LocalDateTime start, LocalDateTime end);

    // Compter les tâches par statut pour un utilisateur
    long countByAssignedUserAndStatus(User user, ClientTaskStatus status);

    // Obtenir les tâches "complétées" pour l'historique
    List<ClientTask> findByAssignedUserAndCompletedTrue(User user);
}