package com.company.Bill_Bridge.service;

import com.company.Bill_Bridge.exceptions.DBException;
import com.company.Bill_Bridge.model.RefreshToken;
import com.company.Bill_Bridge.model.User;
import com.company.Bill_Bridge.model.dtos.RequestDtos.RequestUserRegister;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.ResponseUserRegistration;
import com.company.Bill_Bridge.model.dtos.ResponseDtos.TokenResponse;
import com.company.Bill_Bridge.model.enums.LoginAuthProvider;
import com.company.Bill_Bridge.model.enums.Role;
import com.company.Bill_Bridge.repository.RefreshTokenRepository;
import com.company.Bill_Bridge.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService ;
    private final JWTService jwtService ;
    private final CookieService cookieService ;
    private final RefreshTokenRepository refreshTokenRepository ;
    private final String frontendURL ;
    private final UserRepository userRepository ;

    public Oauth2SuccessHandler(UserService userService , JWTService jwtService, CookieService cookieService
            , RefreshTokenRepository refreshTokenRepository, @Value("${frontend.url}")String frontendURL, UserRepository userRepository) {
        this.userService = userService;
        this.jwtService = jwtService ;
        this.cookieService = cookieService ;
        this.refreshTokenRepository = refreshTokenRepository ;
        this.frontendURL = frontendURL;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("Auhenticatication : {} " , authentication.toString());

        // get oAuthUser return by google
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String registrationToken = "unknown" ;
        if(authentication instanceof OAuth2AuthenticationToken token){
            registrationToken = token.getAuthorizedClientRegistrationId() ; // ex-google , GitHub
        }

        switch(registrationToken){
            case "google" :
                String googleId  = oAuth2User.getAttribute("sub").toString();
                String email = oAuth2User.getAttribute("email").toString()  ;
                String name = oAuth2User.getAttribute("name").toString() ;

              var requestUserRegister =  new RequestUserRegister(
                        name ,
                        email ,
                        null ,
                        Role.CUSTOMER ,
                        LoginAuthProvider.GOOGLE
                ) ;
             
                
                
                // check if user already exist with email if nt then save the user

                ResponseUserRegistration res = null;
                Long userId = null ;
                try {
                    res = userService.createUser(requestUserRegister);
                } catch (DBException e) {
//
                         if(e.getMessage().startsWith("exist-")){
                             userId = Long.parseLong(e.getMessage().substring(6)) ;
                             log.info("{}" , userId);
//                             response.sendRedirect( frontendURL + "/oauth/success");
//                             return;
                         }else{
                             log.info("{}" , e.getMessage());
                             String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
                             response.sendRedirect(frontendURL + "/login?message="+errorMessage);
                             return ;
                         }

                    //return;
                }

                if(res!=null){
                    userId = res.userId() ;
                }

                User user2 = User.builder()
                            .id(userId)
                            .email(res!=null ? res.email() : email)
                            .authProvider(res!=null ? res.authProvider() : LoginAuthProvider.GOOGLE)
                            .role(Role.CUSTOMER)
                            .username(res!=null ? res.username() : name)
                            .build() ; 

                var operation =  generateAccessandRefreshTokenAndSetInCookie(user2 , response) ;

                log.info("TokenResponse-OAuth2 : {}" , operation.toString());
                String redirectUrl = UriComponentsBuilder
                        .fromUriString(frontendURL + "/oauth/success")
                        .queryParam("accessToken", operation.accessToken())
                        .queryParam("refreshToken", operation.refreshToken())
                        .build()
                        .toUriString();

               response.sendRedirect( redirectUrl);


                
        }
    }

    private TokenResponse generateAccessandRefreshTokenAndSetInCookie(User user,HttpServletResponse response){ 
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
        return new TokenResponse(accessToken , refreshToken , jwtService.getAccessTokenExpiration(),
                new ResponseUserRegistration(user.getId() , user.getEmail() , user.getUsername() ,
                            user.getRole() , user.getAuthProvider())
        );
       
        
    }


}
