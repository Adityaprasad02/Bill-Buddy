package com.company.Bill_Bridge.model.dtos.RequestDtos;

import com.company.Bill_Bridge.model.enums.PaymentMode;
import com.company.Bill_Bridge.model.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RequestBillCreation(

        @NotNull BigDecimal amount,
        @NotBlank String title ,
         String billLocation ,
        @NotNull PaymentMode mode
) {
}
