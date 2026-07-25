package com.securestay.dto;

import lombok.Data;

@Data
public class PatientResponseDTO {
    private String id;
    private String fullName;
    private String email;
    private String roomNumber;
    private Boolean admitted;
}