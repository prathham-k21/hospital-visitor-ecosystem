package com.healthbridge.services;

import com.healthbridge.entities.Patient;
import java.util.List;

public interface PatientService {
    Patient addPatient(Patient patient);
    List<Patient> getAllPatients();
    Patient getPatientById(Long id);
    Patient updatePatient(Long id, Patient patientDetails);
    void deletePatient(Long id);

    // --- Add this missing line! ---
    Patient getPatientByRoomNumber(String roomNumber);
}