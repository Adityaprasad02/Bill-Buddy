package com.company.Bill_Bridge.model.dtos.ResponseDtos;

import com.company.Bill_Bridge.model.enums.PaymentMode;
import com.company.Bill_Bridge.model.enums.PaymentStatus;

import java.math.BigDecimal;

public record FetchAllBills(
        Long billId ,
        BigDecimal billAmount ,
        PaymentMode paymentMode ,
        PaymentStatus billStatus ,
        String billTitle
) {
}
