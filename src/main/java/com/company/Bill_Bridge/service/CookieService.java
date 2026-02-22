package com.company.Bill_Bridge.service;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {
    
    @Value("${security.jwt.refresh-token-cookie-name}")
    String refreshTokenCookieName ;
    
    @Value("${security.jwt.cookie-http-only}")
    Boolean cookieHttpOnly ;
    
    @Value("${security.jwt.cookie-secure}")
    Boolean cookieSecure ;
    
    @Value("${security.jwt.cookie-domain}")
    String cookieDomain ;
    
    @Value("${security.jwt.cookie-same-site}")
    String cookieSameSite ;

    // attach refresh token to cookie

    public void attachRefreshCookie(HttpServletResponse response ,
                                     String refreshTokenValue , int maxAge){
        ResponseCookie.ResponseCookieBuilder

                cookieBuilder = ResponseCookie.from(refreshTokenCookieName , refreshTokenValue )
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .domain(cookieDomain)
                .maxAge(maxAge)
                .path("/") ;

        if(cookieDomain!=null && !cookieDomain.isBlank())
        {
            cookieBuilder.domain(cookieDomain);
        }

        ResponseCookie cookie = cookieBuilder.build() ;

        response.addHeader(HttpHeaders.SET_COOKIE , cookie.toString());
    }


    // add no store headers
    public void addNoStoreHeaders(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL , "no-store");
        response.setHeader("Pragma"  , "no-cache");
    }


    // clear refresh token from cookie
    public void clearRefreshCookie(HttpServletResponse response) {

        var builder = ResponseCookie.from(refreshTokenCookieName , "")
                .maxAge(0)
                .httpOnly(cookieHttpOnly)
                .path("/")
                .sameSite(cookieSameSite)
                .secure(cookieSecure);

        if(cookieDomain!=null && !cookieDomain.isBlank())
        {
            builder.domain(cookieDomain);

        }


        ResponseCookie responseCookie = builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }



}
