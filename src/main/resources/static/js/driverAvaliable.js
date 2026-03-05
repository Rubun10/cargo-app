// js/driverAvaliable.js

let drivers = [];
let currentStatus = "ALL";
let driverMetrics = {};

function initDriverAvailablePage() {
  ensureRatingHeader();

  const token = localStorage.getItem("authToken");
  const authHeaders = token ? { Authorization: `Bearer ${token}` } : {};

  Promise.all([
    fetch("/api/drivers", { credentials: "include", headers: authHeaders }).then(res => (res.ok ? res.json() : [])),
    fetch("/api/payment-invoices", { credentials: "include", headers: authHeaders })
      .then(res => (res.ok ? res.json() : null))
      .catch(() => null)
  ])
    .then(([driverData, invoicePayload]) => {
      drivers = Array.isArray(driverData) ? driverData : [];
      driverMetrics = buildDriverMetrics(invoicePayload);
      renderDrivers(drivers);
    })
    .catch(err => {
      renderDrivers([]);
    });
}

function renderDrivers(list) {
  ensureRatingHeader();

  const table = document.getElementById("driverTable");
  if (!table) return;

  table.innerHTML = "";

  if (list.length === 0) {
    table.innerHTML = `<tr><td colspan="6">No drivers</td></tr>`;
    return;
  }

  list.forEach(d => {
    const name = d.name || "N/A";
    const phone = d.phone || "-";
    const location = d.location || "-";
    const vehicleType = d.vehicleType || "-";
    const experienceYears = Number.isFinite(Number(d.experienceYears)) ? Number(d.experienceYears) : "-";
    const status = (d.status || "OFFLINE").toUpperCase();
    const photo = d.photo || "https://via.placeholder.com/40?text=No+Photo";
    const metrics = getMetricsForDriver(d);
    table.innerHTML += `
      <tr>
        <td><img src="${photo}" width="40" alt="Driver profile" onerror="this.src='https://via.placeholder.com/40?text=No+Photo'"></td>
        <td>
          <b>${name}</b><br>
          Phone: ${phone}<br>
          Location: ${location}<br>
          Experience: ${experienceYears} year(s)<br>
          Vehicle Type: ${vehicleType}
        </td>
        <td>
          <div class="rating-stars">${renderAverageStars(metrics.averageStars)}</div>
          <div class="rating-meta">Credit Points: ${metrics.creditPoints}</div>
        </td>
        <td>${status}</td>
        <td>${vehicleType}</td>
        <td>
          ${
            status === "OFFLINE" || phone === "-"
              ? `<button class="call-btn" disabled>Unavailable</button>`
              : `<a href="tel:${phone}"><button class="call-btn">Call</button></a>`
          }
        </td>
      </tr>`;
  });
}

function ensureRatingHeader() {
  const headerRow = document.querySelector("table thead tr");
  if (!headerRow) return;

  const headers = Array.from(headerRow.querySelectorAll("th"));
  const hasRating = headers.some(th => (th.textContent || "").trim().toUpperCase() === "RATING");
  if (hasRating) return;

  const ratingTh = document.createElement("th");
  ratingTh.textContent = "Rating";

  if (headers.length >= 3) {
    headerRow.insertBefore(ratingTh, headers[2]);
  } else {
    headerRow.appendChild(ratingTh);
  }
}

function buildDriverMetrics(invoicePayload) {
  const metricsByDriverId = {};
  const invoices = invoicePayload && Array.isArray(invoicePayload.invoices)
    ? invoicePayload.invoices
    : [];

  invoices.forEach(item => {
    const driverId = Number(item.driverId);
    const rating = Number(item.driverReviewRating);
    if (!Number.isFinite(driverId) || !Number.isFinite(rating) || rating <= 0) return;

    if (!metricsByDriverId[driverId]) {
      metricsByDriverId[driverId] = {
        totalStars: 0,
        reviews: 0,
        creditPoints: 0,
        averageStars: 0
      };
    }

    const normalizedRating = Math.max(1, Math.min(5, Math.round(rating)));
    const words = countWords(item.driverReviewComment || "");

    metricsByDriverId[driverId].totalStars += normalizedRating;
    metricsByDriverId[driverId].reviews += 1;
    metricsByDriverId[driverId].creditPoints += words * normalizedRating;
  });

  Object.keys(metricsByDriverId).forEach(driverId => {
    const data = metricsByDriverId[driverId];
    data.averageStars = data.reviews > 0 ? data.totalStars / data.reviews : 0;
  });

  return metricsByDriverId;
}

function getMetricsForDriver(driver) {
  const driverId = Number(driver.userId);
  const data = driverMetrics[driverId];
  if (!data) {
    return {
      totalStars: 0,
      reviews: 0,
      averageStars: 0,
      creditPoints: 0
    };
  }
  return data;
}

function countWords(text) {
  if (!text) return 0;
  return String(text).trim().split(/\s+/).filter(Boolean).length;
}

function renderAverageStars(average) {
  const rounded = Math.max(0, Math.min(5, Math.round(Number(average) || 0)));
  return `<span class="filled-star">${"&#9733;".repeat(rounded)}</span><span class="empty-star">${"&#9734;".repeat(5 - rounded)}</span>`;
}

window.filterStatus = function (status) {
  currentStatus = status;
  applyFilters();
};

window.searchDrivers = function () {
  applyFilters();
};

function applyFilters() {
  const text = document.getElementById("search").value.toLowerCase();

  const filtered = drivers.filter(d =>
    (currentStatus === "ALL" || (d.status || "").toUpperCase() === currentStatus) &&
    (
      (d.name || "").toLowerCase().includes(text) ||
      (d.phone || "").includes(text) ||
      (d.vehicleType || "").toLowerCase().includes(text) ||
      (d.location || "").toLowerCase().includes(text)
    )
  );

  renderDrivers(filtered);
}

window.initDriverAvailablePage = initDriverAvailablePage;
