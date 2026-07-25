package com.securestay.services;

import com.securestay.dto.PatientResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class HealthBridgeClientService {

    private final RestTemplate restTemplate;

    private final String HEALTHBRIDGE_API_URL = "http://localhost:8081/api/patients/room/";

    // This reads the toggle from application.properties.
    // If you forget to add it to the file, it defaults to true.
    @Value("${app.integration.healthbridge.enabled:true}")
    private boolean isIntegrationEnabled;

    public PatientResponseDTO verifyPatient(String patientId) {

        // --- STANDALONE MODE ---
        // --- STANDALONE MODE ---
        if (!isIntegrationEnabled) {
            System.out.println("⚠️ HealthBridge integration is DISABLED. Running in Standalone Mode.");

            PatientResponseDTO dummyPatient = new PatientResponseDTO();
            dummyPatient.setId(patientId); // No more type errors here
            dummyPatient.setFullName("Demo Patient");
            dummyPatient.setRoomNumber("Demo-101");
            dummyPatient.setEmail("demo@healthbridge.com");
            dummyPatient.setAdmitted(true);

            return dummyPatient;
        }

        // --- MICROSERVICE MODE ---
        try {
            String url = HEALTHBRIDGE_API_URL + patientId;
            return restTemplate.getForObject(url, PatientResponseDTO.class);

        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Verification Failed: No patient found with ID " + patientId);
        } catch (Exception e) {
            throw new RuntimeException("Communication Error: Could not connect to HealthBridge.");
        }
    }
}