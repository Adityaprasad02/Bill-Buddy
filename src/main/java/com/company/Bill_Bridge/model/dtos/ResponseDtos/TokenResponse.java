package com.company.Bill_Bridge.model.dtos.ResponseDtos;

public record TokenResponse(
        String accessToken ,
        String refreshToken ,
        Long expiresIn ,
        ResponseUserRegistration userRegistration
) {
}
