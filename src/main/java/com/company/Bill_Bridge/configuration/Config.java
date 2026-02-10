package com.company.Bill_Bridge.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class Config {

     @Bean
     public SecurityFilterChain securityFilterChain(HttpSecurity http) {


          http.csrf(AbstractHttpConfigurer::disable)
                  .authorizeHttpRequests( (request) ->
                              request.requestMatchers("/register.html",
                                              "/login.html" , "/user/create/**" , "/index.html" ,"/h2/**").permitAll()
                                      .requestMatchers("/merchant/**").hasRole("MERCHANT")
                                      .requestMatchers("/user/**" , "/dashboard.html" , "/ws/**").hasAnyRole("CUSTOMER" , "MERCHANT")
                                      .anyRequest().authenticated())
                  .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                  .formLogin( form -> form.loginPage("/login.html").loginProcessingUrl("/do-login").defaultSuccessUrl("/dashboard.html" , true).permitAll()) ;

          return http.build() ;
     }

     @Bean
     public BCryptPasswordEncoder encoder(){
          return new BCryptPasswordEncoder(12) ;
     }
}
