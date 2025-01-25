package com.taller.resource.mapper;

import com.taller.model.Client;
import com.taller.resource.dto.ClientDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    List<ClientDTO> clientToClientDTO(List<Client> client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDateTime", ignore = true)
    @Mapping(target = "modificationDatetime", ignore = true)
    @Mapping(target = "repairs", ignore = true)
    @Mapping(target = "devices", ignore = true)
    Client clientDTOToClient(ClientDTO clientDTO);
}
