package com.company.Bill_Bridge.model.dtos.RequestDtos;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SendBillNotification (
        @NotNull String customerName ,
        @NotNull Long billId ,
        @NotNull String title ,
        @NotNull BigDecimal amount,
        @NotNull String merchantName
) {
}
