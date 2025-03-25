package com.example.projetpfe.service.Impl;

import com.example.projetpfe.dto.ClientQuestionnaireDTO;
import com.example.projetpfe.entity.*;
import com.example.projetpfe.repository.ClientRepository;
import com.example.projetpfe.repository.ClientTaskRepository;
import com.example.projetpfe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientTaskRepository clientTaskRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository,
                         UserRepository userRepository,
                         ClientTaskRepository clientTaskRepository) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.clientTaskRepository = clientTaskRepository;
    }

    // Créer un nouveau client
    @Transactional
    public Client createClient(ClientQuestionnaireDTO clientDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail);

        Client client = convertDtoToEntity(clientDTO);
        client.setCreatedBy(user);
        client.setUpdatedBy(user);

        // Créer automatiquement une tâche pour le nouveau client
        Client savedClient = clientRepository.save(client);
        createDefaultTaskForClient(savedClient, user);

        return savedClient;
    }

    // Mettre à jour un client existant
    @Transactional
    public Client updateClient(Long id, ClientQuestionnaireDTO clientDTO, String userEmail) {
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + id));

        User user = userRepository.findByEmail(userEmail);

        // Mettre à jour les champs
        updateClientFromDTO(existingClient, clientDTO);
        existingClient.setUpdatedBy(user);
        existingClient.setUpdatedAt(LocalDateTime.now());

        return clientRepository.save(existingClient);
    }

    // Marquer un questionnaire client comme complété
    @Transactional
    public Client completeQuestionnaire(Long id, String userEmail) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + id));

        User user = userRepository.findByEmail(userEmail);

        client.setCompleted(true);
        client.setUpdatedBy(user);
        client.setUpdatedAt(LocalDateTime.now());

        // Mettre à jour également les tâches associées
        for (ClientTask task : client.getTasks()) {
            if (!task.isCompleted()) {
                task.setCompleted(true);
                task.setStatus(ClientTaskStatus.COMPLETED);
                task.setUpdatedBy(user);
                task.setUpdatedAt(LocalDateTime.now());
            }
        }

        return clientRepository.save(client);
    }

    // Convertir DTO en entité
    private Client convertDtoToEntity(ClientQuestionnaireDTO dto) {
        Client client = new Client();
        updateClientFromDTO(client, dto);
        return client;
    }
    // Ajouter cette méthode dans ClientService

    // Mise à jour de l'entité à partir du DTO
    private void updateClientFromDTO(Client client, ClientQuestionnaireDTO dto) {
        client.setNom(dto.getNom());
        client.setPrenom(dto.getPrenom());
        client.setTelephone(dto.getTelephone());
        client.setEmail(dto.getEmail());
        client.setRaisonNonRenouvellement(dto.getRaisonNonRenouvellement());
        client.setQualiteService(dto.getQualiteService());
        client.setADifficultesRencontrees(dto.getADifficultesRencontrees());
        client.setPrecisionDifficultes(dto.getPrecisionDifficultes());
        client.setInteretNouveauCredit(dto.getInteretNouveauCredit());
        client.setRendezVousAgence(dto.getRendezVousAgence());
        client.setDateHeureRendezVous(dto.getDateHeureRendezVous());
        client.setFacteurInfluence(dto.getFacteurInfluence());
        client.setAutresFacteurs(dto.getAutresFacteurs());

        // Ne pas écraser le statut completed s'il est déjà true
        if (dto.getCompleted() != null && (client.getCompleted() == null || !client.getCompleted())) {
            client.setCompleted(dto.getCompleted());
        }
    }

    // Convertir une entité en DTO
    public ClientQuestionnaireDTO convertEntityToDto(Client client) {
        ClientQuestionnaireDTO dto = new ClientQuestionnaireDTO();

        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setPrenom(client.getPrenom());
        dto.setTelephone(client.getTelephone());
        dto.setEmail(client.getEmail());
        dto.setRaisonNonRenouvellement(client.getRaisonNonRenouvellement());
        dto.setQualiteService(client.getQualiteService());
        dto.setADifficultesRencontrees(client.getADifficultesRencontrees());
        dto.setPrecisionDifficultes(client.getPrecisionDifficultes());
        dto.setInteretNouveauCredit(client.getInteretNouveauCredit());
        dto.setRendezVousAgence(client.getRendezVousAgence());
        dto.setDateHeureRendezVous(client.getDateHeureRendezVous());
        dto.setFacteurInfluence(client.getFacteurInfluence());
        dto.setAutresFacteurs(client.getAutresFacteurs());
        dto.setCompleted(client.getCompleted());

        return dto;
    }

    // Trouver un client par ID
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + id));
    }

    // Trouver tous les clients
    public List<ClientQuestionnaireDTO> findAllClients() {
        return clientRepository.findAll().stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    // Trouver les clients non traités
    public List<ClientQuestionnaireDTO> findUncompletedClients() {
        return clientRepository.findByCompletedFalse().stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    // Trouver les clients traités
    public List<ClientQuestionnaireDTO> findCompletedClients() {
        return clientRepository.findByCompletedTrue().stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    // Trouver les clients par numéro de téléphone
    public List<ClientQuestionnaireDTO> findClientsByTelephone(String telephone) {
        return clientRepository.findByTelephone(telephone).stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    // Récupérer les tâches associées à un client
    public List<ClientTask> getClientTasks(Long clientId) {
        Client client = getClientById(clientId);
        return client.getTasks();
    }

    // Créer une tâche par défaut pour un nouveau client
    private void createDefaultTaskForClient(Client client, User user) {
        ClientTask task = new ClientTask();
        task.setClient(client);
        task.setStatus(ClientTaskStatus.PENDING);
        task.setNotes("Tâche créée automatiquement pour nouveau client");
        task.setCreatedBy(user);
        task.setUpdatedBy(user);
        task.setCompleted(false);

        clientTaskRepository.save(task);
    }


    // Version qui retourne des DTOs
    public List<ClientQuestionnaireDTO> findClientDtosByStatus(ClientStatus status) {
        return clientRepository.findByStatus(status).stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
    }

    // Version qui retourne des entités
    public List<Client> findClientsByStatus(ClientStatus status) {
        return clientRepository.findByStatus(status);
    }

    // Ajouter méthode pour mettre à jour le statut
    @Transactional
    public Client updateClientStatus(Long id, ClientStatus newStatus, String userEmail) {
        Client client = getClientById(id);
        User user = userRepository.findByEmail(userEmail);

        client.setStatus(newStatus);
        client.setUpdatedBy(user);
        client.setUpdatedAt(LocalDateTime.now());

        // Si statut contacté, marquer comme complété
        if (newStatus == ClientStatus.CONTACTE) {
            client.setCompleted(true);
        }

        return clientRepository.save(client);
    }
}