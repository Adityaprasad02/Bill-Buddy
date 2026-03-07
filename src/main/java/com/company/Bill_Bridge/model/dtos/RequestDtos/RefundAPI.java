package com.company.Bill_Bridge.model.dtos.RequestDtos;

import java.math.BigDecimal;

public record RefundAPI(
        Long id ,
        BigDecimal refundAmount
) {
}
