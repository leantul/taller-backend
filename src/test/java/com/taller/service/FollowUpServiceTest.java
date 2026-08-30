package com.taller.service;

import com.taller.model.Client;
import com.taller.model.FollowUp;
import com.taller.model.FollowUpCommitment;
import com.taller.model.enums.CommitmentOutcomeEnum;
import com.taller.model.enums.FollowUpStatusEnum;
import com.taller.model.repository.ClientRepository;
import com.taller.model.repository.FollowUpCommitmentRepository;
import com.taller.model.repository.FollowUpRepository;
import com.taller.resource.dto.FollowUpCommitmentDTO;
import com.taller.resource.dto.FollowUpSaveDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowUpServiceTest {

    @Mock private FollowUpRepository followUpRepository;
    @Mock private FollowUpCommitmentRepository commitmentRepository;
    @Mock private ClientRepository clientRepository;

    private FollowUpService service;

    @BeforeEach
    void setUp() {
        service = new FollowUpService(followUpRepository, commitmentRepository, clientRepository);
    }

    @Test
    void save_allowsPotentialContactWithoutCreatingClient() {
        FollowUpSaveDTO request = new FollowUpSaveDTO(null, null, " usuario.redes ", "INSTAGRAM",
                "@usuario", " Notebook Lenovo ", "No enciende", LocalDate.of(2026, 9, 2), null, null,
                null, null);
        when(followUpRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.save(request);

        ArgumentCaptor<FollowUp> captor = ArgumentCaptor.forClass(FollowUp.class);
        verify(followUpRepository).save(captor.capture());
        FollowUp saved = captor.getValue();
        assertNull(saved.getClient());
        assertEquals("usuario.redes", saved.getContactName());
        assertEquals("Notebook Lenovo", saved.getDeviceDescription());
        assertEquals(FollowUpStatusEnum.PENDING, saved.getStatus());
        verifyNoInteractions(clientRepository);
    }

    @Test
    void save_linksExistingClientWithoutRequiringPotentialContactFields() {
        Client client = new Client();
        client.setId("client-1");
        client.setName("Ada");
        client.setLastName("Lovelace");
        when(clientRepository.findById("client-1")).thenReturn(Optional.of(client));
        when(followUpRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.save(new FollowUpSaveDTO(null, "client-1", null, null, null,
                "PC", null, null, FollowUpStatusEnum.PENDING, null, null, null));

        ArgumentCaptor<FollowUp> captor = ArgumentCaptor.forClass(FollowUp.class);
        verify(followUpRepository).save(captor.capture());
        assertSame(client, captor.getValue().getClient());
    }

    @Test
    void save_rejectsUnidentifiablePotentialContact() {
        FollowUpSaveDTO request = new FollowUpSaveDTO(null, null, "Persona", "WHATSAPP", " ",
                "Notebook", null, null, null, null, null, null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.save(request));

        assertEquals("Indicá cómo contactar a la persona", error.getMessage());
        verify(followUpRepository, never()).save(any());
    }

    @Test
    void save_registersInitialPromiseUsingCommitmentHistory() {
        LocalDate promisedDate = LocalDate.of(2026, 9, 12);
        when(followUpRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(commitmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.save(new FollowUpSaveDTO(null, null, "Persona", "WHATSAPP", "111",
                "Notebook", null, null, FollowUpStatusEnum.PENDING, null, promisedDate, "Por la mañana"));

        ArgumentCaptor<FollowUpCommitment> captor = ArgumentCaptor.forClass(FollowUpCommitment.class);
        verify(commitmentRepository).save(captor.capture());
        assertEquals(promisedDate, captor.getValue().getPromisedDate());
        assertEquals("Por la mañana", captor.getValue().getNotes());
        assertEquals(CommitmentOutcomeEnum.PENDING, captor.getValue().getOutcome());
        assertEquals(FollowUpStatusEnum.CONFIRMED, captor.getValue().getFollowUp().getStatus());
    }

    @Test
    void addCommitment_preservesHistoryAndMarksPreviousPromiseAsRescheduled() {
        FollowUp followUp = new FollowUp();
        followUp.setId("follow-up-1");
        FollowUpCommitment previous = new FollowUpCommitment();
        previous.setOutcome(CommitmentOutcomeEnum.PENDING);
        when(followUpRepository.findById("follow-up-1")).thenReturn(Optional.of(followUp));
        when(commitmentRepository.findByFollowUpIdAndOutcome("follow-up-1", CommitmentOutcomeEnum.PENDING))
                .thenReturn(List.of(previous));
        when(commitmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.addCommitment("follow-up-1", new FollowUpCommitmentDTO(null,
                LocalDate.of(2026, 9, 10), null, "La trae por la tarde", null));

        assertEquals(CommitmentOutcomeEnum.RESCHEDULED, previous.getOutcome());
        assertEquals(FollowUpStatusEnum.CONFIRMED, followUp.getStatus());
        ArgumentCaptor<FollowUpCommitment> captor = ArgumentCaptor.forClass(FollowUpCommitment.class);
        verify(commitmentRepository).save(captor.capture());
        assertSame(followUp, captor.getValue().getFollowUp());
        assertEquals(CommitmentOutcomeEnum.PENDING, captor.getValue().getOutcome());
    }
}
