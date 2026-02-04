package com.company.Bill_Bridge.model.dtos.ResponseDtos;


import com.company.Bill_Bridge.model.enums.LoginAuthProvider;
import com.company.Bill_Bridge.model.enums.Role;



public record ResponseUserRegistration (

    Long userId ,
    String email ,
    String username ,
    Role role ,
    LoginAuthProvider authProvider
) { }
