package com.taller.service;

import com.taller.model.DeviceType;
import com.taller.model.repository.DeviceTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceTypeServiceTest {

    @Mock
    private DeviceTypeRepository deviceTypeRepository;

    @Test
    void getAll_returnsReadOnlyCatalogInRepositoryOrder() {
        DeviceType notebook = new DeviceType("Notebook");
        notebook.setId("notebook-id");
        DeviceType tablet = new DeviceType("Tablet");
        tablet.setId("tablet-id");
        when(deviceTypeRepository.findAllByOrderByNameAsc()).thenReturn(List.of(notebook, tablet));

        var result = new DeviceTypeService(deviceTypeRepository).getAll();

        assertEquals(List.of("notebook-id", "tablet-id"), result.stream().map(item -> item.getId()).toList());
        assertEquals(List.of("Notebook", "Tablet"), result.stream().map(item -> item.getName()).toList());
    }
}
