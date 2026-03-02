package com.company.Bill_Bridge.controller;


import com.company.Bill_Bridge.exceptions.DBException;
import com.company.Bill_Bridge.model.RefreshToken;
import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.model.dtos.RequestDtos.LoginRequest;

import com.company.Bill_Bridge.model.dtos.ResponseDtos.ResponseUserRegistration;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.TokenResponse;
import com.company.Bill_Bridge.repository.RefreshTokenRepository;
import com.company.Bill_Bridge.repository.UserRepository;
import com.company.Bill_Bridge.service.CookieService;
import com.company.Bill_Bridge.service.JWTService;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager ;

    @Autowired
    private UserRepository userRepository ;

    @Autowired
    private JWTService jwtService ;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository ;

    @Autowired
    private CookieService cookieService ;




    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest , HttpServletResponse response){

        String username = loginRequest.username();
        String password = loginRequest.password();


        Authentication authentication = authenticationManager
                                       .authenticate(new UsernamePasswordAuthenticationToken(username,password)) ;

        User user = userRepository.findByUsername(username).orElseThrow(()->
                        new DBException("user not found with username " + username)) ;

        //generate jti for refresh token
        String jti = UUID.randomUUID().toString() ;

        //refresh token build
        RefreshToken refreshTokenObj = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                .revoked(false)
                .build() ;

        // save
        refreshTokenRepository.save(refreshTokenObj) ;

        // generate access token
        String accessToken  = jwtService.generateAccessToken(user) ;

        // generate refresh token
        String refreshToken = jwtService.generateRefreshToken(user,refreshTokenObj.getJti()) ;

        // attach refresh token to cookie
        cookieService.attachRefreshCookie(response , refreshToken , Math.toIntExact(jwtService.getRefreshTokenExpiration()));

        // add no store in headers
        cookieService.addNoStoreHeaders(response);

        // return TokenResponse ;
        return ResponseEntity.ok( new TokenResponse(accessToken , refreshToken , jwtService.getAccessTokenExpiration(),
                new ResponseUserRegistration(user.getId() , user.getEmail() , user.getUsername() ,
                            user.getRole() , user.getAuthProvider()))
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request , HttpServletResponse response){

        String refreshToken  = null ;

        String refreshHeader = request.getHeader("X-REFRESH-TOKEN") ;
        if(refreshHeader!=null && !refreshHeader.isBlank()){
             refreshToken = refreshHeader.trim();
        }

        if(refreshToken!=null && jwtService.isRefreshToken(refreshToken)){
            String jti = jwtService.getJti(refreshToken) ;

            RefreshToken oldRefreshToken = refreshTokenRepository.findByJti(jti)
                                          .orElseThrow(()-> new BadCredentialsException("the refresh token ain't exist")) ;

            if(oldRefreshToken.isRevoked()){
                throw new BadCredentialsException("refreshToken has been revoked")  ;
            }

            if(oldRefreshToken.getExpiresAt().isBefore(Instant.now())){
                throw new BadCredentialsException("refresh token expired") ;
            }

            // revoke -> true
            oldRefreshToken.setRevoked(true);

            //generate new jti for new refresh token
            String newJti = UUID.randomUUID().toString() ;

            // set replaced by field from null to new jti
            oldRefreshToken.setReplacedByToken(newJti);

            // save the changes
            refreshTokenRepository.save(oldRefreshToken) ;

            User user = oldRefreshToken.getUser() ;


            // generate new Refresh Token
            RefreshToken refreshTokenObj = RefreshToken.builder()
                    .jti(newJti)
                    .user(user)
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                    .revoked(false)
                    .build() ;

            // save
            refreshTokenRepository.save(refreshTokenObj) ;

            // generate new access token
            String newAccessToken  = jwtService.generateAccessToken(user) ;

            // generate new refresh token
            String newRefreshToken = jwtService.generateRefreshToken(user,refreshTokenObj.getJti()) ;

            // attach refresh token to cookie
            cookieService.attachRefreshCookie(response , newRefreshToken , Math.toIntExact(jwtService.getRefreshTokenExpiration()));

            // add no store in headers
            cookieService.addNoStoreHeaders(response);

            // return TokenResponse ;
            return ResponseEntity.ok( new TokenResponse(newAccessToken , newRefreshToken , jwtService.getAccessTokenExpiration(),
                    new ResponseUserRegistration(user.getId() , user.getEmail() , user.getUsername() ,
                            user.getRole() , user.getAuthProvider()))
            );
        }
           return null ;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void>  logout (HttpServletRequest request , HttpServletResponse response){
        String refreshToken  = null ;

        String refreshHeader = request.getHeader("X-REFRESH-TOKEN") ;
        if(refreshHeader!=null && !refreshHeader.isBlank()){
            refreshToken = refreshHeader.trim();

            if(refreshToken!=null && jwtService.isRefreshToken(refreshToken)) {
                String jti = jwtService.getJti(refreshToken);

                RefreshToken oldRefreshToken = refreshTokenRepository.findByJti(jti)
                        .orElseThrow(() -> new BadCredentialsException("the refresh token ain't exist"));

                oldRefreshToken.setRevoked(true);
                refreshTokenRepository.save(oldRefreshToken) ;

                cookieService.clearRefreshCookie(response) ;
                cookieService.addNoStoreHeaders(response);

                SecurityContextHolder.clearContext();
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            }
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
