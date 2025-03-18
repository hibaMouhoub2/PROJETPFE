package com.example.projetpfe.dto;

import com.example.projetpfe.entity.FacteurInfluence;
import com.example.projetpfe.entity.InteretCredit;
import com.example.projetpfe.entity.PriseContact;
import com.example.projetpfe.entity.QualiteService;
import com.example.projetpfe.entity.RaisonNonRenouvellement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientQuestionnaireDTO {

    private Long id;

    @NotEmpty(message = "Le nom est requis")
    private String nom;

    @NotEmpty(message = "Le prénom est requis")
    private String prenom;

    @NotEmpty(message = "Le numéro de téléphone est requis")
    private String telephone;

    private String email;

    private PriseContact priseDeContact;

    private RaisonNonRenouvellement raisonNonRenouvellement;

    private QualiteService qualiteService;

    // Changé en booléen
    private Boolean aDifficultesRencontrees;

    private String precisionDifficultes;

    // Changé en enum
    private InteretCredit interetNouveauCredit;

    private Boolean rendezVousAgence;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateHeureRendezVous;

    private FacteurInfluence facteurInfluence;

    private String autresFacteurs;

    private Boolean completed;

    // Ajoutez des méthodes utilitaires au besoin
}