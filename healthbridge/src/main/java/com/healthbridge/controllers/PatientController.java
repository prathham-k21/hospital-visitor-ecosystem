package com.healthbridge.controllers;

import com.healthbridge.entities.Patient;
import com.healthbridge.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // POST http://localhost:8081/api/patients
    @PostMapping
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody Patient patient) {
        Patient savedPatient = patientService.addPatient(patient);
        return new ResponseEntity<>(savedPatient, HttpStatus.CREATED);
    }

    // GET http://localhost:8081/api/patients
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    // GET http://localhost:8081/api/patients/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(patientService.getPatientById(id));
        } catch (RuntimeException e) {
            // Returns a clean 404 instead of a 500 Internal Server Error crash
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // --- NEW ENDPOINT FOR SECURESTAY INTEGRATION ---
    // GET http://localhost:8081/api/patients/room/{roomNumber}
    @GetMapping("/room/{roomNumber}")
    public ResponseEntity<?> getPatientByRoomNumber(@PathVariable String roomNumber) {
        try {
            Patient patient = patientService.getPatientByRoomNumber(roomNumber);
            return ResponseEntity.ok(patient);
        } catch (RuntimeException e) {
            // If the room is empty or doesn't exist, tell SecureStay gracefully
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    // -----------------------------------------------

    // PUT http://localhost:8081/api/patients/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @Valid @RequestBody Patient patientDetails) {
        return ResponseEntity.ok(patientService.updatePatient(id, patientDetails));
    }

    // DELETE http://localhost:8081/api/patients/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok("Patient deleted successfully!");
    }
}