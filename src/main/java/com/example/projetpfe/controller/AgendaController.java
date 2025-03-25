package com.example.projetpfe.controller;

import com.example.projetpfe.dto.ClientTaskDTO;
import com.example.projetpfe.entity.Client;
import com.example.projetpfe.entity.ClientStatus;
import com.example.projetpfe.entity.ClientTask;
import com.example.projetpfe.entity.ClientTaskStatus;
import com.example.projetpfe.repository.ClientRepository;
import com.example.projetpfe.service.Impl.ClientService;
import com.example.projetpfe.service.Impl.ClientTaskService;
import com.example.projetpfe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/agenda")
public class AgendaController {

    private final ClientTaskService clientTaskService;
    private final UserService userService;
    private final ClientService clientService;
    private final ClientRepository clientRepository;

    @Autowired
    public AgendaController(ClientTaskService clientTaskService, UserService userService, ClientService clientService, ClientRepository clientRepository) {
        this.clientTaskService = clientTaskService;
        this.userService = userService;
        this.clientService = clientService;
        this.clientRepository = clientRepository;
    }

    // Afficher l'agenda de l'utilisateur connecté
    @GetMapping
    public String viewAgenda(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        // Récupérer les clients par statut
        Map<String, List<Client>> clients = new HashMap<>();
        clients.put("nonTraites", clientService.findClientsByStatus(ClientStatus.NON_TRAITE));
        clients.put("absents", clientService.findClientsByStatus(ClientStatus.ABSENT));

        // Récupérer aussi les tâches de rappel pour les absents (peut-être pas implémenté encore)
        List<ClientTask> callbackTasks = clientTaskService.getUserCallbackTasks(userEmail);

        // Statistiques par statut
        Map<String, Long> stats = new HashMap<>();
        stats.put("nonTraites", clientRepository.countByStatus(ClientStatus.NON_TRAITE));
        stats.put("contactes", clientRepository.countByStatus(ClientStatus.CONTACTE));
        stats.put("absents", clientRepository.countByStatus(ClientStatus.ABSENT));
        stats.put("refus", clientRepository.countByStatus(ClientStatus.REFUS));

        model.addAttribute("clients", clients);
        model.addAttribute("callbackTasks", callbackTasks);
        model.addAttribute("stats", stats);

        return "agenda/index";
    }

    // Afficher le détail d'une tâche
    @GetMapping("/{taskId}")
    public String viewTaskDetail(@PathVariable Long taskId, Model model) {
        ClientTask task = clientTaskService.getTaskById(taskId);
        model.addAttribute("task", task);
        return "agenda/detail";
    }

    // Formulaire pour mettre à jour le statut d'une tâche
    @GetMapping("/{taskId}/update")
    public String showUpdateForm(@PathVariable Long taskId, Model model) {
        ClientTask task = clientTaskService.getTaskById(taskId);
        model.addAttribute("task", task);
        model.addAttribute("statuses", ClientTaskStatus.values());
        return "agenda/update";
    }

    // Traiter une tâche (marquer comme complétée)
    @PostMapping("/{taskId}/complete")
    public String completeTask(@PathVariable Long taskId,
                               @RequestParam(required = false) String notes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        clientTaskService.updateTaskStatus(taskId, ClientTaskStatus.COMPLETED, notes, userEmail);
        return "redirect:/agenda";
    }

    // Planifier un rappel
    @PostMapping("/{taskId}/callback")
    public String scheduleCallback(@PathVariable Long taskId,
                                   @RequestParam LocalDateTime callbackDate,
                                   @RequestParam(required = false) String notes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        clientTaskService.scheduleCallback(taskId, callbackDate, notes, userEmail);
        return "redirect:/agenda";
    }

    // Afficher l'historique des tâches complétées
    @GetMapping("/history")
    public String viewHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        List<ClientTask> history = clientTaskService.getUserHistory(userEmail);
        model.addAttribute("completedTasks", history);

        return "agenda/history";
    }
    @PostMapping("/{taskId}/update")
    public String processUpdateForm(@PathVariable Long taskId,
                                    @ModelAttribute("task") ClientTask taskForm,
                                    BindingResult result) {

        if (result.hasErrors()) {
            // S'il y a des erreurs, retournez à la page du formulaire
            return "agenda/update";
        }

        // Récupérer l'utilisateur actuel
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        // Mettre à jour la tâche
        clientTaskService.updateTask(taskId, taskForm, userEmail);

        // Rediriger vers l'agenda après mise à jour
        return "redirect:/agenda";
    }
}