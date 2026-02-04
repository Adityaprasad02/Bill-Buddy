package com.company.Bill_Bridge.model;


import com.company.Bill_Bridge.model.enums.LoginAuthProvider;
import com.company.Bill_Bridge.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.security.AuthProvider;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "app_users")
@Builder
public class User {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO)
    private Long id ;

    @Column(name = "username" , unique = true , nullable = false)
    @NotBlank(message = "username is required")
    private String username ;

    @Column(name = "email" , unique = true , nullable = false)
    @NotBlank(message = "email is required")
    @Email
    private String email ;


    @Column(name = "password" , nullable = false)
    @NotBlank(message = "password is required")
    private String password ;

    @Column(name = "role" , nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "role is required")
    private Role role ;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false , name = "auth")
    private LoginAuthProvider authProvider ;



}
