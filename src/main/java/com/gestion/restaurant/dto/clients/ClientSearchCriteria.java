package com.gestion.restaurant.dto.clients;

import lombok.Data;

@Data
public class ClientSearchCriteria {
    private String nom;
    private String prenom;
    private Long idTypeClient;
    private String contact;
}
