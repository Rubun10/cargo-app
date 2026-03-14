(function () {
  if (window.__globalCompletionPopperInit) return;
  window.__globalCompletionPopperInit = true;

  const POLL_MS = 8000;
  const SEEN_KEY = "globalCompletionSeenIds";
  const KYC_SEEN_KEY = "globalKYCSeenIds";
  const REVIEW_SEEN_KEY = "globalDriverReviewSeenIds";
  let completionBaselineReady = false;
  let reviewBaselineReady = false;
  let pollId = null;
  let timerId = null;
  let burstId = null;

  function getSeenSet() {
    try {
      const raw = sessionStorage.getItem(SEEN_KEY);
      if (!raw) return new Set();
      const arr = JSON.parse(raw);
      if (!Array.isArray(arr)) return new Set();
      return new Set(arr.map(v => String(v)));
    } catch (_) {
      return new Set();
    }
  }

  function saveSeenSet(set) {
    try {
      sessionStorage.setItem(SEEN_KEY, JSON.stringify(Array.from(set)));
    } catch (_) {}
  }

  function getKYCSeenSet() {
    try {
      const raw = sessionStorage.getItem(KYC_SEEN_KEY);
      if (!raw) return new Set();
      const arr = JSON.parse(raw);
      if (!Array.isArray(arr)) return new Set();
      return new Set(arr.map(v => String(v)));
    } catch (_) {
      return new Set();
    }
  }

  function saveKYCSeenSet(set) {
    try {
      sessionStorage.setItem(KYC_SEEN_KEY, JSON.stringify(Array.from(set)));
    } catch (_) {}
  }

  function getReviewSeenSet() {
    try {
      const raw = sessionStorage.getItem(REVIEW_SEEN_KEY);
      if (!raw) return new Set();
      const arr = JSON.parse(raw);
      if (!Array.isArray(arr)) return new Set();
      return new Set(arr.map(v => String(v)));
    } catch (_) {
      return new Set();
    }
  }

  function saveReviewSeenSet(set) {
    try {
      sessionStorage.setItem(REVIEW_SEEN_KEY, JSON.stringify(Array.from(set)));
    } catch (_) {}
  }

  function ensurePopperDom(isKYC = false, isApproval = true) {
    const existingId = isKYC ? "globalKYCPopper" : "globalCompletionPopper";
    if (document.getElementById(existingId)) return;

    const style = document.createElement("style");
    style.id = isKYC ? "globalKYCPopperStyle" : "globalCompletionPopperStyle";
    
    if (isKYC) {
      // KYC Popper styles - Green for approval, Red for rejection
      style.textContent = 
        ".gcp-popper{position:fixed;inset:0;z-index:10040;display:none;align-items:center;justify-content:center;pointer-events:none}" +
        ".gcp-popper.active{display:flex}" +
        ".gcp-popper.approval{background:rgba(16,185,129,0.95)}" +
        ".gcp-popper.rejection{background:rgba(239,68,68,0.95)}" +
        ".gcp-core{display:flex;flex-direction:column;align-items:center;gap:10px;animation:gcpPopIn .32s ease}" +
        ".gcp-icon{width:170px;height:170px;display:flex;align-items:center;justify-content:center;font-size:96px;color:#fff}" +
        ".gcp-text{background:#fff;color:#111;padding:12px 28px;border-radius:999px;font-size:18px;font-weight:700;text-align:center;box-shadow:0 4px 20px rgba(0,0,0,0.3)}" +
        ".gcp-confetti{position:absolute;inset:0}" +
        ".gcp-piece{position:absolute;width:8px;height:16px;left:var(--sx,50%);top:var(--sy,50%);opacity:0;border-radius:2px;animation:gcpBurst .9s ease-out forwards;animation-delay:var(--delay,0ms);transform:translate(-50%,-50%);background:var(--color,#fff)}" +
        "@keyframes gcpPopIn{from{transform:scale(.78);opacity:0}to{transform:scale(1);opacity:1}}" +
        "@keyframes gcpBurst{0%{opacity:0;transform:translate(-50%,-50%) scale(.4) rotate(0deg)}12%{opacity:1}100%{opacity:0;transform:translate(calc(-50% + var(--dx,0px)),calc(-50% + var(--dy,0px))) rotate(var(--rot,0deg))}}";
    } else {
      style.textContent = 
        ".gcp-popper{position:fixed;inset:0;z-index:10040;display:none;align-items:center;justify-content:center;pointer-events:none;background:rgba(15,23,42,.62)}" +
        ".gcp-popper.active{display:flex}" +
        ".gcp-core{display:flex;flex-direction:column;align-items:center;gap:10px;animation:gcpPopIn .32s ease}" +
        ".gcp-icon{width:170px;height:170px;display:flex;align-items:center;justify-content:center;font-size:96px;color:#4f46e5}" +
        ".gcp-text{background:rgba(15,23,42,.84);color:#fff;padding:8px 14px;border-radius:999px;font-size:14px;font-weight:600;text-align:center}" +
        ".gcp-confetti{position:absolute;inset:0}" +
        ".gcp-piece{position:absolute;width:8px;height:16px;left:var(--sx,50%);top:var(--sy,50%);opacity:0;border-radius:2px;animation:gcpBurst .9s ease-out forwards;animation-delay:var(--delay,0ms);transform:translate(-50%,-50%);background:var(--color,#22d3ee)}" +
        "@keyframes gcpPopIn{from{transform:scale(.78);opacity:0}to{transform:scale(1);opacity:1}}" +
        "@keyframes gcpBurst{0%{opacity:0;transform:translate(-50%,-50%) scale(.4) rotate(0deg)}12%{opacity:1}100%{opacity:0;transform:translate(calc(-50% + var(--dx,0px)),calc(-50% + var(--dy,0px))) rotate(var(--rot,0deg))}}";
    }
    document.head.appendChild(style);

    const popper = document.createElement("div");
    popper.id = existingId;
    popper.className = "gcp-popper";
    
    if (isKYC) {
      const iconClass = isApproval ? "fa-circle-check" : "fa-circle-xmark";
      const iconColor = isApproval ? "#10b981" : "#ef4444";
      popper.innerHTML = 
        '<div class="gcp-core">' +
        '  <div class="gcp-icon" style="color:' + iconColor + '"><i class="fas ' + iconClass + '"></i></div>' +
        '  <div class="gcp-text" id="globalKYCCompletionText">KYC Approved!</div>' +
        "</div>" +
        '<div class="gcp-confetti" id="globalKYCCompletionConfetti"></div>';
    } else {
      popper.innerHTML =
        '<div class="gcp-core">' +
        '  <div class="gcp-icon"><i class="fas fa-circle-check"></i></div>' +
        '  <div class="gcp-text" id="globalCompletionText">Shipping completed</div>' +
        "</div>" +
        '<div class="gcp-confetti" id="globalCompletionConfetti"></div>';
    }
    document.body.appendChild(popper);
  }

  function appendConfetti(count, isKYC = false) {
    const confettiId = isKYC ? "globalKYCCompletionConfetti" : "globalCompletionConfetti";
    const confetti = document.getElementById(confettiId);
    if (!confetti) return;
    
    const colors = isKYC 
      ? ["#ffffff", "#10b981", "#34d399", "#6ee7b7", "#059669", "#047857"]
      : ["#4f46e5", "#22d3ee", "#f59e0b", "#a78bfa", "#10b981", "#ef4444"];
      
    for (let i = 0; i < count; i++) {
      const piece = document.createElement("span");
      piece.className = "gcp-piece";
      piece.style.setProperty("--sx", `${8 + Math.random() * 84}%`);
      piece.style.setProperty("--sy", `${10 + Math.random() * 80}%`);
      piece.style.setProperty("--dx", `${Math.round((Math.random() - 0.5) * 420)}px`);
      piece.style.setProperty("--dy", `${Math.round((Math.random() - 0.5) * 320)}px`);
      piece.style.setProperty("--rot", `${Math.round((Math.random() - 0.5) * 520)}deg`);
      piece.style.setProperty("--delay", `${Math.round(Math.random() * 180)}ms`);
      piece.style.setProperty("--color", colors[i % colors.length]);
      confetti.appendChild(piece);
      setTimeout(() => piece.remove(), 950);
    }
  }

  function showPopper(message, isKYC = false, isApproval = true) {
    const popperId = isKYC ? "globalKYCPopper" : "globalCompletionPopper";
    const textId = isKYC ? "globalKYCCompletionText" : "globalCompletionText";
    const confettiId = isKYC ? "globalKYCCompletionConfetti" : "globalCompletionConfetti";
    
    const popper = document.getElementById(popperId);
    const text = document.getElementById(textId);
    const confetti = document.getElementById(confettiId);
    if (!popper || !text) return;

    text.textContent = message || (isKYC ? (isApproval ? "KYC Approved!" : "KYC Rejected") : "Shipping completed");
    
    // Set colors based on KYC type
    if (isKYC) {
      popper.classList.remove("approval", "rejection");
      popper.classList.add(isApproval ? "approval" : "rejection");
    }
    
    if (isKYC) {
      const iconWrap = popper.querySelector(".gcp-icon");
      const icon = popper.querySelector(".gcp-icon i");
      if (iconWrap) {
        iconWrap.style.color = isApproval ? "#10b981" : "#ef4444";
      }
      if (icon) {
        icon.className = isApproval ? "fas fa-circle-check" : "fas fa-circle-xmark";
      }
    }

    if (confetti) confetti.innerHTML = "";
    appendConfetti(70, isKYC);
    popper.classList.add("active");

    if (timerId) clearTimeout(timerId);
    if (burstId) clearInterval(burstId);
    burstId = setInterval(() => appendConfetti(18, isKYC), 650);

    timerId = setTimeout(() => {
      popper.classList.remove("active");
      if (burstId) {
        clearInterval(burstId);
        burstId = null;
      }
      if (confetti) confetti.innerHTML = "";
    }, 4200);
  }

  async function fetchCurrentUser() {
    try {
      const res = await fetch("/api/user/me", { credentials: "include" });
      if (!res.ok) return null;
      const user = await res.json();
      return {
        role: String(user.role || "").toUpperCase(),
        userId: user.userId
      };
    } catch (_) {
      return null;
    }
  }

  // Monitor for shipping completions
  async function monitorCompletions() {
    try {
      const res = await fetch("/api/tracking/cargos", { credentials: "include" });
      if (!res.ok) return;
      const loads = await res.json();
      if (!Array.isArray(loads)) return;

      const completedIds = loads
        .filter(load => load && String(load.status || "").toUpperCase() === "COMPLETED")
        .map(load => String(load.loadId || "").trim())
        .filter(Boolean);

      const seen = getSeenSet();

      if (!completionBaselineReady) {
        completedIds.forEach(id => seen.add(id));
        saveSeenSet(seen);
        completionBaselineReady = true;
        return;
      }

      const newlyCompleted = completedIds.filter(id => !seen.has(id));
      if (newlyCompleted.length > 0) {
        newlyCompleted.forEach(id => seen.add(id));
        saveSeenSet(seen);
        const msg = newlyCompleted.length === 1
          ? `Shipping completed: ${newlyCompleted[0]}`
          : `${newlyCompleted.length} shipments completed`;
        showPopper(msg, false, true);
      }
    } catch (_) {}
  }

  // Monitor for KYC notifications (show for unread items even on first load)
  async function monitorKYCNotifications() {
    try {
      const res = await fetch("/api/notifications", { 
        credentials: "include",
        headers: { "Content-Type": "application/json" }
      });
      if (!res.ok) return;
      const notifications = await res.json();
      if (!Array.isArray(notifications)) return;

      const seen = getKYCSeenSet();
      const kycNotifications = notifications.filter(n => {
        const type = (n.type || "").toString().toUpperCase();
        return (type === "KYC_APPROVED" || type === "KYC_REJECTED") && n.read === false;
      });

      // Show only the latest unread KYC notification once.
      const newKYCNotifications = kycNotifications.filter(n => !seen.has(String(n.id)));
      if (newKYCNotifications.length > 0) {
        const latest = newKYCNotifications
          .slice()
          .sort((a, b) => {
            const aTime = a && a.createdAt ? new Date(a.createdAt).getTime() : 0;
            const bTime = b && b.createdAt ? new Date(b.createdAt).getTime() : 0;
            if (aTime !== bTime) return bTime - aTime;
            const aId = Number(a && a.id) || 0;
            const bId = Number(b && b.id) || 0;
            return bId - aId;
          })[0];

        if (latest) {
          const isApproval = (latest.type || "").toString().toUpperCase() === "KYC_APPROVED";
          const message = isApproval
            ? "Your KYC has been Approved! "
            : "Your KYC has been Rejected. Please resubmit correct documents.";

          ensurePopperDom(true, isApproval);
          showPopper(message, true, isApproval);
        }

        // Mark all unread KYC notifications as read so the popper shows only once.
        newKYCNotifications.forEach(n => {
          seen.add(String(n.id));
          fetch(`/api/notifications/${n.id}/read`, {
            method: "POST",
            credentials: "include"
          }).catch(() => {});
        });
        saveKYCSeenSet(seen);
      }
    } catch (_) {}
  }

  async function showKycApprovedFallback(userId) {
    if (!userId) return;
    const key = `kycApprovedShown:${userId}`;
    if (localStorage.getItem(key) === "true") return;
    try {
      const res = await fetch("/api/kyc/status", { credentials: "include" });
      if (!res.ok) return;
      const payload = await res.json();
      if (payload && payload.kycCompleted === true) {
        ensurePopperDom(true, true);
        showPopper("Your KYC has been Approved!", true, true);
        localStorage.setItem(key, "true");
      }
    } catch (_) {}
  }

  // Monitor for driver review completion notifications
  async function monitorDriverReviewNotifications(role) {
    if (role !== "DRIVER") return;

    try {
      const res = await fetch("/api/notifications", {
        credentials: "include",
        headers: { "Content-Type": "application/json" }
      });
      if (!res.ok) return;
      const notifications = await res.json();
      if (!Array.isArray(notifications)) return;

      const seen = getReviewSeenSet();
      const reviewNotifications = notifications.filter(n => {
        const type = (n.type || "").toString().toUpperCase();
        return type === "DRIVER_REVIEWED";
      });

      if (!reviewBaselineReady) {
        reviewNotifications.forEach(n => seen.add(String(n.id)));
        saveReviewSeenSet(seen);
        reviewBaselineReady = true;
        return;
      }

      const newReviewNotifications = reviewNotifications.filter(n => !seen.has(String(n.id)));
      if (newReviewNotifications.length > 0) {
        newReviewNotifications.forEach(n => {
          seen.add(String(n.id));
          const message = n && n.message
            ? n.message
            : "Shipper reviewed your completed trip.";
          showPopper(message, false, true);
        });
        saveReviewSeenSet(seen);
      }
    } catch (_) {}
  }

  async function init() {
    const user = await fetchCurrentUser();
    if (!user || (user.role !== "DRIVER" && user.role !== "SHIPPER")) return;
    const role = user.role;
    const hasLocalCompletionPopper = !!document.getElementById("completionPopper");
    
    // Initialize completion popper only on pages without local completion popper
    if (!hasLocalCompletionPopper) {
      ensurePopperDom(false, true);
      await monitorCompletions();
    }
    
    // Initialize KYC popper
    ensurePopperDom(true, true);  // approval
    ensurePopperDom(true, false); // rejection
    await monitorKYCNotifications();
    await showKycApprovedFallback(user.userId);
    await monitorDriverReviewNotifications(role);
    
    if (pollId) clearInterval(pollId);
    pollId = setInterval(() => {
      if (!hasLocalCompletionPopper) {
        monitorCompletions();
      }
      monitorKYCNotifications();
      monitorDriverReviewNotifications(role);
    }, POLL_MS);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
