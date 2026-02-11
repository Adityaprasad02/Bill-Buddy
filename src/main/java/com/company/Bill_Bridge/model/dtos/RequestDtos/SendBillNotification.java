package com.company.Bill_Bridge.model.dtos.RequestDtos;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SendBillNotification (
        @NotNull String customerName ,
        @NotNull Long billId ,
        @NotNull String title ,
        @NotNull BigDecimal amount,
        @NotNull String merchantName ,
        @NotNull String merchantUserName,
        @NotNull String merchantId ,
        @NotNull Long customerId

) {
}
