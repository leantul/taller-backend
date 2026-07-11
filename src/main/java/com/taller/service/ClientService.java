package com.taller.service;

import com.taller.model.Client;
import com.taller.model.repository.ClientRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.ClientDetailView;
import com.taller.model.repository.projection.ClientListView;
import com.taller.model.repository.projection.ClientRepairHistoryView;
import com.taller.resource.dto.ClientDetailDTO;
import com.taller.resource.dto.ClientDTO;
import com.taller.resource.dto.ClientHistoryDTO;
import com.taller.resource.dto.ClientListItemDTO;
import com.taller.resource.dto.ClientRepairHistoryItemDTO;
import com.taller.resource.dto.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final RepairRepository repairRepository;

    @Transactional(readOnly = true)
    public PageDTO<ClientListItemDTO> findPage(int page, int size, String term, String sortBy, String sortDir) {
        Page<ClientListView> result = clientRepository.findPage(
                normalizeTerm(term),
                normalizeSortBy(sortBy),
                normalizeSortDir(sortDir),
                pageRequest(page, size, 100));
        return toPage(result, result.getContent().stream().map(this::toListItemDto).toList());
    }

    @Transactional(readOnly = true)
    public ClientHistoryDTO findHistory(String id, int page, int size, boolean includeClient) {
        ClientDetailDTO client = includeClient ? findDetail(id) : null;
        Page<ClientRepairHistoryView> result = repairRepository.findClientHistory(id, pageRequest(page, size, 50));
        return new ClientHistoryDTO(
                client,
                toPage(result, result.getContent().stream().map(this::toHistoryItemDto).toList())
        );
    }

    @Transactional(readOnly = true)
    public List<ClientDTO> findAll() {
        return clientRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public ClientDTO save(ClientDTO clientDTO) {
        Client client = clientDTO.getId() == null
                ? new Client()
                : clientRepository.findById(clientDTO.getId())
                        .orElseThrow(() -> new IllegalArgumentException("El cliente indicado no existe"));
        copyEditableFields(clientDTO, client);
        return toDto(clientRepository.save(client));
    }

    private void copyEditableFields(ClientDTO clientDTO, Client client) {
        client.setName(clientDTO.getName());
        client.setLastName(clientDTO.getLastName());
        client.setReference(clientDTO.getReference());
        client.setEmail(clientDTO.getEmail());
        client.setAddress(clientDTO.getAddress());
        client.setPhone(clientDTO.getPhone());
        client.setBirthDate(clientDTO.getBirthDate());
        client.setNotes(clientDTO.getNotes());
        client.setPhones(clientDTO.getPhones() == null ? List.of() : List.copyOf(clientDTO.getPhones()));
        client.setEmails(clientDTO.getEmails() == null ? List.of() : List.copyOf(clientDTO.getEmails()));
    }

    @Transactional
    public void delete(String id) {
        clientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ClientDTO findById(String id) {
        return clientRepository.findById(id).map(this::toDto).orElse(null);
    }

    private ClientDetailDTO findDetail(String id) {
        ClientDetailView client = clientRepository.findDetailById(id).orElse(null);
        if (client == null) {
            return null;
        }
        return new ClientDetailDTO(
                client.getId(), client.getName(), client.getLastName(), client.getReference(), client.getEmail(),
                client.getAddress(), client.getPhone(), client.getNotes(),
                List.copyOf(clientRepository.findAdditionalPhonesById(id)),
                List.copyOf(clientRepository.findAdditionalEmailsById(id)),
                client.getBirthDate()
        );
    }

    @Transactional(readOnly = true)
    public List<ClientDTO> search(String term) {
        return clientRepository.search(term).stream().map(this::toDto).toList();
    }

    private ClientDTO toDto(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setLastName(client.getLastName());
        dto.setReference(client.getReference());
        dto.setEmail(client.getEmail());
        dto.setAddress(client.getAddress());
        dto.setPhone(client.getPhone());
        dto.setBirthDate(client.getBirthDate());
        dto.setNotes(client.getNotes());
        dto.setPhones(client.getPhones());
        dto.setEmails(client.getEmails());
        return dto;
    }

    private ClientListItemDTO toListItemDto(ClientListView client) {
        return new ClientListItemDTO(
                client.getId(),
                client.getName(),
                client.getLastName(),
                client.getPhone(),
                client.getDeviceCount(),
                client.getRepairCount());
    }

    private ClientRepairHistoryItemDTO toHistoryItemDto(ClientRepairHistoryView repair) {
        return new ClientRepairHistoryItemDTO(
                repair.getId(), repair.getOrderNumber(), repair.getStatus(), repair.getDeviceBrand(), repair.getDeviceModel(),
                repair.getReceiveDateTime(), repair.getReturnDateTime()
        );
    }

    private <T> PageDTO<T> toPage(Page<?> page, List<T> content) {
        return new PageDTO<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private String normalizeTerm(String term) {
        return term == null ? "" : term.trim();
    }

    private String normalizeSortBy(String sortBy) {
        return switch (sortBy == null ? "" : sortBy.trim()) {
            case "name", "deviceCount", "repairCount", "phone" -> sortBy.trim();
            default -> "createdAt";
        };
    }

    private String normalizeSortDir(String sortDir) {
        return "asc".equalsIgnoreCase(sortDir) ? "asc" : "desc";
    }

    private PageRequest pageRequest(int page, int size, int maximumSize) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), maximumSize));
    }
}
