package com.example.projetpfe.controller;

import com.example.projetpfe.dto.ClientQuestionnaireDTO;
import com.example.projetpfe.entity.*;
import com.example.projetpfe.service.Impl.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // Afficher la liste des clients
    @GetMapping
    public String listClients(Model model) {
        List<ClientQuestionnaireDTO> clients = clientService.findAllClients();
        model.addAttribute("clients", clients);
        return "clients/index";
    }

    // Afficher la liste des clients non traités
    @GetMapping("/pending")
    public String listPendingClients(Model model) {
        List<ClientQuestionnaireDTO> clients = clientService.findUncompletedClients();
        model.addAttribute("clients", clients);
        return "clients/pending";
    }

    // Formulaire pour créer un nouveau client
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("client", new ClientQuestionnaireDTO());
        addEnumAttributesToModel(model);
        return "clients/questionnaire";
    }

    // Traiter la création d'un client
    @PostMapping("/create")
    public String processCreate(@Valid @ModelAttribute("client") ClientQuestionnaireDTO client,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            addEnumAttributesToModel(model);
            return "clients/questionnaire";
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();

            clientService.createClient(client, userEmail);
            redirectAttributes.addFlashAttribute("success", "Client créé avec succès");
            return "redirect:/clients";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création: " + e.getMessage());
            return "redirect:/clients/create";
        }
    }

    // Formulaire pour modifier un client existant
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Client client = clientService.getClientById(id);
        ClientQuestionnaireDTO clientDTO = clientService.convertEntityToDto(client);

        model.addAttribute("client", clientDTO);
        addEnumAttributesToModel(model);

        return "clients/questionnaire";
    }

    // Traiter la modification d'un client
    @PostMapping("/{id}/edit")
    public String processEdit(@PathVariable Long id,
                              @Valid @ModelAttribute("client") ClientQuestionnaireDTO client,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            addEnumAttributesToModel(model);
            return "clients/questionnaire";
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();

            clientService.updateClient(id, client, userEmail);
            redirectAttributes.addFlashAttribute("success", "Client mis à jour avec succès");
            return "redirect:/clients";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
            return "redirect:/clients/" + id + "/edit";
        }
    }

    // Afficher les détails d'un client
    @GetMapping("/{id}")
    public String viewClientDetails(@PathVariable Long id, Model model) {
        Client client = clientService.getClientById(id);
        List<ClientTask> clientTasks = clientService.getClientTasks(id);
        model.addAttribute("client", client);
        model.addAttribute("clientTasks", clientTasks);
        return "clients/detail";
    }

    // Marquer un questionnaire comme complété
    @PostMapping("/{id}/complete")
    public String completeQuestionnaire(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();

            clientService.completeQuestionnaire(id, userEmail);
            redirectAttributes.addFlashAttribute("success", "Questionnaire marqué comme complété");
            return "redirect:/clients";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/clients/" + id;
        }
    }

    // Helper method pour ajouter les enums au modèle
    private void addEnumAttributesToModel(Model model) {
        model.addAttribute("prisesContact", PriseContact.values());
        model.addAttribute("raisonsNonRenouvellement", RaisonNonRenouvellement.values());
        model.addAttribute("qualitesService", QualiteService.values());
        model.addAttribute("facteursInfluence", FacteurInfluence.values());
    }
}