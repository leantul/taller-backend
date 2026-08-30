package com.taller.resource.controller;

import com.taller.resource.dto.CommitmentOutcomeUpdateDTO;
import com.taller.resource.dto.FollowUpCommitmentDTO;
import com.taller.resource.dto.FollowUpDetailDTO;
import com.taller.resource.dto.FollowUpListItemDTO;
import com.taller.resource.dto.FollowUpSaveDTO;
import com.taller.resource.dto.PageDTO;
import com.taller.service.FollowUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow-up")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @GetMapping("/page")
    public PageDTO<FollowUpListItemDTO> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String term,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return followUpService.findPage(page, size, term, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public FollowUpDetailDTO getById(@PathVariable String id) {
        return followUpService.findById(id);
    }

    @PostMapping
    public FollowUpDetailDTO save(@Valid @RequestBody FollowUpSaveDTO dto) {
        return followUpService.save(dto);
    }

    @PostMapping("/{id}/commitments")
    public FollowUpCommitmentDTO addCommitment(@PathVariable String id, @Valid @RequestBody FollowUpCommitmentDTO dto) {
        return followUpService.addCommitment(id, dto);
    }

    @PatchMapping("/{id}/commitments/{commitmentId}/outcome")
    public FollowUpCommitmentDTO updateOutcome(@PathVariable String id, @PathVariable String commitmentId,
                                                @Valid @RequestBody CommitmentOutcomeUpdateDTO dto) {
        return followUpService.updateCommitmentOutcome(id, commitmentId, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        followUpService.delete(id);
    }
}
