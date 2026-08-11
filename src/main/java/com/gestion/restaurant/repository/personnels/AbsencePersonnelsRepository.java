package com.gestion.restaurant.repository.personnels;

import com.gestion.restaurant.entity.personnels.AbsencePersonnels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AbsencePersonnelsRepository extends JpaRepository<AbsencePersonnels, Long> {
    List<AbsencePersonnels> findByPersonnel_IdOrderByDateDebutDesc(Long idPersonnel);
    List<AbsencePersonnels> findByPersonnelId(Long idPersonnel);
}