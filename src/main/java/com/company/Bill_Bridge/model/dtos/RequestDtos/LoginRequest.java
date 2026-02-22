package com.company.Bill_Bridge.model.dtos.RequestDtos;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank String username ,
        @NotBlank String password
) {
}
