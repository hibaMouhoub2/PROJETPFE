package com.example.projetpfe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String telephone;
    private String email;


    @Enumerated(EnumType.STRING)
    private RaisonNonRenouvellement raisonNonRenouvellement;

    @Enumerated(EnumType.STRING)
    private QualiteService qualiteService;

    @Enumerated(EnumType.STRING)
    private ClientStatus status = ClientStatus.NON_TRAITE;

    // Changé en booléen pour la question difficultés
    private Boolean aDifficultesRencontrees;

    private String precisionDifficultes;

    // Pour l'intérêt pour un nouveau crédit, nous utiliserons une énumération
    @Enumerated(EnumType.STRING)
    private InteretCredit interetNouveauCredit;

    private Boolean rendezVousAgence;

    private LocalDateTime dateHeureRendezVous;

    @Enumerated(EnumType.STRING)
    private FacteurInfluence facteurInfluence;

    private String autresFacteurs;

    // Relation inverse avec ClientTask (un client peut avoir plusieurs tâches)
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClientTask> tasks = new ArrayList<>();

    // Informations de suivi
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    private LocalDateTime updatedAt;

    private Boolean completed;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        completed = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method pour ajouter une tâche au client
    public void addTask(ClientTask task) {
        tasks.add(task);
        task.setClient(this);
    }

    // Helper method pour retirer une tâche du client
    public void removeTask(ClientTask task) {
        tasks.remove(task);
        task.setClient(null);
    }

    // Méthode pour obtenir le nom complet du client
    public String getFullName() {
        return prenom + " " + nom;
    }
}