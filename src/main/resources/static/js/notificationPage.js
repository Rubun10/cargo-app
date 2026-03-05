// =============================
// Notification JS
// =============================

function initNotificationPage() { 
	
    const container = document.getElementById("notificationsContainer");
    if (!container) return;
    const token = localStorage.getItem("authToken");
    const authHeaders = token ? { "Authorization": `Bearer ${token}` } : {};

    // =============================
    // Fetch Notifications
    // =============================
    async function fetchNotifications() {
        try {
            const response = await fetch("/api/notifications", {
                method: "GET",
                credentials: "include",
                headers: authHeaders
            });

	
            if (response.status === 401) {
                window.location.href = "/login";
                return;
            }

            if (!response.ok) throw new Error("Failed to fetch notifications");

            const notifications = await response.json();
            renderNotifications(notifications);

        } catch (error) {
        }
    }

    function renderNotifications(notifications) {

        container.innerHTML = "";

        if (!notifications || notifications.length === 0) {
            container.innerHTML =
                `<div class="no-notification">No notifications</div>`;
            return;
        }

        notifications.forEach(n => {
            const senderName = n.senderName ?? n.sender_name ?? n.sendername ?? "";
            const senderId = n.senderId ?? n.senderID ?? n.sender_id ?? null;
            const entityType = (n.entityType ?? n.entity_type ?? "").toString().toUpperCase();
            const entityId = n.entityId ?? n.entityID ?? n.entity_id ?? null;
            const type = (n.type ?? "").toString().toUpperCase();

            const div = document.createElement("div");
			
            div.className = "notification";
            
            if (!n.read) div.classList.add("unread");
            const senderLine = `<div class="sender-info">From: ${senderName || "Unknown"} (ID: ${senderId ?? "-"})</div>`;
            const actionButton = canApprove(type, entityType, entityId, n.message, n.read)
                ? `
                    <button class="approve-btn" data-load-id="${toLoadId(entityId)}">Approve Cargo</button>
                    <button class="cancel-approve-btn" data-load-id="${toLoadId(entityId)}">Cancel</button>
                  `
                : "";

            div.innerHTML = `
                <strong>${n.type}</strong><br>
                ${n.message}
                ${senderLine}
                <div class="notification-footer">
                    <small>${new Date(n.createdAt).toLocaleString()}</small>
                    ${actionButton}
                </div>
            `;

            div.addEventListener("click", (event) => {
                if (
                    event.target.classList.contains("approve-btn") ||
                    event.target.classList.contains("cancel-approve-btn")
                ) return;
                markAsRead(n.id, div);
            });

            const approveBtn = div.querySelector(".approve-btn");
            if (approveBtn) {
                approveBtn.addEventListener("click", async (event) => {
                    event.stopPropagation();
                    approveBtn.disabled = true;
                    await approveCargo(approveBtn.dataset.loadId, n.id, div);
                });
            }
            
            const cancelBtn = div.querySelector(".cancel-approve-btn");
            if (cancelBtn) {
                cancelBtn.addEventListener("click", async (event) => {
                    event.stopPropagation();
                    cancelBtn.disabled = true;
                    if (approveBtn) approveBtn.disabled = true;
                    await cancelCargoApproval(cancelBtn.dataset.loadId, n.id, div);
                });
            }

            container.appendChild(div);
        });
		
		
    }

    // =============================
    // Mark As Read
    // =============================
    async function markAsRead(notificationId, element) {
        try {
            const response = await fetch(
                `/api/notifications/${notificationId}/read`,
                {
                    method: "POST",
                    credentials: "include",
                    headers: authHeaders
                }
            );

            if (!response.ok) throw new Error("Failed to mark read");

            element.classList.remove("unread");

        } catch (error) {
        }
    }

    function canApprove(type, entityType, entityId, message, isRead) {
        const isDriverRequest =
            type === "DRIVER_REQUEST" ||
            type === "DRIVER REQUEST" ||
            (typeof message === "string" && message.toLowerCase().includes("driver requested"));
        const isLoadEntity = !entityType || entityType === "LOAD";

        return !isRead && isDriverRequest && isLoadEntity && !!entityId;
    }

    function toLoadId(entityId) {
        if (!entityId) return "";
        return `LD-${entityId}`;
    }

    async function approveCargo(loadId, notificationId, element) {
        if (!loadId) {
            alert("Missing load reference in notification.");
            return;
        }

        try {
            const response = await fetch(`/api/cargo-available/${loadId}/approve`, {
                method: "POST",
                credentials: "include",
                headers: authHeaders
            });

            if (response.status === 401) {
                window.location.href = "/login";
                return;
            }

            if (!response.ok) {
                let message = "Failed to approve cargo";
                try {
                    const payload = await response.json();
                    if (payload && payload.message) message = payload.message;
                } catch (_) {
                    const text = await response.text();
                    if (text) message = text;
                }
                throw new Error(message);
            }

            await markAsRead(notificationId, element);
            element.remove();
            await fetchNotifications();
            alert("Cargo approved successfully. Driver notified.");
        } catch (error) {
            alert(error.message || "Unable to approve cargo.");
        }
    }
    
    async function cancelCargoApproval(loadId, notificationId, element) {
        if (!loadId) {
            alert("Missing load reference in notification.");
            return;
        }

        try {
            const response = await fetch(`/api/cargo-available/${loadId}/cancel`, {
                method: "POST",
                credentials: "include",
                headers: authHeaders
            });

            if (response.status === 401) {
                window.location.href = "/login";
                return;
            }

            if (!response.ok) {
                let message = "Failed to cancel cargo approval";
                try {
                    const payload = await response.json();
                    if (payload && payload.message) message = payload.message;
                } catch (_) {
                    const text = await response.text();
                    if (text) message = text;
                }
                throw new Error(message);
            }

            await markAsRead(notificationId, element);
            element.remove();
            await fetchNotifications();
            alert("Cargo approval cancelled. Driver notified and status set to available.");
        } catch (error) {
            alert(error.message || "Unable to cancel cargo approval.");
        }
    }

    // Optional auto refresh
    fetchNotifications();
    setInterval(fetchNotifications, 10000);
}
