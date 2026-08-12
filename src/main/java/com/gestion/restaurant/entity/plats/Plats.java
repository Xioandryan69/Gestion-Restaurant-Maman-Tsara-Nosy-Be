package com.gestion.restaurant.entity.plats;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "plats")
@Data
@NoArgsConstructor
public class Plats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nom;

    @ManyToOne
    @JoinColumn(name = "idcategorieplats", nullable = false)
    private CategoriePlats categoriePlats;

    @Column(name = "prixvente", nullable = false, precision = 16, scale = 3)
    private BigDecimal prixVente;
}
