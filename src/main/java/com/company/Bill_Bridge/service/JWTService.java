package com.company.Bill_Bridge.service;


import com.company.Bill_Bridge.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
@Setter
public class JWTService {


    private final String secret ;


    private final String issuer ;


    private final Long accessTokenExpiration ;


    private final Long refreshTokenExpiration ;

    private final SecretKey secretKey ;

    public JWTService(@Value("${security.jwt.secret}") String secret ,
                      @Value("${security.jwt.issuer}") String issuer ,
                      @Value("${security.jwt.access-token-expiration}") Long accessTokenExpiration,
                      @Value("${security.jwt.refresh-token-expiration}") Long refreshTokenExpiration){

        this.secret = secret ;
        this.issuer = issuer ;
        this.accessTokenExpiration = accessTokenExpiration ;
        this.refreshTokenExpiration = refreshTokenExpiration ;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)) ;

    }







    // generate access token
    public String generateAccessToken(User user){
      return Jwts.builder()
              .id(UUID.randomUUID().toString())
              .claims(Map.of(
                      "typ" , "access" ,
                      "role" , user.getRole() ,
                      "email" , user.getEmail()
              ))
              .subject(user.getId().toString())
              .issuedAt(Date.from(Instant.now()))
              .expiration(Date.from(Instant.now().plusMillis(accessTokenExpiration)))
              .issuer(issuer)
              .signWith(secretKey)
              .compact() ;
    }

    // generate refresh token
   public String generateRefreshToken(User user , String jti ){
          return Jwts
                  .builder()
                  .id(jti)
                  .claim("typ" , "refresh")
                  .subject(user.getId().toString())
                  .issuedAt(Date.from(Instant.now()))
                  .expiration(Date.from(Instant.now().plusMillis(refreshTokenExpiration)))
                  .issuer(issuer)
                  .signWith(secretKey)
                  .compact();
   }

   public Jws<Claims> parseClaims(String token){
         return Jwts.parser()
                 .verifyWith(secretKey)
                 .build()
                 .parseSignedClaims(token) ;
   }


   // is accessToken
    public Boolean isAccessToken(String token){
       Claims claims =  parseClaims(token).getPayload() ;
       return claims.get("typ").equals("access");
    }

    // is refreshToken
    public Boolean isRefreshToken(String token){
        Claims claims =  parseClaims(token).getPayload() ;
        return claims.get("typ").equals("refresh");
    }

    public Boolean isJwtValid(String token){
        Claims claims =  parseClaims(token).getPayload() ;
        return claims.getExpiration().after(Date.from(Instant.now())) ;
    }

    public String getRole(String token){
        Claims claims  = parseClaims(token).getPayload() ;
        return  claims.get("role").toString() ;
    }



}
