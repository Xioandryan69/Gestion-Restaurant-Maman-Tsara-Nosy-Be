package com.gestion.restaurant.service.clients;

import com.gestion.restaurant.dto.clients.ClientRequestDto;
import com.gestion.restaurant.entity.clients.Clients;
import com.gestion.restaurant.entity.clients.TypeClient;
import com.gestion.restaurant.exception.BusinessRuleException;
import com.gestion.restaurant.exception.ResourceNotFoundException;
import com.gestion.restaurant.repository.clients.ClientsRepository;
import com.gestion.restaurant.repository.clients.TypeClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientsServiceTest {

    @Mock ClientsRepository clientsRepository;
    @Mock TypeClientRepository typeClientRepository;
    @InjectMocks ClientsService service;

    @Test
    void saveFromDto_nomObligatoire() {
        assertThatThrownBy(() -> service.saveFromDto(new ClientRequestDto()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("nom");
    }

    @Test
    void saveFromDto_ok() {
        TypeClient type = new TypeClient();
        type.setId(1L);
        when(typeClientRepository.findById(1L)).thenReturn(Optional.of(type));
        when(clientsRepository.save(any())).thenAnswer(inv -> {
            Clients c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        ClientRequestDto dto = new ClientRequestDto();
        dto.setNom("Rabe");
        dto.setPrenom("Soa");
        dto.setContact("032");
        dto.setIdTypeClient(1L);

        assertThat(service.saveFromDto(dto).getNom()).isEqualTo("Rabe");
    }

    @Test
    void findById_introuvable() {
        when(clientsRepository.findByIdWithType(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteById_introuvable() {
        when(clientsRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> service.deleteById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
