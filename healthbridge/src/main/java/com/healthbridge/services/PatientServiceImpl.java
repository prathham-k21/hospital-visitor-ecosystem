package com.healthbridge.services;

import com.healthbridge.entities.Patient;
import com.healthbridge.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final EmailService emailService; // 1. Inject the new EmailService

    @Override
    public Patient addPatient(Patient patient) {

        // 1. Check for duplicates!
        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new RuntimeException("A patient with the email " + patient.getEmail() + " is already admitted.");
        }

        // 2. If it's a new email, save the patient to the database
        Patient savedPatient = patientRepository.save(patient);

        // 3. Trigger the email in the background
        try {
            emailService.sendAdmissionEmail(
                    savedPatient.getEmail(),
                    savedPatient.getFullName(),
                    savedPatient.getRoomNumber()
            );
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return savedPatient;
    }

    // ... keeping your existing get, update, and delete methods exactly as they are ...

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
    }

    @Override
    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient existingPatient = getPatientById(id);
        existingPatient.setFullName(patientDetails.getFullName());
        existingPatient.setEmail(patientDetails.getEmail());
        existingPatient.setAge(patientDetails.getAge());
        existingPatient.setContactNumber(patientDetails.getContactNumber());
        existingPatient.setDiseaseOrSymptoms(patientDetails.getDiseaseOrSymptoms());
        existingPatient.setRoomNumber(patientDetails.getRoomNumber());
        existingPatient.setAdmitted(patientDetails.getAdmitted());
        return patientRepository.save(existingPatient);
    }

    // The @Override will now work because the interface has the method
    @Override
    public Patient getPatientByRoomNumber(String roomNumber) {
        // The .orElseThrow will now work because the repository returns an Optional
        return patientRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RuntimeException("No patient found in room: " + roomNumber));
    }

    @Override
    public void deletePatient(Long id) {
        // Fetch the patient first
        Patient patient = getPatientById(id);

        // Trigger the discharge email
        try {
            emailService.sendDischargeEmail(patient.getEmail(), patient.getFullName());
        } catch (Exception e) {
            System.err.println("Failed to send discharge email: " + e.getMessage());
        }

        // Finally, remove them from the database
        patientRepository.delete(patient);
    }

}