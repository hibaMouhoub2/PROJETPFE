package com.example.projetpfe.controller;

import com.example.projetpfe.dto.ClientTaskDTO;
import com.example.projetpfe.entity.ClientTask;
import com.example.projetpfe.entity.ClientTaskStatus;
import com.example.projetpfe.entity.User;
import com.example.projetpfe.service.Impl.ClientService;
import com.example.projetpfe.service.Impl.ClientTaskService;
import com.example.projetpfe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/tasks")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTaskController {

    private final ClientTaskService clientTaskService;
    private final UserService userService;
    private final ClientService clientService;

    @Autowired
    public AdminTaskController(ClientTaskService clientTaskService,
                               UserService userService,
                               ClientService clientService) {
        this.clientTaskService = clientTaskService;
        this.userService = userService;
        this.clientService = clientService;
    }


    // Afficher toutes les tâches clients
    @GetMapping
    public String viewAllTasks(Model model,
                               @RequestParam(required = false) Long userId,
                               @RequestParam(required = false) ClientTaskStatus status) {

        // Récupérer la liste des utilisateurs pour le filtre
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("statuses", ClientTaskStatus.values());

        // Appliquer les filtres si présents
        List<ClientTask> tasks;

        if (userId != null) {
            tasks = clientTaskService.getTasksByUser(userId);
            model.addAttribute("selectedUser", userId);
        } else if (status != null) {
            tasks = clientTaskService.getTasksByStatus(status);
            model.addAttribute("selectedStatus", status);
        } else {
            tasks = clientTaskService.getAllTasks();
        }

        model.addAttribute("tasks", tasks);
        return "admin/tasks";
    }

    // Afficher les tâches non assignées
    @GetMapping("/unassigned")
    public String viewUnassignedTasks(Model model) {
        List<ClientTask> unassignedTasks = clientTaskService.getUnassignedTasks();
        model.addAttribute("tasks", unassignedTasks);
        model.addAttribute("users", userService.findAllUsers());
        return "admin/unassigned-tasks";
    }

    // Formulaire d'assignation d'une tâche
    @GetMapping("/{taskId}/assign")
    public String showAssignForm(@PathVariable Long taskId, Model model) {
        ClientTask task = clientTaskService.getTaskById(taskId);
        model.addAttribute("task", task);
        model.addAttribute("users", userService.findAllUsers());
        return "admin/assign-task";
    }

    // Traiter l'assignation d'une tâche
    @PostMapping("/{taskId}/assign")
    public String processAssignment(@PathVariable Long taskId,
                                    @RequestParam Long userId,
                                    RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = auth.getName();

            clientTaskService.assignTaskToUser(taskId, userId, adminEmail);
            redirectAttributes.addFlashAttribute("success", "La tâche a été assignée avec succès");
            return "redirect:/admin/tasks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'assignation: " + e.getMessage());
            return "redirect:/admin/tasks";
        }
    }

    // Traiter l'assignation multiple de tâches
    @PostMapping("/assign-multiple")
    public String assignMultipleTasks(@RequestParam List<Long> taskIds,
                                      @RequestParam Long userId,
                                      RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = auth.getName();

            clientTaskService.assignMultipleTasks(taskIds, userId, adminEmail);
            redirectAttributes.addFlashAttribute("success", taskIds.size() + " tâches ont été assignées avec succès");
            return "redirect:/admin/tasks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'assignation multiple: " + e.getMessage());
            return "redirect:/admin/tasks";
        }
    }

    // Créer une nouvelle tâche client
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new ClientTaskDTO());
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("clients", clientService.findAllClients());
        return "admin/create-task";
    }

    // Traiter la création d'une tâche
    // Traiter la création d'une tâche
    @PostMapping("/create")
    public String processCreate(@ModelAttribute("task") ClientTaskDTO taskDTO,
                                RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = auth.getName();

            clientTaskService.createClientTask(taskDTO, adminEmail);
            redirectAttributes.addFlashAttribute("success", "La tâche a été créée avec succès");
            return "redirect:/admin/tasks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création: " + e.getMessage());
            return "redirect:/admin/tasks/create";
        }
    }

    // Vue détaillée d'une tâche
    @GetMapping("/{taskId}")
    public String viewTaskDetail(@PathVariable Long taskId, Model model) {
        ClientTask task = clientTaskService.getTaskById(taskId);
        model.addAttribute("task", task);
        return "admin/task-detail";
    }
}