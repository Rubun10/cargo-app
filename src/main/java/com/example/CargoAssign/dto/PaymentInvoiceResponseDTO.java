package com.example.CargoAssign.dto;

import java.util.List;

public class PaymentInvoiceResponseDTO {

    private final Double totalEarnings;
    private final Double paidAmount;
    private final Double pendingAmount;
    private final Long pendingInvoices;
    private final List<PaymentInvoiceItemDTO> invoices;

    public PaymentInvoiceResponseDTO(Double totalEarnings,
                                     Double paidAmount,
                                     Double pendingAmount,
                                     Long pendingInvoices,
                                     List<PaymentInvoiceItemDTO> invoices) {
        this.totalEarnings = totalEarnings;
        this.paidAmount = paidAmount;
        this.pendingAmount = pendingAmount;
        this.pendingInvoices = pendingInvoices;
        this.invoices = invoices;
    }

    public Double getTotalEarnings() {
        return totalEarnings;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public Double getPendingAmount() {
        return pendingAmount;
    }

    public Long getPendingInvoices() {
        return pendingInvoices;
    }

    public List<PaymentInvoiceItemDTO> getInvoices() {
        return invoices;
    }
}
