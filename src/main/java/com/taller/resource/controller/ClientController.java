package com.taller.resource.controller;

import com.taller.model.Client;
import com.taller.resource.dto.ClientDTO;
import com.taller.resource.dto.DeviceDTO;
import com.taller.resource.mapper.ClientMapper;
import com.taller.resource.mapper.DeviceMapper;
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
    private final ClientMapper clientMapper;
    private final DeviceMapper deviceMapper;

    @GetMapping
    public List<ClientDTO> getClients() {
        return clientMapper.clientToClientDTO(clientService.findAll());
    }

    @GetMapping("/{id}/devices")
    public List<DeviceDTO> getDevicesFromAClient(@PathVariable String id) {
        return deviceMapper.deviceToDeviceDTOList(clientService.findDevicesByClientId(id));
    }

    @PostMapping
    public Client saveClient(@RequestBody ClientDTO clientDTO) {
        return clientService.save(clientMapper.clientDTOToClient(clientDTO));
    }

    @DeleteMapping
    public void deleteClient(@RequestBody ClientDTO clientDTO) {
        clientService.delete(clientService.findById(clientDTO.getId()));
    }
}
