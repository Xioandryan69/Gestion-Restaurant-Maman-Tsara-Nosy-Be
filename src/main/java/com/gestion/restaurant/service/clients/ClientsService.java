package com.gestion.restaurant.service.clients;

import com.gestion.restaurant.dto.clients.*;
import com.gestion.restaurant.entity.clients.Clients;
import com.gestion.restaurant.entity.clients.TypeClient;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.clients.ClientsRepository;
import com.gestion.restaurant.repository.clients.TypeClientRepository;
import com.gestion.restaurant.specification.clients.ClientsSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientsService {

    private final ClientsRepository clientsRepository;
    private final TypeClientRepository typeClientRepository;

    public ClientsService(ClientsRepository clientsRepository, TypeClientRepository typeClientRepository) {
        this.clientsRepository = clientsRepository;
        this.typeClientRepository = typeClientRepository;
    }

    @Transactional(readOnly = true)
    public Page<ClientResponseDto> search(ClientSearchCriteria criteria, Pageable pageable) {
        Specification<Clients> spec = ClientsSpecification.withFilters(criteria);
        return clientsRepository.findAll(spec, pageable).map(ClientMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Clients findById(Long id) {
        return clientsRepository.findByIdWithType(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'ID : " + id));
    }

    @Transactional(readOnly = true)
    public ClientRequestDto findDtoById(Long id) {
        Clients c = findById(id);
        ClientRequestDto dto = new ClientRequestDto();
        dto.setId(c.getId());
        dto.setNom(c.getNom());
        dto.setPrenom(c.getPrenom());
        dto.setContact(c.getContact());
        if (c.getTypeClient() != null) dto.setIdTypeClient(c.getTypeClient().getId());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<TypeClient> findAllTypes() {
        return typeClientRepository.findAll();
    }

    @Transactional
    public ClientResponseDto saveFromDto(ClientRequestDto dto) {
        if (dto.getNom() == null || dto.getNom().isBlank()) {
            throw new BusinessRuleException("Le nom du client est obligatoire.");
        }
        if (dto.getIdTypeClient() == null) {
            throw new BusinessRuleException("Le type de client est obligatoire.");
        }

        Clients client = (dto.getId() != null) ? findById(dto.getId()) : new Clients();
        TypeClient typeClient = typeClientRepository.findById(dto.getIdTypeClient())
                .orElseThrow(() -> new ResourceNotFoundException("Type client introuvable : " + dto.getIdTypeClient()));

        client.setNom(dto.getNom());
        client.setPrenom(dto.getPrenom());
        client.setContact(dto.getContact());
        client.setTypeClient(typeClient);

        return ClientMapper.toDto(clientsRepository.save(client));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!clientsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client introuvable avec l'ID : " + id);
        }
        clientsRepository.deleteById(id);
    }
}