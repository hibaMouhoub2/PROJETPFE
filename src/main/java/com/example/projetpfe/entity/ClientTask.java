package com.example.projetpfe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "client_tasks")
public class ClientTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relation avec l'entité Client
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    // État de la tâche
    @Enumerated(EnumType.STRING)
    private ClientTaskStatus status;

    // Date planifiée pour le traitement/rappel
    private LocalDateTime scheduledDate;

    // Commentaires/Notes
    @Column(length = 1000)
    private String notes;

    // Assignation
    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    // Champs d'audit
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    private LocalDateTime updatedAt;

    // Indique si la tâche a été traitée
    private boolean completed;

    // Méthode helper pour initialiser les timestamps
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Par défaut, les tâches sont en attente (PENDING)
        if (status == null) {
            status = ClientTaskStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters pour les informations client (pour la compatibilité avec le code existant)
    public String getClientName() {
        return client != null ? client.getPrenom() + " " + client.getNom() : null;
    }

    public String getPhoneNumber() {
        return client != null ? client.getTelephone() : null;
    }

    public String getEmail() {
        return client != null ? client.getEmail() : null;
    }
}