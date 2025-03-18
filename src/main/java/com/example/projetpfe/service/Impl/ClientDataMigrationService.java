package com.example.projetpfe.service;

import com.example.projetpfe.entity.Client;
import com.example.projetpfe.entity.ClientTask;
import com.example.projetpfe.repository.ClientRepository;
import com.example.projetpfe.repository.ClientTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour migrer les données des tâches clients existantes
 * vers le nouveau modèle utilisant l'entité Client.
 */
@Service
public class ClientDataMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(ClientDataMigrationService.class);

    private final ClientTaskRepository clientTaskRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public ClientDataMigrationService(ClientTaskRepository clientTaskRepository,
                                      ClientRepository clientRepository) {
        this.clientTaskRepository = clientTaskRepository;
        this.clientRepository = clientRepository;
    }

    /**
     * Méthode exécutée au démarrage de l'application pour migrer les données.
     * Elle cherche les tâches clients qui n'ont pas de client associé,
     * et crée des entités Client pour ces tâches si nécessaire.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateClientTasksData() {
        logger.info("Démarrage de la migration des données ClientTask...");

        List<ClientTask> tasksWithoutClient = clientTaskRepository.findAll().stream()
                .filter(task -> task.getClient() == null && task.getClientName() != null)
                .toList();

        if (tasksWithoutClient.isEmpty()) {
            logger.info("Aucune tâche client à migrer trouvée.");
            return;
        }

        logger.info("Nombre de tâches à migrer : {}", tasksWithoutClient.size());

        for (ClientTask task : tasksWithoutClient) {
            try {
                migrateTask(task);
            } catch (Exception e) {
                logger.error("Erreur lors de la migration de la tâche ID {}: {}", task.getId(), e.getMessage());
            }
        }

        logger.info("Migration des données ClientTask terminée.");
    }

    /**
     * Migre une tâche client individuelle.
     */
    @Transactional
    public void migrateTask(ClientTask task) {
        String clientName = task.getClientName();
        String phoneNumber = task.getPhoneNumber();

        if (clientName == null && phoneNumber == null) {
            logger.warn("Tâche ID {} sans nom client ni téléphone, impossible de migrer.", task.getId());
            return;
        }

        // Tenter de trouver un client existant par téléphone
        Optional<Client> existingClient = Optional.empty();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            List<Client> clients = clientRepository.findByTelephone(phoneNumber);
            if (!clients.isEmpty()) {
                existingClient = Optional.of(clients.get(0));
            }
        }

        // Si aucun client trouvé, en créer un nouveau
        Client client;
        if (existingClient.isEmpty()) {
            client = new Client();

            // Tenter de séparer le nom et prénom si possible
            if (clientName != null && !clientName.isEmpty()) {
                String[] nameParts = clientName.trim().split("\\s+", 2);
                if (nameParts.length > 1) {
                    client.setPrenom(nameParts[0]);
                    client.setNom(nameParts[1]);
                } else {
                    client.setPrenom(clientName);
                    client.setNom(""); // Nom de famille inconnu
                }
            } else {
                client.setPrenom("Client");
                client.setNom("Sans Nom");
            }

            client.setTelephone(phoneNumber);
            client.setEmail(task.getEmail());
            client.setCreatedBy(task.getCreatedBy());
            client.setUpdatedBy(task.getUpdatedBy());
            client.setCreatedAt(task.getCreatedAt());
            client.setUpdatedAt(task.getUpdatedAt());
            client.setCompleted(task.isCompleted());

            client = clientRepository.save(client);
            logger.info("Nouveau client créé: {} {}", client.getPrenom(), client.getNom());
        } else {
            client = existingClient.get();
            logger.info("Client existant utilisé: {} {}", client.getPrenom(), client.getNom());
        }

        // Associer la tâche au client
        task.setClient(client);
        clientTaskRepository.save(task);
        logger.info("Tâche ID {} associée au client ID {}", task.getId(), client.getId());
    }
}