package com.company.Bill_Bridge.model.dtos.ResponseDtos;

import java.util.UUID;

public record BillMerchantDetails (
        UUID merchantId ,
        String merchantName,
        String merchantUserName
)
{}
