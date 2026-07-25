const API_URL = 'http://localhost:8081/api/patients';

// Load patients as soon as the page loads
document.addEventListener('DOMContentLoaded', fetchPatients);

// Handle form submission
document.getElementById('patientForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent page reload

    const newPatient = {
        fullName: document.getElementById('fullName').value,
        email: document.getElementById('email').value,
        age: parseInt(document.getElementById('age').value),
        contactNumber: document.getElementById('contactNumber').value,
        diseaseOrSymptoms: document.getElementById('disease').value,
        roomNumber: document.getElementById('roomNumber').value,
        admitted: true // Default to true when adding through this form
    };

    fetch(API_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(newPatient)
    })
    .then(async response => {
        if (response.status === 201) {
            alert('Patient admitted successfully!');
            document.getElementById('patientForm').reset(); // Clear the form
            fetchPatients(); // Refresh the table
        } else {
            // This catches the RuntimeException thrown by the backend for duplicates
            alert('Error: Patient could not be admitted. They may already exist in the system or the data is invalid.');
        }
    })
    .catch(error => console.error('Error adding patient:', error));
});

// Fetch and display patients
function fetchPatients() {
    fetch(API_URL)
        .then(response => response.json())
        .then(patients => {
            const tableBody = document.getElementById('patientTableBody');
            tableBody.innerHTML = ''; // Clear existing rows

            patients.forEach(patient => {
                const row = document.createElement('tr');

                // Highlight admitted status for easy visibility
                const statusText = patient.admitted ? "🟢 Admitted" : "🔴 Discharged";

                row.innerHTML = `
                    <td>${patient.id}</td>
                    <td><strong>${patient.fullName}</strong></td>
                    <td>${patient.email}</td>
                    <td>${patient.roomNumber}</td>
                    <td>${statusText}</td>
                    <td>
                        <button onclick="deletePatient(${patient.id})" style="color: red;">Discharge / Delete</button>
                    </td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error('Error fetching patients:', error));
}

// Delete a patient
function deletePatient(id) {
    if (confirm('Are you sure you want to remove this patient from the system?')) {
        fetch(`${API_URL}/${id}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                fetchPatients(); // Refresh the table
            }
        })
        .catch(error => console.error('Error deleting patient:', error));
    }
}

// Export data to CSV
document.getElementById('exportBtn').addEventListener('click', function() {
    fetch(API_URL)
        .then(response => response.json())
        .then(patients => {
            if (patients.length === 0) {
                alert("No patients to export.");
                return;
            }

            // 1. Define the CSV headers
            const headers = ['ID', 'Full Name', 'Email', 'Age', 'Contact Number', 'Disease/Symptoms', 'Room Number', 'Admitted'];
            let csvContent = headers.join(',') + '\n';

            // 2. Loop through the data and format it
            patients.forEach(p => {
                // Wrap text fields in quotes to prevent commas in text from breaking the CSV
                const row = [
                    p.id,
                    `"${p.fullName}"`,
                    `"${p.email}"`,
                    p.age,
                    `"${p.contactNumber}"`,
                    `"${p.diseaseOrSymptoms}"`,
                    `"${p.roomNumber}"`,
                    p.admitted ? 'Yes' : 'No'
                ];
                csvContent += row.join(',') + '\n';
            });

            // 3. Create a Blob (a file-like object of immutable, raw data)
            const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });

            // 4. Create a hidden download link and click it programmatically
            const url = URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.setAttribute("href", url);
            link.setAttribute("download", "healthbridge_patients.csv");
            link.style.display = 'none';
            document.body.appendChild(link);

            link.click(); // Trigger the download

            // 5. Clean up
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
        })
        .catch(error => console.error('Error exporting CSV:', error));
});