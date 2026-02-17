package com.company.Bill_Bridge.model.dtos.RequestDtos;

import com.company.Bill_Bridge.model.enums.MerchantType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestMerchantRegister {
    @NotBlank
    @NotNull
    private String businessName ;

    @NotNull
    private MerchantType type ;

    @NotBlank
    private String address ;


    @NotNull
    private String gstNumber ;
}
