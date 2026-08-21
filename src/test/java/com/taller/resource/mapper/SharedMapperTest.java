package com.taller.resource.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.taller.model.DeviceObservation;
import com.taller.model.RepairPart;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SharedMapperTest {

    @Test
    void mapsEveryDeviceObservationField() {
        LocalDateTime observedAt = LocalDateTime.of(2026, 7, 30, 10, 15);
        LocalDateTime followUpAt = observedAt.plusMonths(3);
        LocalDateTime resolvedAt = observedAt.plusDays(1);
        DeviceObservation observation = DeviceObservation.builder()
                .deviceId("device-id")
                .repairId("repair-id")
                .note("note")
                .observedAt(observedAt)
                .followUpAt(followUpAt)
                .resolvedAt(resolvedAt)
                .build();
        observation.setId("observation-id");

        var result = DeviceObservationMapper.toDto(observation);

        assertThat(result.getId()).isEqualTo("observation-id");
        assertThat(result.getDeviceId()).isEqualTo("device-id");
        assertThat(result.getRepairId()).isEqualTo("repair-id");
        assertThat(result.getNote()).isEqualTo("note");
        assertThat(result.getObservedAt()).isEqualTo(observedAt);
        assertThat(result.getFollowUpAt()).isEqualTo(followUpAt);
        assertThat(result.getResolvedAt()).isEqualTo(resolvedAt);
    }

    @Test
    void mapsEveryRepairPartField() {
        RepairPart part = RepairPart.builder()
                .repairId("repair-id")
                .name("part")
                .quantity(2)
                .provider("provider")
                .cost(new BigDecimal("12.50"))
                .salePrice(new BigDecimal("20.00"))
                .build();
        part.setId("part-id");

        var result = RepairPartMapper.toDto(part);

        assertThat(result.getId()).isEqualTo("part-id");
        assertThat(result.getRepairId()).isEqualTo("repair-id");
        assertThat(result.getName()).isEqualTo("part");
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getProvider()).isEqualTo("provider");
        assertThat(result.getCost()).isEqualByComparingTo("12.50");
        assertThat(result.getSalePrice()).isEqualByComparingTo("20.00");
    }
}
