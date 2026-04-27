package com.taller.resource.controller;

import com.taller.resource.dto.ClientDTO;
import com.taller.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(originPatterns = "*", maxAge = 3600)
@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public List<ClientDTO> getClients() {
        return clientService.findAll();
    }

    @GetMapping("/{id}")
    public ClientDTO getById(@PathVariable String id) {
        return clientService.findById(id);
    }

    @GetMapping("/search")
    public List<ClientDTO> search(@RequestParam String term) {
        return clientService.search(term);
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
