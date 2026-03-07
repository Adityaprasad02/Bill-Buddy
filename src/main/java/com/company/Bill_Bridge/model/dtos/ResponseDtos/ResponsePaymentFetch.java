package com.company.Bill_Bridge.model.dtos.ResponseDtos;

import com.company.Bill_Bridge.model.Bill;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponsePaymentFetch(
        Long id,
        String TxnId,

        String bankTxnId,
        String orderId,
        BigDecimal txnAmount,
        String txnType,

        String gatewayName,

        String bankName,

        BigDecimal refundAmt,
        LocalDateTime txnDate,

        String resultStatus
) {
}
