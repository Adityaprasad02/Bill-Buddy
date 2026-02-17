package com.company.Bill_Bridge.model.dtos.RequestDtos;

import java.util.Map;

public record SendPaymentInfo(
        PaymentResponse paymentResponse ,
        Map<String , Object> paymentData
) {
}
