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

    @NotEmpty(message = "Le nom du client est requis")
    private String clientName;

    @NotEmpty(message = "Le numéro de téléphone est requis")
    private String phoneNumber;

    private String email;

    private String notes;

    @NotNull(message = "L'utilisateur assigné est requis")
    private Long assignedUserId;

    private ClientTaskStatus status;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduledDate;
}