package com.company.Bill_Bridge.model.dtos.RequestDtos;

import com.company.Bill_Bridge.model.enums.LoginAuthProvider;
import com.company.Bill_Bridge.model.enums.Role;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
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
public class RequestUserRegister {

    @NotBlank(message = "username is required")
    private String username ;


    @NotBlank(message = "email is required")
    @Email
    private String email ;




    private String password ;


    @NotNull(message = "role is required")
    private Role role ;

    @Column(nullable = false , name = "auth")
    private LoginAuthProvider authProvider ;
}
