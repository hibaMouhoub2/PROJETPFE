package com.example.projetpfe.repository;

import com.example.projetpfe.entity.Client;
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

    // Trouver toutes les tâches (pour l'admin)
    List<ClientTask> findAllByOrderByCreatedAtDesc();

    // Trouver les tâches non assignées
    List<ClientTask> findByAssignedUserIsNull();

    // Trouver les tâches par utilisateur assigné
    List<ClientTask> findByAssignedUserOrderByCreatedAtDesc(User user);

    // Trouver les tâches par statut (pour tous les utilisateurs)
    List<ClientTask> findByStatus(ClientTaskStatus status);

    // Trouver les tâches associées à un client
    List<ClientTask> findByClient(Client client);

    // Trouver les tâches complétées pour un client
    List<ClientTask> findByClientAndCompletedTrue(Client client);

    // Trouver les tâches en attente pour un client
    List<ClientTask> findByClientAndCompletedFalse(Client client);
}