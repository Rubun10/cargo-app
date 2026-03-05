// ============================
// INIT (CALLED AFTER LOAD)
// ============================
function initProfilePage() {

    attachEditButtons();
    attachSaveButtons();
    attachCancelButtons();
    attachKeyboardHandlers();
    attachNotificationToggle();

    fetchUserProfile();
}

// ============================
// FETCH PROFILE DATA
// ============================
function fetchUserProfile() {
    const token = localStorage.getItem("authToken");
    const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};

    fetch("/api/user/profile", {
        credentials: "include",
        headers: authHeaders
    })
        .then(res => {
            if (!res.ok) {
                if (res.status === 401) {
                    window.location.href = "/login";
                }
                throw new Error("Failed to fetch profile");
            }
            return res.json();
        })
		.then(data => {

		    document.querySelector(".userName").innerText = data.name;
		    document.querySelector(".userIdText").innerText = "User ID: " + data.id;

		    document.getElementById("mobile-value").innerText = data.mobile || "";
		    document.getElementById("email-value").innerText = data.email || "";
		    document.getElementById("location-value").innerText = data.location || "";

		    // 🔥 ADD THIS
		    const img = document.getElementById("profile-userPhoto");
		    const icon = document.getElementById("profile-userIcon");

		    if (data.profilePhoto) {
		        img.src = data.profilePhoto + "?t=" + Date.now();

		        img.style.display = "block";
		        icon.style.display = "none";
		    } else {
		        img.style.display = "none";
		        icon.style.display = "block";
		    }

		})
}


// ============================
// EDIT BUTTONS
// ============================
function attachEditButtons() {
    document.querySelectorAll(".edit-btn").forEach(button => {
        button.onclick = function (e) {
            e.stopPropagation();
            openEditBox(this.dataset.field);
        };
    });
}

function openEditBox(field) {

    document.querySelectorAll(".edit-box").forEach(box => {
        box.classList.remove("active");
    });

    const box = document.getElementById(`${field}-edit-box`);
    if (!box) return;

    box.classList.add("active");

    const input = document.getElementById(`${field}-input`);
    if (input) setTimeout(() => input.focus(), 100);
}


// ============================
// CANCEL / CLOSE
// ============================
function attachCancelButtons() {
    document.querySelectorAll(".cancel-btn, .close-edit").forEach(btn => {
        btn.onclick = function (e) {
            e.stopPropagation();
            closeEditBox(this.dataset.field);
        };
    });
}

function closeEditBox(field = null) {
    if (field) {
        const box = document.getElementById(`${field}-edit-box`);
        if (box) box.classList.remove("active");
    } else {
        document.querySelectorAll(".edit-box").forEach(box => {
            box.classList.remove("active");
        });
    }
}


// ============================
// SAVE BUTTONS
// ============================
function attachSaveButtons() {
    document.querySelectorAll(".save-btn").forEach(button => {
        button.onclick = function (e) {
            e.stopPropagation();
            handleSave(this.dataset.field);
        };
    });
}

function handleSave(field) {

    let value;

    if (field === "password") {

        const password = document.getElementById("password-input").value.trim();
        const confirm = document.getElementById("password-confirm").value.trim();

        if (!password || !confirm) {
            alert("Please fill both password fields");
            return;
        }

        if (password !== confirm) {
            alert("Passwords do not match");
            return;
        }

        value = password;

    } else {

        const input = document.getElementById(`${field}-input`);

        if (!input || !input.value.trim()) {
            alert("Value cannot be empty");
            return;
        }

        value = input.value.trim();
    }

    saveProfile(field, value);
}


// ============================
// SAVE TO BACKEND
// ============================
function saveProfile(field, value) {
    const token = localStorage.getItem("authToken");
    const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};

    fetch("/api/user/update", {
        method: "PUT",
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
            ...authHeaders
        },
        body: JSON.stringify({
            field,
            value
        })
    })
        .then(async res => {
            if (!res.ok) {
                const raw = await res.text();
                const msg = normalizeErrorMessage(raw, field);
                throw new Error(msg);
            }
        })
        .then(() => {

            const valueEl = document.getElementById(`${field}-value`);

            if (field === "password") {
                valueEl.textContent = "--------";
            } else {
                valueEl.textContent = value;
            }

            closeEditBox(field);
            showSuccessMessage(field, `${field} updated successfully`);
        })
        .catch(err => alert(err.message));
}

// ============================
// ERROR MESSAGE NORMALIZER
// ============================
function normalizeErrorMessage(rawMessage, field) {
    const msg = (rawMessage || "").toString();
    const lower = msg.toLowerCase();

    const isDuplicate =
        lower.includes("duplicate") ||
        lower.includes("duplicate entry") ||
        lower.includes("already exists") ||
        lower.includes("e11000") ||
        lower.includes("unique constraint");

    if (isDuplicate) {
        if (field === "mobile" || lower.includes("mobile")) {
            return "This mobile number already exists. Please use a different number.";
        }
        if (field === "email" || lower.includes("email")) {
            return "This email already exists. Please use a different email.";
        }
        return "This value already exists. Please use a different one.";
    }

    return msg || "Unable to update profile. Please try again.";
}


// ============================
// SUCCESS MESSAGE
// ============================
function showSuccessMessage(field, message) {

    const item = document.getElementById(`${field}-item`);
    if (!item) return;

    const existing = item.querySelector(".success-label");
    if (existing) existing.remove();

    const label = document.createElement("div");
    label.className = "success-label";
    label.innerHTML = `<i class="fas fa-check-circle"></i> ${message}`;

    item.appendChild(label);

    setTimeout(() => {
        if (label.parentNode) label.remove();
    }, 3000);
}


// ============================
// NOTIFICATION TOGGLE
// ============================
function attachNotificationToggle() {

    const toggle = document.getElementById("notification-toggle");
    if (!toggle) return;
    
    const settingKey = "liveTrackingNotificationEnabled";
    const saved = localStorage.getItem(settingKey);
    toggle.checked = saved === "true";

    toggle.onchange = function () {
        localStorage.setItem(settingKey, String(this.checked));
        window.dispatchEvent(new CustomEvent("liveTrackingNotificationPreferenceChanged"));
        const status = this.checked ? "enabled" : "disabled";
        showSuccessMessage("notification", `Live tracking notifications ${status}`);
        alert(
            this.checked
                ? "Live tracking notification is ON. Nearby cargo available alerts will be shown."
                : "Live tracking notification is OFF. Nearby cargo available alerts will not be shown."
        );
    };
}


// ============================
// KEYBOARD HANDLERS
// ============================
function attachKeyboardHandlers() {

    document.addEventListener("keydown", e => {
        if (e.key === "Escape") closeEditBox();
    });

    document.querySelectorAll(".edit-input").forEach(input => {

        input.addEventListener("keypress", e => {

            if (e.key === "Enter") {
                e.preventDefault();

                const field = input.id
                    .replace("-input", "")
                    .replace("-confirm", "");

                const btn = document.querySelector(`.save-btn[data-field="${field}"]`);

                if (btn) btn.click();
            }
        });
    });
}
