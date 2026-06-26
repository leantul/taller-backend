package com.taller.service;

import com.taller.model.Device;
import com.taller.model.DeviceType;
import com.taller.model.repository.DeviceObservationRepository;
import com.taller.model.repository.DevicePasswordHistoryRepository;
import com.taller.model.repository.DeviceRepository;
import com.taller.model.repository.DeviceTypeRepository;
import com.taller.model.repository.projection.DeviceListView;
import com.taller.resource.dto.DeviceDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private DeviceTypeRepository deviceTypeRepository;
    @Mock
    private DevicePasswordHistoryRepository devicePasswordHistoryRepository;
    @Mock
    private DeviceObservationRepository deviceObservationRepository;

    @Test
    void save_usesCatalogTypeAndReturnsFriendlyName() {
        DeviceType notebook = new DeviceType("Notebook");
        notebook.setId("type-id");
        when(deviceTypeRepository.findById("type-id")).thenReturn(Optional.of(notebook));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
            Device device = invocation.getArgument(0);
            device.setId("device-id");
            return device;
        });
        when(devicePasswordHistoryRepository.findFirstByDeviceIdAndIsCurrentTrue("device-id")).thenReturn(Optional.empty());
        when(devicePasswordHistoryRepository.findByDeviceIdOrderByCreationDateTimeDesc("device-id")).thenReturn(List.of());
        when(deviceObservationRepository.findByDeviceIdOrderByObservedAtDesc("device-id")).thenReturn(List.of());

        DeviceDTO request = new DeviceDTO();
        request.setBrand("Marca");
        request.setModel("Modelo");
        request.setClientId("client-id");
        request.setDeviceTypeId("type-id");

        DeviceDTO saved = service().save(request);

        assertEquals("type-id", saved.getDeviceTypeId());
        assertEquals("Notebook", saved.getDeviceTypeName());
    }

    @Test
    void findPage_passesGlobalSortToRepository() {
        DeviceListView row = mock(DeviceListView.class);
        when(row.getId()).thenReturn("device-id");
        when(row.getBrand()).thenReturn("Marca");
        when(row.getModel()).thenReturn("Modelo");
        when(deviceRepository.findPage("mac", "", "", "client", "asc", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(row), PageRequest.of(0, 10), 1));
        when(deviceObservationRepository.findByDeviceIdInAndResolvedAtIsNullOrderByObservedAtDesc(List.of("device-id")))
                .thenReturn(List.of());

        service().findPage(0, 10, " mac ", "", "", "client", "asc");

        verify(deviceRepository).findPage("mac", "", "", "client", "asc", PageRequest.of(0, 10));
    }

    private DeviceService service() {
        return new DeviceService(
                deviceRepository,
                deviceTypeRepository,
                devicePasswordHistoryRepository,
                deviceObservationRepository);
    }
}
