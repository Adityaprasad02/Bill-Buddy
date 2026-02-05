package com.company.Bill_Bridge.model.dtos.ResponseDtos;

import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestBillCreation;
import com.company.Bill_Bridge.model.enums.PaymentMode;
import com.company.Bill_Bridge.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ResponseBillGenerated(
        BillMerchantDetails billMerchantDetails ,
        BillCustomerDetails billCustomerDetails,
        Long billId  ,
        String title ,
        BigDecimal amount,
        String billLocation ,
        PaymentStatus status  ,
        PaymentMode mode
        ) {
}

