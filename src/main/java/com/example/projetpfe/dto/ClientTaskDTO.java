package com.example.projetpfe.dto;

import com.example.projetpfe.entity.ClientTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientTaskDTO {

    private Long id;

    // Référence à l'ID du client au lieu des informations directes
    @NotNull(message = "Le client est requis")
    private Long clientId;

    // Ces champs sont conservés pour compatibilité avec la vue,
    // mais ne seront pas utilisés pour la persistance
    private String clientName;
    private String phoneNumber;
    private String email;

    private String notes;

    @NotNull(message = "L'utilisateur assigné est requis")
    private Long assignedUserId;

    private ClientTaskStatus status;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduledDate;
}