package com.company.Bill_Bridge.model.dtos.ResponseDtos;

import com.company.Bill_Bridge.model.enums.MerchantType;

import java.util.UUID;

public record ResponseGetMerchantDetails(
        UUID merchantId ,
        String businessName ,
        MerchantType type ,
        String address ,
        Long gstNumber
) {
}
