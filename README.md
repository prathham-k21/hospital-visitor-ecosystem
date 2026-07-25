====================================================================
HOSPITAL VISITOR ECOSYSTEM (MICROSERVICE ARCHITECTURE)
====================================================================

Overview
--------
The Hospital Visitor Ecosystem is a robust, dual-service backend architecture designed to modernize hospital entry protocols. It replaces traditional paper logbooks with a secure, touchless QR-code check-in system that verifies visitor data against a live patient database in real-time.

This project demonstrates practical microservice communication, fault-tolerant design, and independent API development using Java and Spring Boot.

System Architecture
-------------------
The ecosystem is split into two distinct, independently deployable Spring Boot applications that communicate via REST APIs:

1. HealthBridge (Port: 8081)
   The Core Patient Management API
    * Role: Acts as the source of truth for hospital admissions.
    * Features: Full CRUD operations for patient records, email uniqueness validation, and a custom findByRoomNumber endpoint designed specifically to resolve data-type mismatches with external services.
    * Database: MySQL (healthbridge_db)

2. SecureStay (Port: 8082)
   The Visitor Management & QR Generation Gateway
    * Role: Handles the frontend UI, processes visitor check-ins, and manages checkout scanning.
    * Features: Takes visitor details and a target Room Number, reaches out to HealthBridge over the network to verify the patient, generates a secure QR pass, and logs the visit.
    * Database: MySQL (securestay_db)

Key Engineering Highlights
--------------------------
* Microservice Communication: Utilizes Spring's RestTemplate to facilitate cross-service HTTP requests (SecureStay fetching data from HealthBridge).
* Fault Tolerance & Standalone Mode: Implemented a Feature Toggle (app.integration.healthbridge.enabled=false). If HealthBridge goes offline, SecureStay can seamlessly fall back to an isolated standalone mode using mock data, ensuring the front desk never experiences downtime.
* Graceful Exception Handling: Global exception handling prevents 500 Internal Server Errors, returning clean 404s for missing records and 400s for duplicate emails to keep the frontend UI stable.

Tech Stack
----------
* Backend: Java 21, Spring Boot 4.x, REST APIs
* Data Access: Spring Data JPA, Hibernate ORM
* Database: MySQL
* Frontend: HTML5, CSS3, Vanilla JavaScript
* Utilities: Lombok, Jakarta Validation, HTML5-QRCode Scanner

Setup & Installation
--------------------
Prerequisites:
* Java 21+ installed
* MySQL Server running locally
* Maven installed

1. Database Setup
   Create the following schemas in your local MySQL instance:
   CREATE DATABASE healthbridge_db;
   CREATE DATABASE securestay_db;

2. Configure Environment Variables
   To keep credentials secure, this repository uses example configuration files.
    * Navigate to healthbridge/src/main/resources/ and securestay/src/main/resources/.
    * Rename the provided application.properties.example files to application.properties.
    * Update the spring.datasource.password in both files with your local MySQL password.

3. Run the Microservices
    * Start HealthBridge First: Navigate to the healthbridge directory and run the Spring Boot application. It will boot on http://localhost:8081.
    * Start SecureStay Second: Navigate to the securestay directory and run the Spring Boot application. It will boot on http://localhost:8082.

4. Access the UI
   Open your browser and navigate to http://localhost:8082/index.html to interact with the fully integrated visitor check-in system.

Author
------
Pratham Kadam