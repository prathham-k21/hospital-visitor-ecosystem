const API_BASE_URL = 'http://localhost:8082';
let html5QrCode;
window.onload = loadActive;

function toggleTheme() {
    document.body.classList.toggle('light-mode');
    const isLight = document.body.classList.contains('light-mode');
    document.getElementById('theme-icon').innerText = isLight ? '☀️' : '🌙';
    document.getElementById('theme-text').innerText = isLight ? 'Day Mode' : 'Night Mode';
}

async function checkIn() {
    const btn = document.querySelector('.btn-checkin');
    btn.disabled = true;
    btn.innerText = "Verifying & Sending Pass...";

    const data = {
        name: document.getElementById('name').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value,
        purpose: document.getElementById('purpose').value,
        patientId: document.getElementById('patientId').value.trim() // Sent to HealthBridge via backend
    };

    try {
        const res = await fetch(`${API_BASE_URL}/api/visitors/check-in`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (res.ok) {
            const savedVisitor = await res.json(); // Get the returned object to show the room number
            await loadActive();
            clearInputs();
            alert(`✅ Success! A Secure QR Pass has been sent to ${data.email}.\n\nProceed to Room: ${savedVisitor.roomNumber}\nVisiting: ${savedVisitor.hostName}`);
        } else {
            // Read the custom error message returned by our ResponseEntity
            const errorMsg = await res.text();
            alert(errorMsg);
        }
    } catch (error) {
        console.error("Connection error:", error);
        alert("Connection error. Ensure the SecureStay server is running.");
    } finally {
        btn.disabled = false;
        btn.innerText = "Check In";
    }
}

async function loadActive() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/visitors/active`);
        if (!res.ok) return;

        const visitors = await res.json();
        const tableBody = document.getElementById('visitorTable');

        // Note: The backend still returns hostName and hostEmail perfectly because it mapped them from HealthBridge!
        tableBody.innerHTML = visitors.map(v => `
            <tr>
                <td><b>${v.name}</b><br><small style="color:var(--text-muted)">${v.phone}</small></td>
                <td>${v.hostName} (Room: ${v.roomNumber})<br><small style="color:var(--text-muted)">${v.hostEmail}</small></td>
                <td><button class="btn-out" onclick="checkOut(${v.id})">Manual Out</button></td>
            </tr>
        `).join('');
    } catch (error) {
        console.error("Error loading active visitors:", error);
    }
}

async function checkOut(id) {
    try {
        const res = await fetch(`${API_BASE_URL}/api/visitors/check-out/${id}`, { method: 'PUT' });
        if (res.ok) {
            loadActive();
            return true;
        }
        return false;
    } catch (error) {
        console.error("Error during checkout:", error);
        return false;
    }
}

function toggleScanner() {
    const reader = document.getElementById('reader');
    const btn = document.getElementById('scan-btn');

    if (reader.style.display === 'none' || reader.style.display === '') {
        reader.style.display = 'block';
        btn.innerText = "🛑 Stop Scanner";
        btn.classList.add('active');
        startScanner();
    } else {
        stopScanner();
    }
}

function startScanner() {
    html5QrCode = new Html5Qrcode("reader");
    html5QrCode.start(
        { facingMode: "user" },
        { fps: 10, qrbox: 250 },
        async (decodedText) => {
            const success = await checkOut(decodedText);
            if (success) {
                alert("Checkout Successful! Thank you for visiting.");
                stopScanner();
            }
        },
        () => {}
    ).catch(err => {
        console.error("Camera access denied", err);
        stopScanner();
    });
}

function stopScanner() {
    const reader = document.getElementById('reader');
    const btn = document.getElementById('scan-btn');
    if (html5QrCode) {
        html5QrCode.stop().then(() => {
            reader.style.display = 'none';
            btn.innerText = "📷 Scan Pass to Checkout";
            btn.classList.remove('active');
        }).catch(err => console.error("Error stopping scanner", err));
    }
}

function clearInputs() {
    document.querySelectorAll('input').forEach(i => i.value = '');
}