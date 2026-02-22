package com.company.Bill_Bridge.configuration;


import com.company.Bill_Bridge.filter.JWTFilter;
import com.company.Bill_Bridge.service.CustomUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestClient;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class Config {

     @Autowired
     private CustomUserDetailService userDetailService ;

     @Autowired
     private JWTFilter jwtFilter ;



     @Bean
     public SecurityFilterChain securityFilterChain(HttpSecurity http) {


          http.csrf(AbstractHttpConfigurer::disable)
                  .authorizeHttpRequests( (request) ->
                              request.requestMatchers("/register",
                                              "/login" , "/user/create/**" , "/index.html" ,"/h2/**","/refresh" ,"/logout"
                                             ).permitAll()
                                      .requestMatchers("/merchant/**").hasRole("MERCHANT")
                                      .requestMatchers("/user/**" , "/dashboard.html" , "/ws/**" ).hasAnyRole("CUSTOMER" , "MERCHANT")
                                      .anyRequest().authenticated())
                  .sessionManagement(session ->
                          session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                  .cors(Customizer.withDefaults())
                  .authenticationProvider(provider())
                  .addFilterBefore(jwtFilter , UsernamePasswordAuthenticationFilter.class)
                  .logout(AbstractHttpConfigurer::disable)
          ;
//                  .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
//                  .formLogin( form -> form.loginPage("/login.html").loginProcessingUrl("/do-login").defaultSuccessUrl("/dashboard.html" , true).permitAll()) ;

          return http.build() ;
     }

     @Bean
     public BCryptPasswordEncoder encoder(){
          return new BCryptPasswordEncoder(12) ;
     }

     @Bean
     public RestClient restClient (){
          return  RestClient.builder()
                  .baseUrl("https://securestage.paytmpayments.com")
                  .build() ;
     }

     @Bean
     public AuthenticationProvider provider(){
          DaoAuthenticationProvider daoAuthenticationProvider
                  = new DaoAuthenticationProvider(userDetailService) ;
          daoAuthenticationProvider.setPasswordEncoder(encoder());
          return daoAuthenticationProvider ;
     }
     @Bean
     public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
          return config.getAuthenticationManager() ;
     }

     @Bean
     public CorsConfigurationSource corsConfigurationSource(
     ) {
          CorsConfiguration configuration = new CorsConfiguration();
          configuration.setAllowedOrigins(List.of("*")); // For production, restrict to your frontends
          configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
          configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
          configuration.setExposedHeaders(List.of("Authorization"));
          configuration.setAllowCredentials(true);
          configuration.setMaxAge(3600L);

          UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
          source.registerCorsConfiguration("/**", configuration);
          return source;
     }




}
