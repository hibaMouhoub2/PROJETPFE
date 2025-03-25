package com.example.projetpfe.repository;

import com.example.projetpfe.entity.Client;
import com.example.projetpfe.entity.ClientStatus;
import com.example.projetpfe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    // Trouver les clients par numéro de téléphone
    List<Client> findByTelephone(String telephone);

    // Trouver les clients par email
    List<Client> findByEmail(String email);

    // Trouver les clients créés par un utilisateur spécifique
    List<Client> findByCreatedBy(User user);

    // Trouver les clients traités (questionnaire complété)
    List<Client> findByCompletedTrue();

    // Trouver les clients non traités (questionnaire non complété)
    List<Client> findByCompletedFalse();

    List<Client> findByStatus(ClientStatus status);
    long countByStatus(ClientStatus status);
    // Trouver par nom et prénom
    List<Client> findByNomAndPrenom(String nom, String prenom);
}