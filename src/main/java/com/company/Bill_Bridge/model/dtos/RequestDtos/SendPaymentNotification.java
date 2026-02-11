package com.company.Bill_Bridge.model.dtos.RequestDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SendPaymentNotification(
        @NotNull  Long billId,
        @NotNull Long customerId,
        @NotBlank  String customerName,
        @NotNull String merchantId,
        @NotBlank String merchantName,
        @NotBlank String title,
        @NotBlank String status,
        @NotBlank String timestamp,
        @NotNull BigDecimal amount
) {}

