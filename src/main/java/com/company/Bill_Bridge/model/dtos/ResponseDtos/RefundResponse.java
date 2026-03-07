package com.company.Bill_Bridge.model.dtos.ResponseDtos;

import com.company.Bill_Bridge.model.PaymentDetails;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import java.math.BigDecimal;

public record RefundResponse(
        String orderId, String refId, String resultStatus, String resultCode,
        String resultMsg, String refundId, BigDecimal refundAmount
) {
}
