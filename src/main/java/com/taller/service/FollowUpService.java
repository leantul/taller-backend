package com.taller.service;

import com.taller.model.Client;
import com.taller.model.FollowUp;
import com.taller.model.FollowUpCommitment;
import com.taller.model.enums.CommitmentOutcomeEnum;
import com.taller.model.enums.FollowUpStatusEnum;
import com.taller.model.repository.ClientRepository;
import com.taller.model.repository.FollowUpCommitmentRepository;
import com.taller.model.repository.FollowUpRepository;
import com.taller.model.repository.projection.FollowUpListView;
import com.taller.resource.dto.CommitmentOutcomeUpdateDTO;
import com.taller.resource.dto.FollowUpCommitmentDTO;
import com.taller.resource.dto.FollowUpDetailDTO;
import com.taller.resource.dto.FollowUpListItemDTO;
import com.taller.resource.dto.FollowUpSaveDTO;
import com.taller.resource.dto.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.taller.service.support.PageSupport.boundedPageRequest;
import static com.taller.service.support.PageSupport.normalizeSortDirection;
import static com.taller.service.support.PageSupport.normalizeTerm;
import static com.taller.service.support.PageSupport.toPageDto;

@Service
@RequiredArgsConstructor
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final FollowUpCommitmentRepository commitmentRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public PageDTO<FollowUpListItemDTO> findPage(int page, int size, String term, String sortBy, String sortDir) {
        Page<FollowUpListView> result = followUpRepository.findPage(
                normalizeTerm(term), normalizeSortBy(sortBy), normalizeSortDirection(sortDir),
                boundedPageRequest(page, size, 100));
        return toPageDto(result, result.getContent().stream().map(this::toListItem).toList());
    }

    @Transactional(readOnly = true)
    public FollowUpDetailDTO findById(String id) {
        FollowUp followUp = findEntity(id);
        List<FollowUpCommitmentDTO> commitments = commitmentRepository
                .findByFollowUpIdOrderByCreationDateTimeDesc(id).stream()
                .map(this::toCommitmentDto)
                .toList();
        return toDetailDto(followUp, commitments);
    }

    @Transactional
    public FollowUpDetailDTO save(FollowUpSaveDTO dto) {
        FollowUp followUp = dto.id() == null ? new FollowUp() : findEntity(dto.id());
        Client client = dto.clientId() == null || dto.clientId().isBlank() ? null : clientRepository.findById(dto.clientId())
                .orElseThrow(() -> new IllegalArgumentException("El cliente indicado no existe"));
        validateContact(client, dto.contactName(), dto.contactValue());

        followUp.setClient(client);
        followUp.setContactName(trimToNull(dto.contactName()));
        followUp.setContactChannel(trimToNull(dto.contactChannel()));
        followUp.setContactValue(trimToNull(dto.contactValue()));
        followUp.setDeviceDescription(dto.deviceDescription().trim());
        followUp.setReportedProblem(trimToNull(dto.reportedProblem()));
        followUp.setNextContactDate(dto.nextContactDate());
        followUp.setStatus(dto.status() == null ? FollowUpStatusEnum.PENDING : dto.status());
        followUp.setNotes(trimToNull(dto.notes()));
        FollowUp saved = followUpRepository.save(followUp);
        return toDetailDto(saved, saved.getCommitments().stream().map(this::toCommitmentDto).toList());
    }

    @Transactional
    public FollowUpCommitmentDTO addCommitment(String followUpId, FollowUpCommitmentDTO dto) {
        FollowUp followUp = findEntity(followUpId);
        commitmentRepository.findByFollowUpIdAndOutcome(followUpId, CommitmentOutcomeEnum.PENDING)
                .forEach(existing -> existing.setOutcome(CommitmentOutcomeEnum.RESCHEDULED));

        FollowUpCommitment commitment = new FollowUpCommitment();
        commitment.setFollowUp(followUp);
        commitment.setPromisedDate(dto.promisedDate());
        commitment.setNotes(trimToNull(dto.notes()));
        commitment.setOutcome(CommitmentOutcomeEnum.PENDING);
        followUp.setStatus(FollowUpStatusEnum.CONFIRMED);
        return toCommitmentDto(commitmentRepository.save(commitment));
    }

    @Transactional
    public FollowUpCommitmentDTO updateCommitmentOutcome(String followUpId, String commitmentId, CommitmentOutcomeUpdateDTO dto) {
        FollowUpCommitment commitment = commitmentRepository.findById(commitmentId)
                .filter(item -> item.getFollowUp().getId().equals(followUpId))
                .orElseThrow(() -> new IllegalArgumentException("El compromiso indicado no existe"));
        commitment.setOutcome(dto.outcome());
        if (dto.outcome() == CommitmentOutcomeEnum.COMPLETED) {
            commitment.getFollowUp().setStatus(FollowUpStatusEnum.COMPLETED);
        }
        return toCommitmentDto(commitmentRepository.save(commitment));
    }

    @Transactional
    public void delete(String id) {
        if (!followUpRepository.existsById(id)) {
            throw new IllegalArgumentException("El seguimiento indicado no existe");
        }
        followUpRepository.deleteById(id);
    }

    private FollowUp findEntity(String id) {
        return followUpRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El seguimiento indicado no existe"));
    }

    private void validateContact(Client client, String contactName, String contactValue) {
        if (client == null && (contactName == null || contactName.isBlank())) {
            throw new IllegalArgumentException("Indicá un cliente o el nombre del contacto");
        }
        if (client == null && (contactValue == null || contactValue.isBlank())) {
            throw new IllegalArgumentException("Indicá cómo contactar a la persona");
        }
    }

    private FollowUpListItemDTO toListItem(FollowUpListView view) {
        String clientName = String.join(" ", List.of(
                view.getClientName() == null ? "" : view.getClientName(),
                view.getClientLastName() == null ? "" : view.getClientLastName())).trim();
        return new FollowUpListItemDTO(view.getId(), view.getClientId(),
                clientName.isBlank() ? view.getContactName() : clientName,
                view.getContactChannel(), view.getContactValue(), view.getDeviceDescription(),
                view.getNextContactDate(), view.getCurrentPromisedDate(), view.getStatus(),
                valueOrZero(view.getCommitmentCount()), valueOrZero(view.getMissedCommitmentCount()));
    }

    private FollowUpDetailDTO toDetailDto(FollowUp followUp, List<FollowUpCommitmentDTO> commitments) {
        Client client = followUp.getClient();
        String clientName = client == null ? null : (client.getName() + " " + client.getLastName()).trim();
        return new FollowUpDetailDTO(followUp.getId(), client == null ? null : client.getId(), clientName,
                followUp.getContactName(), followUp.getContactChannel(), followUp.getContactValue(),
                followUp.getDeviceDescription(), followUp.getReportedProblem(), followUp.getNextContactDate(),
                followUp.getStatus(), followUp.getNotes(), commitments);
    }

    private FollowUpCommitmentDTO toCommitmentDto(FollowUpCommitment commitment) {
        return new FollowUpCommitmentDTO(commitment.getId(), commitment.getPromisedDate(), commitment.getOutcome(),
                commitment.getNotes(), commitment.getCreationDateTime());
    }

    private String normalizeSortBy(String sortBy) {
        return switch (sortBy == null ? "" : sortBy.trim()) {
            case "name", "nextContactDate", "commitmentCount" -> sortBy.trim();
            default -> "createdAt";
        };
    }

    private long valueOrZero(Long value) {
        return value == null ? 0 : value;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
