/* Available Cargo JS */
(function () {
    if (window.initCargoAvailablePage) {
        return;
    }

    window.initCargoAvailablePage = function () {
        if (window.__cargoAvailableInitDone) return;
        window.__cargoAvailableInitDone = true;


        // DOM
        const cargoContainer = document.getElementById("cargoContainer");
        const searchInput = document.getElementById("searchInput");
        const detailsModal = document.getElementById("detailsModal");

        const dLoadId = document.getElementById("dLoadId");
        const dType = document.getElementById("dType");
        const dWeight = document.getElementById("dWeight");
        const dPrice = document.getElementById("dPrice");
        const dDate = document.getElementById("dDate");
        const dRoute = document.getElementById("dRoute");
        const dAdd = document.getElementById("dAdd");

        const confirmBtn = document.getElementById("confirmBtn");

        // API
        const API = "/api/cargo-available";
        const NEARBY_CARGO_HIGHLIGHT_KEY = "nearbyCargoHighlightIds";
        const token = localStorage.getItem("authToken");
        const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};

        let allLoads = [];
        let selectedLoadId = null;
        let currentUserRole = null;

        function formatWeight(value, unit) {
            const numeric = Number(value);
            const safeValue = Number.isFinite(numeric) ? numeric : 0;
            const safeUnit = String(unit || "kg").trim().toLowerCase();
            const label = safeUnit === "ton" ? "Ton" : "Kg";
            return `${safeValue} ${label}`;
        }

        function formatAdditionalDetails(raw) {
            const text = (raw || "").toString().trim();
            if (!text) return "-";
            // Guard against accidental tuple dump strings.
            const tuplePattern = /,\s*'LD-\d+'|,\s*NULL\b|,\s*'\d{4}-\d{2}-\d{2}/i;
            if (!tuplePattern.test(text)) return text;
            const firstPart = text.split(",")[0].trim();
            return firstPart || text;
        }

        async function loadCurrentUserRole() {
            try {
                const res = await fetch("/api/user/me", { credentials: "include" });
                if (!res.ok) return;
                const user = await res.json();
                currentUserRole = user && user.role ? String(user.role).toUpperCase() : null;
            } catch (err) {
            }
        }

        async function ensureDriverKycCompleted() {
            try {
                const res = await fetch("/api/kyc/status", { credentials: "include" });
                if (res.status === 401) {
                    window.location.href = "/login";
                    return false;
                }
                if (!res.ok) return false;

                const payload = await res.json();
                if (payload && payload.kycCompleted === true) {
                    return true;
                }

                alert("Please complete KYC before confirming cargo.");
                window.location.href = "/driver-verification?kycRequired=cargo-confirm";
                return false;
            } catch (err) {
                alert("Unable to verify KYC status.");
                return false;
            }
        }

        function highlightNearbyLoads() {
            const stored = sessionStorage.getItem(NEARBY_CARGO_HIGHLIGHT_KEY);
            if (!stored) return;

            sessionStorage.removeItem(NEARBY_CARGO_HIGHLIGHT_KEY);

            let loadIds = [];
            try {
                const parsed = JSON.parse(stored);
                if (Array.isArray(parsed)) {
                    loadIds = parsed.map(id => String(id));
                }
            } catch (err) {
                return;
            }

            if (loadIds.length === 0) return;

            const cardsToHighlight = loadIds
                .map(id => cargoContainer.querySelector(`.cargo-card[data-load-id="${id}"]`))
                .filter(Boolean);

            if (cardsToHighlight.length === 0) return;

            cardsToHighlight.forEach(card => card.classList.add("nearby-highlight"));
            cardsToHighlight[0].scrollIntoView({ behavior: "smooth", block: "center" });

            setTimeout(() => {
                cardsToHighlight.forEach(card => card.classList.remove("nearby-highlight"));
            }, 5000);
        }

        function getStatusClass(status) {
            status = status.toUpperCase();

            if (status === "AVAILABLE") return "available";
            if (status === "ACTIVE") return "active";
            if (status === "PENDING") return "pending";

            return "";
        }

        // Load Available Cargos
        function loadCargos() {
            fetch(API, { credentials: "include", headers: authHeaders })
                .then(res => {
                    if (res.status === 401) {
                        alert("Session expired. Please login again.");
                        window.location.href = "/login";
                        return [];
                    }

                    if (res.status === 204) {
                        return [];
                    }

                    if (!res.ok) {
                        return [];
                    }

                    const contentType = res.headers.get("content-type") || "";
                    if (!contentType.includes("application/json")) {
                        return [];
                    }
                    return res.json();
                })
                .then(data => {
                    allLoads = data || [];
                    cargoContainer.innerHTML = "";

                    if (!Array.isArray(allLoads) || allLoads.length === 0) {
                        cargoContainer.innerHTML =
                            `<div class="no-cargo"><p>No Available Cargos</p></div>`;
                        return;
                    }

                    allLoads.forEach(load => {
                        cargoContainer.innerHTML += `
                    <div class="cargo-card" data-load-id="${load.loadId}">
                        <div class="cargo-image">
                            <img src="${load.cargoImage || 'image/default.jpg'}" alt="Cargo Image">
                        </div>
                        <div class="cargo-info">
                            <div class="cargo-header">
                                <h3>${load.shipperName || '-'}</h3>
                                <span class="status-badge ${getStatusClass(load.status)}">
                                    ${load.status || '-'}
                                </span>
                            </div>
                            <p>Pickup ${load.pickupLocation || '-'} -> ${load.dropLocation || '-'}</p>
                            <p>Weight ${formatWeight(load.weightKg, load.weightUnit)}</p>
                            <p>Price ${load.price || 0}</p>
                        </div>

                        <button class="details-btn"
                            onclick="openDetails('${load.loadId}')">
                            Details
                        </button>
                    </div>
                `;
                    });

                    highlightNearbyLoads();
                })
                .catch(err => {
                    cargoContainer.innerHTML =
                        `<div class="no-cargo"><p>Unable to load cargos</p></div>`;
                });
        }

        // Open Details
        window.openDetails = function (id) {
            const load = allLoads.find(l => l.loadId == id);

            if (!load) {
                alert("Load not found");
                return;
            }

            selectedLoadId = load.loadId;

            dLoadId.innerText = load.loadId || "-";
            dType.innerText = load.loadType || "-";
            dWeight.innerText = formatWeight(load.weightKg, load.weightUnit);
            dPrice.innerText = "Rs " + (load.price || 0);
            dDate.innerText = load.expectedDate || "-";
            dRoute.innerText =
                (load.pickupLocation || "-") + " -> " +
                (load.dropLocation || "-");
            dAdd.innerText = formatAdditionalDetails(load.additionalDetails);

            detailsModal.style.display = "flex";
        };

        // Close Modal
        window.closeDetails = function () {
            detailsModal.style.display = "none";
        };

        // Confirm Load (Driver Accept)
        confirmBtn.addEventListener("click", async function () {
            if (currentUserRole !== "DRIVER") {
                alert("Only driver can confirm the load");
                return;
            }

            if (!selectedLoadId) {
                alert("No load selected");
                return;
            }

            const kycCompleted = await ensureDriverKycCompleted();
            if (!kycCompleted) return;

            fetch(`/api/cargo-available/${selectedLoadId}/request`, {
                method: "POST",
                credentials: "include",
                headers: authHeaders
            })
                .then(res => {
                    if (res.status === 401) {
                        window.location.href = "/login";
                        return;
                    }
                    if (res.status === 403) {
                        return res.json()
                            .then(payload => {
                                if (payload && payload.kycRequired) {
                                    alert(payload.message || "Please complete KYC before confirming cargo.");
                                    window.location.href = payload.redirectUrl || "/driver-verification?kycRequired=cargo-confirm";
                                    return;
                                }
                                alert(payload && payload.message ? payload.message : "Only driver can confirm the load");
                            })
                            .catch(() => {
                                alert("Only driver can confirm the load");
                            });
                    }
                    if (!res.ok) {
                        alert("Unable to accept load");
                        return;
                    }

                    alert("Load request sent successfully.");
                    closeDetails();
                    loadCargos();
                })
                .catch(err => {
                });
        });

        // Search Filter
        searchInput.addEventListener("input", function () {
            const term = this.value.toLowerCase();

            document.querySelectorAll(".cargo-card").forEach(card => {
                card.style.display =
                    card.innerText.toLowerCase().includes(term)
                        ? "flex"
                        : "none";
            });
        });

        loadCurrentUserRole().finally(loadCargos);
    };

    if (document.readyState !== "loading") {
        window.initCargoAvailablePage();
    } else {
        document.addEventListener("DOMContentLoaded", window.initCargoAvailablePage);
    }
})();
