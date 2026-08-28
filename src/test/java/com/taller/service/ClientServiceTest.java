package com.taller.service;

import com.taller.model.Client;
import com.taller.model.enums.RepairStatusEnum;
import com.taller.model.repository.ClientRepository;
import com.taller.model.repository.RepairRepository;
import com.taller.model.repository.projection.ClientBasicView;
import com.taller.model.repository.projection.ClientDetailView;
import com.taller.model.repository.projection.ClientListView;
import com.taller.model.repository.projection.ClientRepairHistoryView;
import com.taller.resource.dto.ClientHistoryDTO;
import com.taller.resource.dto.ClientDTO;
import com.taller.resource.dto.PageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private RepairRepository repairRepository;

    private ClientService service;

    @BeforeEach
    void setUp() {
        service = new ClientService(clientRepository, repairRepository);
    }

    @Test
    void findPage_mapsOnlyListFieldsAndCounts() {
        ClientListView row = mock(ClientListView.class);
        when(row.getId()).thenReturn("c1");
        when(row.getName()).thenReturn("Ada");
        when(row.getLastName()).thenReturn("Lovelace");
        when(row.getDeviceCount()).thenReturn(3L);
        when(row.getRepairCount()).thenReturn(7L);
        when(clientRepository.findPage("ada", "repairCount", "desc", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(row), PageRequest.of(0, 10), 1));

        PageDTO<?> result = service.findPage(0, 10, " ada ", "repairCount", "desc");

        assertEquals(1, result.totalElements());
        assertEquals(3L, ((com.taller.resource.dto.ClientListItemDTO) result.content().getFirst()).deviceCount());
        assertEquals(7L, ((com.taller.resource.dto.ClientListItemDTO) result.content().getFirst()).repairCount());
    }

    @Test
    void findHistory_returnsDetailAndPagedRepairs() {
        ClientDetailView client = mock(ClientDetailView.class);
        when(client.getId()).thenReturn("c1");
        when(client.getName()).thenReturn("Ada");
        when(client.getReference()).thenReturn("Hermana de Grace");
        when(clientRepository.findDetailById("c1")).thenReturn(Optional.of(client));
        when(clientRepository.findAdditionalPhonesById("c1")).thenReturn(List.of("222"));
        when(clientRepository.findAdditionalEmailsById("c1")).thenReturn(List.of());

        ClientRepairHistoryView repair = mock(ClientRepairHistoryView.class);
        when(repair.getId()).thenReturn("r1");
        when(repair.getStatus()).thenReturn(RepairStatusEnum.RETIRADA);
        when(repair.getReceiveDateTime()).thenReturn(LocalDateTime.of(2026, 6, 1, 10, 0));
        when(repairRepository.findClientHistory("c1", PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(repair), PageRequest.of(0, 5), 1));

        ClientHistoryDTO result = service.findHistory("c1", 0, 5, true);

        assertEquals("Ada", result.client().name());
        assertEquals("Hermana de Grace", result.client().reference());
        assertEquals("r1", result.repairs().content().getFirst().id());
    }

    @Test
    void findHistory_skipsClientQueriesWhenDetailIsNotRequested() {
        when(repairRepository.findClientHistory("c1", PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 5), 0));

        ClientHistoryDTO result = service.findHistory("c1", 0, 5, false);

        assertNull(result.client());
        verify(repairRepository).findClientHistory("c1", PageRequest.of(0, 5));
    }

    @Test
    void search_skipsRepositoryForTermsShorterThanTwoCharacters() {
        assertEquals(List.of(), service.search(" a ", 20));

        verify(clientRepository, never()).search(anyString(), any());
    }

    @Test
    void search_normalizesTermAndUsesRequestedLimit() {
        ClientBasicView row = mock(ClientBasicView.class);
        when(row.getId()).thenReturn("c1");
        when(row.getName()).thenReturn("Ada");
        when(row.getLastName()).thenReturn("Lovelace");
        when(clientRepository.search("ada", PageRequest.of(0, 20))).thenReturn(List.of(row));

        List<ClientDTO> result = service.search(" ada ", 20);

        assertEquals(1, result.size());
        assertEquals("c1", result.getFirst().getId());
    }

    @Test
    void search_capsResultLimitAtFifty() {
        when(clientRepository.search("ada", PageRequest.of(0, 50))).thenReturn(List.of());

        service.search("ada", 500);

        verify(clientRepository).search("ada", PageRequest.of(0, 50));
    }

    @Test
    void save_existingClient_updatesManagedEntityInsteadOfRecreatingIt() {
        Client existing = new Client();
        existing.setId("c1");
        existing.setName("Anterior");
        List<String> managedPhones = new ArrayList<>(List.of("111"));
        List<String> managedEmails = new ArrayList<>(List.of("anterior@example.com"));
        existing.setPhones(managedPhones);
        existing.setEmails(managedEmails);
        ClientDTO update = new ClientDTO();
        update.setId("c1");
        update.setName("Ada");
        update.setPhones(List.of("222", "333"));
        update.setEmails(List.of("ada@example.com"));
        when(clientRepository.findById("c1")).thenReturn(Optional.of(existing));
        when(clientRepository.save(existing)).thenReturn(existing);

        ClientDTO result = service.save(update);

        assertEquals("Ada", result.getName());
        assertSame(managedPhones, existing.getPhones());
        assertSame(managedEmails, existing.getEmails());
        assertEquals(List.of("222", "333"), result.getPhones());
        assertEquals(List.of("ada@example.com"), result.getEmails());
        verify(clientRepository).save(existing);
    }
}
