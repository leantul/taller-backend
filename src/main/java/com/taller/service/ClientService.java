package com.taller.service;

import com.taller.model.Client;
import com.taller.model.Device;
import com.taller.model.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    public void delete(Client client) {
        clientRepository.delete(client);
    }

    public Client findById(String id) {
        return clientRepository.findById(id).orElse(null);
    }

    public List<Device> findDevicesByClientId(String id) {
        return clientRepository.findDevicesByClientId(id);
    }
}
