package com.example.projetpfe.service.Impl;

import com.example.projetpfe.dto.ClientTaskDTO;
import com.example.projetpfe.entity.Client;
import com.example.projetpfe.entity.ClientTask;
import com.example.projetpfe.entity.ClientTaskStatus;
import com.example.projetpfe.entity.User;
import com.example.projetpfe.repository.ClientRepository;
import com.example.projetpfe.repository.ClientTaskRepository;
import com.example.projetpfe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class ClientTaskService {

    private final ClientTaskRepository clientTaskRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository; // Ajout de la référence au repository Client

    @Autowired
    public ClientTaskService(ClientTaskRepository clientTaskRepository,
                             UserRepository userRepository,
                             ClientRepository clientRepository) {
        this.clientTaskRepository = clientTaskRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }
    // Ajouter cette méthode dans ClientTaskService
    public List<ClientTask> getUserCallbackTasks(String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        // Cette méthode retourne les tâches de type rappel pour les clients avec statut ABSENT
        return clientTaskRepository.findByAssignedUserAndStatus(user, ClientTaskStatus.TO_CALLBACK);
    }

    // Créer une nouvelle tâche client
    @Transactional
    public ClientTask createClientTask(ClientTaskDTO taskDTO, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail);
        Client client = clientRepository.findById(taskDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + taskDTO.getClientId()));

        ClientTask task = new ClientTask();
        task.setClient(client); // Lier à l'entité Client
        task.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : ClientTaskStatus.PENDING);
        task.setNotes(taskDTO.getNotes());
        task.setScheduledDate(taskDTO.getScheduledDate());
        task.setCreatedBy(creator);
        task.setUpdatedBy(creator);

        if (taskDTO.getAssignedUserId() != null) {
            User assignedUser = userRepository.findById(taskDTO.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            task.setAssignedUser(assignedUser);
        }

        return clientTaskRepository.save(task);
    }

    // Assigner une tâche à un utilisateur
    @Transactional
    public ClientTask assignTaskToUser(Long taskId, Long userId, String adminEmail) {
        ClientTask task = clientTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        User admin = userRepository.findByEmail(adminEmail);

        task.setAssignedUser(user);
        task.setUpdatedBy(admin);
        task.setUpdatedAt(LocalDateTime.now());

        return clientTaskRepository.save(task);
    }

    // Obtenir l'agenda d'un utilisateur
    public Map<String, List<ClientTask>> getUserAgenda(String userEmail) {
        User user = userRepository.findByEmail(userEmail);

        Map<String, List<ClientTask>> agenda = new HashMap<>();

        // Tâches en attente
        agenda.put("pending",
                clientTaskRepository.findByAssignedUserAndStatus(user, ClientTaskStatus.PENDING));

        // Tâches à rappeler
        agenda.put("callback",
                clientTaskRepository.findByAssignedUserAndStatus(user, ClientTaskStatus.TO_CALLBACK));

        return agenda;
    }

    // Mettre à jour le statut d'une tâche
    @Transactional
    public ClientTask updateTaskStatus(Long taskId, ClientTaskStatus newStatus,
                                       String notes, String userEmail) {
        ClientTask task = clientTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

        User user = userRepository.findByEmail(userEmail);

        task.setStatus(newStatus);
        if (notes != null && !notes.trim().isEmpty()) {
            task.setNotes(notes);
        }

        // Si marquée comme complétée
        if (newStatus == ClientTaskStatus.COMPLETED) {
            task.setCompleted(true);

            // Également marquer le client comme traité si la tâche est complétée
            if (task.getClient() != null) {
                Client client = task.getClient();
                client.setCompleted(true);
                clientRepository.save(client);
            }
        }

        task.setUpdatedBy(user);
        task.setUpdatedAt(LocalDateTime.now());

        return clientTaskRepository.save(task);
    }

    // Planifier un rappel
    @Transactional
    public ClientTask scheduleCallback(Long taskId, LocalDateTime callbackDate,
                                       String notes, String userEmail) {
        ClientTask task = clientTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

        User user = userRepository.findByEmail(userEmail);

        task.setStatus(ClientTaskStatus.TO_CALLBACK);
        task.setScheduledDate(callbackDate);

        if (notes != null && !notes.trim().isEmpty()) {
            task.setNotes(notes);
        }

        task.setUpdatedBy(user);
        task.setUpdatedAt(LocalDateTime.now());

        return clientTaskRepository.save(task);
    }

    // Obtenir les statistiques d'un utilisateur
    public Map<String, Long> getUserStats(String userEmail) {
        User user = userRepository.findByEmail(userEmail);

        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", clientTaskRepository.countByAssignedUserAndStatus(
                user, ClientTaskStatus.PENDING));
        stats.put("callback", clientTaskRepository.countByAssignedUserAndStatus(
                user, ClientTaskStatus.TO_CALLBACK));
        stats.put("completed", clientTaskRepository.countByAssignedUserAndStatus(
                user, ClientTaskStatus.COMPLETED));

        return stats;
    }

    // Récupérer l'historique d'un utilisateur
    public List<ClientTask> getUserHistory(String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        return clientTaskRepository.findByAssignedUserAndCompletedTrue(user);
    }

    // Obtenir une tâche par son ID
    public ClientTask getTaskById(Long taskId) {
        return clientTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
    }

    public void updateTask(Long taskId, ClientTask updatedTask, String userEmail) {
        // Récupérer la tâche existante
        ClientTask existingTask = getTaskById(taskId);

        // Récupérer l'utilisateur qui fait la mise à jour
        User user = userRepository.findByEmail(userEmail);

        // Mettre à jour les champs
        existingTask.setNotes(updatedTask.getNotes());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setScheduledDate(updatedTask.getScheduledDate());
        existingTask.setUpdatedBy(user);
        existingTask.setUpdatedAt(LocalDateTime.now());

        // Si la tâche est marquée comme complétée
        if (updatedTask.getStatus() == ClientTaskStatus.COMPLETED) {
            existingTask.setCompleted(true);

            // Marquer également le client comme traité
            if (existingTask.getClient() != null) {
                Client client = existingTask.getClient();
                client.setCompleted(true);
                clientRepository.save(client);
            }
        }

        // Enregistrer les modifications
        clientTaskRepository.save(existingTask);
    }

    // Dans ClientTaskService.java

    // Récupérer toutes les tâches (pour l'admin)
    public List<ClientTask> getAllTasks() {
        return clientTaskRepository.findAllByOrderByCreatedAtDesc();
    }

    // Récupérer les tâches non assignées
    public List<ClientTask> getUnassignedTasks() {
        return clientTaskRepository.findByAssignedUserIsNull();
    }

    // Filtrer les tâches par utilisateur
    public List<ClientTask> getTasksByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return clientTaskRepository.findByAssignedUserOrderByCreatedAtDesc(user);
    }

    // Récupérer les tâches par statut (tous utilisateurs confondus)
    public List<ClientTask> getTasksByStatus(ClientTaskStatus status) {
        return clientTaskRepository.findByStatus(status);
    }

    // Assigner plusieurs tâches à la fois
    @Transactional
    public void assignMultipleTasks(List<Long> taskIds, Long userId, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        User admin = userRepository.findByEmail(adminEmail);

        for (Long taskId : taskIds) {
            ClientTask task = clientTaskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
            task.setAssignedUser(user);
            task.setUpdatedBy(admin);
            task.setUpdatedAt(LocalDateTime.now());
            clientTaskRepository.save(task);
        }
    }

    // Obtenir les tâches associées à un client
    public List<ClientTask> getTasksByClientId(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        return clientTaskRepository.findByClient(client);
    }

    // Convertir ClientTaskDTO en ClientTask
    public ClientTaskDTO convertToDTO(ClientTask task) {
        ClientTaskDTO dto = new ClientTaskDTO();
        dto.setId(task.getId());
        dto.setClientId(task.getClient() != null ? task.getClient().getId() : null);
        dto.setClientName(task.getClientName());
        dto.setPhoneNumber(task.getPhoneNumber());
        dto.setEmail(task.getEmail());
        dto.setNotes(task.getNotes());
        dto.setStatus(task.getStatus());
        dto.setScheduledDate(task.getScheduledDate());
        dto.setAssignedUserId(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null);
        return dto;
    }
}