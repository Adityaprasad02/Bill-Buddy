package com.company.Bill_Bridge.model.dtos.ResponseDtos;

import com.company.Bill_Bridge.model.enums.MerchantType;
import com.company.Bill_Bridge.model.enums.Role;

import java.util.UUID;

public record ResponseMerchantRegister(
        ResponseUserRegistration userDetails ,
        UUID merchantId ,
        String businessName ,
        MerchantType type ,
        String address ,
        String gstNumber
) {
}
