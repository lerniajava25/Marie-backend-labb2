package com.example.helpdesk.service;

import com.example.helpdesk.model.Ticket;
import com.example.helpdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> getTicketsByUserId(Long userId) {
        return ticketRepository.findByUserId(userId);
    }

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }
}
