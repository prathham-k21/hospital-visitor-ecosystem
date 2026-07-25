package com.healthbridge.repositories;

import com.healthbridge.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    // Spring Boot automatically translates this into:
    // SELECT count(*) FROM patients WHERE email = ?
    boolean existsByEmail(String email);
    Optional<Patient> findByRoomNumber(String roomNumber);

}