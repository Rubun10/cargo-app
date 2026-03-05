package com.example.CargoAssign.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.CargoAssign.Model.LoadStatus;
import com.example.CargoAssign.Model.PostLoad;
import com.example.CargoAssign.Model.User;
import com.example.CargoAssign.dto.DriverReviewRequest;
import com.example.CargoAssign.dto.PaymentInvoiceItemDTO;
import com.example.CargoAssign.dto.PaymentInvoiceResponseDTO;
import com.example.CargoAssign.repo.PostLoadRepo;
import com.example.CargoAssign.repo.UserRepository;

@Service
public class PaymentInvoiceService {

    private final PostLoadRepo postLoadRepo;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public PaymentInvoiceService(PostLoadRepo postLoadRepo,
                                 UserRepository userRepository,
                                 NotificationService notificationService) {
        this.postLoadRepo = postLoadRepo;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public PaymentInvoiceResponseDTO getMyPaymentInvoices(User sessionUser) {
        List<PostLoad> loads = getUserLoads(sessionUser);

        boolean isShipper = "SHIPPER".equalsIgnoreCase(sessionUser.getRole());
        List<PaymentInvoiceItemDTO> invoices = buildInvoices(loads, isShipper);

        double total = invoices.stream().mapToDouble(i -> i.getAmount() == null ? 0.0 : i.getAmount()).sum();
        double paid = invoices.stream()
                .filter(i -> "PAID".equalsIgnoreCase(i.getStatus()))
                .mapToDouble(i -> i.getAmount() == null ? 0.0 : i.getAmount())
                .sum();
        long pendingCount = invoices.stream()
                .filter(i -> "PENDING".equalsIgnoreCase(i.getStatus()))
                .count();

        return new PaymentInvoiceResponseDTO(
                total,
                paid,
                total - paid,
                pendingCount,
                invoices
        );
    }

    public void submitDriverReview(User shipper, String tripId, DriverReviewRequest request) {
        PostLoad load = postLoadRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Cargo not found"));

        if (!"SHIPPER".equalsIgnoreCase(shipper.getRole())) {
            throw new RuntimeException("Only shipper can submit review");
        }

        if (load.getUser() == null || load.getUser().getId() == null
                || !load.getUser().getId().equals(shipper.getId())) {
            throw new RuntimeException("You can review only your own cargo");
        }

        if (load.getStatus() != LoadStatus.COMPLETED) {
            throw new RuntimeException("Driver must complete cargo before review");
        }

        if (load.getDriverReviewRating() != null) {
            throw new RuntimeException("Review already submitted");
        }
        load.setDriverReviewRating(request.getRating());
        load.setDriverReviewComment(
                request.getReviewComment() == null ? null : request.getReviewComment().trim()
        );
        load.setDriverReviewedAt(LocalDateTime.now());

        postLoadRepo.save(load);

        if (load.getDriverID() != null) {
            userRepository.findById(load.getDriverID()).ifPresent(driver ->
                    notificationService.createNotification(
                            driver,
                            shipper,
                            "LOAD",
                            extractLoadEntityId(load.getLoadId()),
                            "DRIVER_REVIEWED",
                            "Shipper reviewed your completed cargo " + load.getLoadId() + "."
                    )
            );
        }
    }

    private List<PostLoad> getUserLoads(User sessionUser) {
        List<PostLoad> loads;
        if ("SHIPPER".equalsIgnoreCase(sessionUser.getRole())) {
            loads = postLoadRepo.findByUserId(sessionUser.getId());
        } else {
            loads = postLoadRepo.findByDriverID(sessionUser.getId());
        }

        loads.sort(
                Comparator.comparing(
                        PostLoad::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed()
        );
        return loads;
    }

    private List<PaymentInvoiceItemDTO> buildInvoices(List<PostLoad> loads, boolean isShipper) {
        return loads.stream()
                .map(load -> {
                    boolean completed = load.getStatus() == LoadStatus.COMPLETED;
                    return new PaymentInvoiceItemDTO(
                            buildInvoiceId(load.getLoadId()),
                            load.getLoadId(),
                            resolveDate(load),
                            load.getPrice() == null ? 0.0 : load.getPrice(),
                            completed ? "PAID" : "PENDING",
                            load.getStatus() == null ? null : load.getStatus().name(),
                            load.getLoadType(),
                            load.getPickupLocation(),
                            load.getDropLocation(),
                            load.getWeightKg(),
                            load.getDriverName(),
                            load.getDriverID(),
                            load.getShipperName(),
                            load.getAdditionalDetails(),
                            load.getCargoImage(),
                            load.getPaymentDone(),
                            load.getPaymentDate(),
                            load.getDriverReviewRating(),
                            load.getDriverReviewComment(),
                            load.getDriverReviewedAt(),
                            isShipper && completed && load.getDriverReviewRating() == null
                    );
                })
                .toList();
    }

    private LocalDate resolveDate(PostLoad load) {
        if (load.getExpectedDate() != null) {
            return load.getExpectedDate();
        }
        if (load.getCreatedAt() != null) {
            return load.getCreatedAt().toLocalDate();
        }
        return null;
    }

    private String buildInvoiceId(String loadId) {
        if (loadId == null || loadId.isBlank()) {
            return "INV-UNKNOWN";
        }
        return "INV-" + loadId.toUpperCase();
    }

    private Long extractLoadEntityId(String loadId) {
        if (loadId == null || loadId.isBlank()) {
            return null;
        }
        String numeric = loadId.replace("LD-", "").trim();
        try {
            return Long.parseLong(numeric);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
