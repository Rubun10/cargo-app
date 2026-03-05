function initCreditsReviewPage() {
    const listEl = document.getElementById("creditsReviewList");
    const titleEl = document.getElementById("creditsReviewTitle");
    const summaryEl = document.getElementById("creditsReviewSummary");
    if (!listEl || !titleEl || !summaryEl) return;

    Promise.all([
        fetch("/api/user/me", { credentials: "include" }).then(r => (r.ok ? r.json() : null)),
        fetch("/api/payment-invoices", { credentials: "include" }).then(r => (r.ok ? r.json() : null))
    ])
        .then(([user, invoicePayload]) => {
            const role = ((user && user.role) || "").toUpperCase();
            const isShipper = role === "SHIPPER";
            const items = invoicePayload && Array.isArray(invoicePayload.invoices)
                ? invoicePayload.invoices
                : [];

            titleEl.textContent = isShipper ? "Shipper Credits & Driver Reviews" : "Driver Credits & Reviews";

            if (items.length === 0) {
                summaryEl.textContent = "No records found.";
                listEl.innerHTML = `<div class="empty-message">No credits/reviews available yet.</div>`;
                return;
            }

            renderCreditItems(items, isShipper, listEl);
            updateTotalCredits(summaryEl, listEl, items.length);
            bindInteractions(listEl, summaryEl, items.length, isShipper);
        })
        .catch(() => {
            summaryEl.textContent = "Unable to load credits/reviews.";
            listEl.innerHTML = `<div class="empty-message">Please try again.</div>`;
        });
}

function renderCreditItems(items, isShipper, listEl) {
    listEl.innerHTML = items.map(item => {
        const savedStars = normalizeStars(item.driverReviewRating);
        const savedReview = (item.driverReviewComment || "").trim();
        const canReview = isShipper && item.canReview === true;

        const activeStars = canReview ? (savedStars > 0 ? savedStars : 4) : savedStars;
        const activeReview = canReview ? "" : savedReview;
        const words = countWords(activeReview);
        const credits = words * activeStars;

        const partnerName = isShipper
            ? (item.driverName || "Driver Name")
            : (item.shipperName || "Shipper Name");
        const partnerLabel = isShipper ? "Driver Name" : "Shipper Name";
        const cargoImage = item.cargoImage || "/images/imgtruck.png";
        const tripId = item.tripId || "";

        return `
            <div class="credit-item" data-trip-id="${escapeHtml(tripId)}">
                <div class="credit-top">
                    <img src="${escapeHtml(cargoImage)}" alt="Cargo Image" class="cargo-photo">
                    <div class="star-box">
                        <div class="credit-label top-label">Star</div>
                        ${canReview ? `
                            <select class="credit-star-input">${renderStarOptions(activeStars)}</select>
                        ` : ``}
                        <div class="stars star-view">${renderStars(activeStars)}</div>
                    </div>
                </div>

                <div class="credit-body">
                    <div class="credit-name">${partnerLabel} : ${escapeHtml(partnerName)}</div>
                    <div class="credit-line"><strong>Trip:</strong> ${escapeHtml(tripId || "-")}</div>
                    <div class="credit-line"><strong>Credits Points:</strong> <span class="credit-points">${credits}</span></div>
                    <div class="credit-line formula-line"><strong>Formula:</strong> Words(<span class="word-count">${words}</span>) x Stars(<span class="star-count">${activeStars}</span>)</div>

                    <div class="credit-label">Review</div>
                    ${canReview ? `
                        <textarea class="credit-review-input" rows="2" placeholder="Write review for driver"></textarea>
                        <button type="button" class="credit-submit-btn">Submit Review</button>
                    ` : `
                        <div class="review-readonly">${escapeHtml(savedReview || "No review submitted")}</div>
                    `}
                </div>
            </div>
        `;
    }).join("");
}

function bindInteractions(listEl, summaryEl, totalRecords, isShipper) {
    const recalc = () => {
        listEl.querySelectorAll(".credit-item").forEach(card => {
            const input = card.querySelector(".credit-review-input");
            const select = card.querySelector(".credit-star-input");
            if (!input || !select) return;

            const words = countWords(input.value || "");
            const stars = normalizeStars(select.value);
            const points = words * stars;

            const pointsEl = card.querySelector(".credit-points");
            const wordsEl = card.querySelector(".word-count");
            const starsEl = card.querySelector(".star-count");
            const starViewEl = card.querySelector(".star-view");
            if (pointsEl) pointsEl.textContent = String(points);
            if (wordsEl) wordsEl.textContent = String(words);
            if (starsEl) starsEl.textContent = String(stars);
            if (starViewEl) starViewEl.textContent = renderStars(stars);
        });
        updateTotalCredits(summaryEl, listEl, totalRecords);
    };

    listEl.querySelectorAll(".credit-review-input").forEach(el => {
        el.addEventListener("input", recalc);
    });
    listEl.querySelectorAll(".credit-star-input").forEach(el => {
        el.addEventListener("change", recalc);
    });

    if (!isShipper) return;

    listEl.querySelectorAll(".credit-submit-btn").forEach(btn => {
        btn.addEventListener("click", async () => {
            const card = btn.closest(".credit-item");
            if (!card) return;

            const tripId = card.dataset.tripId;
            const reviewInput = card.querySelector(".credit-review-input");
            const starInput = card.querySelector(".credit-star-input");
            const reviewComment = (reviewInput && reviewInput.value ? reviewInput.value.trim() : "");
            const rating = normalizeStars(starInput ? starInput.value : 0);

            if (!tripId) {
                alert("Trip id missing");
                return;
            }
            if (rating < 1 || rating > 5) {
                alert("Please choose stars from 1 to 5");
                return;
            }

            btn.disabled = true;
            try {
                const res = await fetch(`/api/payment-invoices/${encodeURIComponent(tripId)}/review-driver`, {
                    method: "POST",
                    credentials: "include",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ rating, reviewComment })
                });

                const payload = await res.json().catch(() => ({}));
                if (!res.ok) {
                    alert(payload.message || "Unable to submit review");
                    return;
                }

                alert("Review submitted");
                initCreditsReviewPage();
            } catch (_) {
                alert("Unable to submit review");
            } finally {
                btn.disabled = false;
            }
        });
    });
}

function updateTotalCredits(summaryEl, listEl, totalRecords) {
    let totalCredits = 0;
    listEl.querySelectorAll(".credit-points").forEach(el => {
        totalCredits += Number(el.textContent || 0);
    });
    summaryEl.textContent = `Total Credits: ${totalCredits} | Total Records: ${totalRecords}`;
}

function normalizeStars(value) {
    const n = Number(value || 0);
    if (!Number.isFinite(n)) return 0;
    return Math.max(0, Math.min(5, Math.floor(n)));
}

function countWords(text) {
    if (!text) return 0;
    return text.trim().split(/\s+/).filter(Boolean).length;
}

function renderStars(star) {
    const rating = normalizeStars(star);
    return "★".repeat(rating) + "☆".repeat(5 - rating);
}

function renderStarOptions(selectedStar) {
    const selected = normalizeStars(selectedStar);
    let html = "";
    for (let i = 1; i <= 5; i++) {
        html += `<option value="${i}" ${i === selected ? "selected" : ""}>${i}</option>`;
    }
    return html;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
