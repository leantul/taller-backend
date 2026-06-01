package com.taller.service;

import com.taller.model.Client;
import com.taller.model.repository.ClientRepository;
import com.taller.resource.dto.ClientDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public List<ClientDTO> findAll() {
        return clientRepository.findAll().stream().map(this::toDto).toList();
    }

    public ClientDTO save(ClientDTO clientDTO) {
        Client client = new Client();
        client.setName(clientDTO.getName());
        client.setLastName(clientDTO.getLastName());
        client.setDni(clientDTO.getDni());
        client.setEmail(clientDTO.getEmail());
        client.setAddress(clientDTO.getAddress());
        client.setPhone(clientDTO.getPhone());
        client.setBirthDate(clientDTO.getBirthDate());
        client.setNotes(clientDTO.getNotes());
        client.setPhones(clientDTO.getPhones());
        client.setEmails(clientDTO.getEmails());

        if (clientDTO.getId() != null) {
            client.setId(clientDTO.getId());
        }

        return toDto(clientRepository.save(client));
    }

    public void delete(String id) {
        clientRepository.deleteById(id);
    }

    public ClientDTO findById(String id) {
        return clientRepository.findById(id).map(this::toDto).orElse(null);
    }

    public List<ClientDTO> search(String term) {
        return clientRepository.search(term).stream().map(this::toDto).toList();
    }

    private ClientDTO toDto(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setLastName(client.getLastName());
        dto.setDni(client.getDni());
        dto.setEmail(client.getEmail());
        dto.setAddress(client.getAddress());
        dto.setPhone(client.getPhone());
        dto.setBirthDate(client.getBirthDate());
        dto.setNotes(client.getNotes());
        dto.setPhones(client.getPhones());
        dto.setEmails(client.getEmails());
        return dto;
    }

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
}
