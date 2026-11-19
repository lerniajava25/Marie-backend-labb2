package com.example.helpdesk.controller;

import com.example.helpdesk.model.*;
import com.example.helpdesk.service.CommentService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.helpdesk.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.helpdesk.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;
    private final CommentService commentService;

    public TicketController(TicketService ticketService, UserService userService, CommentService commentService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.commentService = commentService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/tickets";
    }

    @GetMapping("/tickets")
    public String showTickets(Model model, Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

        if (user.getRole() == Role.USER) {
            model.addAttribute("tickets", ticketService.getTicketsByUserId(user.getId()));
            model.addAttribute("isUser", true);
        } else {
            model.addAttribute("tickets", ticketService.getAllTickets());
            model.addAttribute("isUser", false);
        }

        return "tickets";
    }

    @GetMapping("/tickets/{id}")
    public String showTicketDetails(@PathVariable Long id,
                                    Model model,
                                    Authentication authentication) {
        Ticket ticket = ticketService.getTicketById(id)
                .orElseThrow(() -> new RuntimeException("Ärendet hittades inte"));

        User user = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Du har inte behörighet att visa detta ärende");
        }

        model.addAttribute("ticket", ticket);
        model.addAttribute("agentView", false);
        model.addAttribute("comments", commentService.getCommentsByTicketId(id));

        return "ticket-details";
    }

    @PostMapping("/tickets/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @RequestParam String message,
                             Authentication authentication) {

        Ticket ticket = ticketService.getTicketById(id)
                .orElseThrow(() -> new RuntimeException("Ärendet hittades inte"));

        User user = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Du har inte behörighet att kommentera detta ärende");
        }

        Comment comment = new Comment();
        comment.setMessage(message);
        comment.setTicket(ticket);
        comment.setUser(user);

        commentService.saveComment(comment);

        return "redirect:/tickets/" + id;
    }

    @GetMapping("/tickets/new")
    public String showNewTicketForm() {
        return "new-ticket";
    }

    @PostMapping("/tickets/new")
    public  String createTicket(@RequestParam String title,
                                @RequestParam String description,
                                Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setUser(user);
        ticketService.saveTicket(ticket);

        return "redirect:/tickets";
    }

    @GetMapping("/agent/tickets")
    public String showAllTickets(Model model) {
        model.addAttribute("tickets", ticketService.getAllTickets());
        return "agent-tickets";
    }

    @GetMapping("/agent/tickets/{id}")
    public String showAgentTicketDetails(@PathVariable Long id, Model model, Authentication authentication) {
        Ticket ticket = ticketService.getTicketById(id)
                .orElseThrow(() -> new RuntimeException("Ärendet hittades inte"));
        User user = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

        model.addAttribute("ticket", ticket);
        model.addAttribute("comments", commentService.getCommentsByTicketId(id));
        model.addAttribute("agentView", true);
        model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);

        return "ticket-details";
    }

    @PostMapping("/agent/tickets/{id}/assign")
    public String assignTicketToMe(@PathVariable Long id,
                                   Authentication authentication) {

        Ticket ticket = ticketService.getTicketById(id)
                .orElseThrow(() -> new RuntimeException("Ärendet hittades inte"));

        User agent = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

        if (ticket.getAssignedAgent() != null &&
                !ticket.getAssignedAgent().getId().equals(agent.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Ärendet är redan tilldelat en annan agent");
        }

        ticket.setAssignedAgent(agent);
        ticketService.saveTicket(ticket);

        return "redirect:/agent/tickets/" + id;
    }

    @PostMapping("/agent/tickets/{id}/comments")
    public String addAgentComment(@PathVariable Long id,
                                  @RequestParam String message,
                                  Authentication authentication) {

        Ticket ticket = ticketService.getTicketById(id)
                .orElseThrow(() -> new RuntimeException("Ärendet hittades inte"));

        User user = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

        Comment comment = new Comment();
        comment.setMessage(message);
        comment.setTicket(ticket);
        comment.setUser(user);

        commentService.saveComment(comment);

        return "redirect:/agent/tickets/" + id;
    }

    @PostMapping("/agent/tickets/{id}/status")
    public String updateTicketStatus(@PathVariable Long id,
                                     @RequestParam TicketStatus status,
                                     Authentication authentication) {

        Ticket ticket = ticketService.getTicketById(id)
                .orElseThrow(() -> new RuntimeException("Ärendet hittades inte"));

        User agent = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

        if (agent.getRole() != Role.ADMIN &&
                (ticket.getAssignedAgent() == null ||
                        !ticket.getAssignedAgent().getId().equals(agent.getId()))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Du kan bara ändra status på ärenden som är tilldelade till dig");
        }

        ticket.setStatus(status);
        ticketService.saveTicket(ticket);

        return "redirect:/agent/tickets/" + id;
    }
}
