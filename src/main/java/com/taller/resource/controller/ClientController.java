package com.taller.resource.controller;

import com.taller.resource.dto.ClientDTO;
import com.taller.resource.dto.ClientHistoryDTO;
import com.taller.resource.dto.ClientListItemDTO;
import com.taller.resource.dto.PageDTO;
import com.taller.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public List<ClientDTO> getClients() {
        return clientService.findAll();
    }

    @GetMapping("/page")
    public PageDTO<ClientListItemDTO> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String term,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return clientService.findPage(page, size, term, sortBy, sortDir);
    }

    @GetMapping("/{id}/history")
    public ClientHistoryDTO getHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "true") boolean includeClient) {
        return clientService.findHistory(id, page, size, includeClient);
    }

    @GetMapping("/{id}")
    public ClientDTO getById(@PathVariable String id) {
        return clientService.findById(id);
    }

    @GetMapping("/search")
    public List<ClientDTO> search(
            @RequestParam String term,
            @RequestParam(defaultValue = "20") int limit) {
        return clientService.search(term, limit);
    }

    @PostMapping
    public ClientDTO saveClient(@RequestBody ClientDTO clientDTO) {
        return clientService.save(clientDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteClient(@PathVariable String id) {
        clientService.delete(id);
    }
}
