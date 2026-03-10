package com.company.Bill_Bridge.configuration;


import com.company.Bill_Bridge.filter.JWTFilter;
import com.company.Bill_Bridge.service.CustomUserDetailService;
import com.company.Bill_Bridge.service.Oauth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Configuration
@EnableWebSecurity
public class Config {

     private final CustomUserDetailService userDetailService ;


     private final JWTFilter jwtFilter ;

     private final Oauth2SuccessHandler oauth2SuccessHandler ;
     private final String frontendURL ;

     private final String paytm_base_url ;


    public Config(CustomUserDetailService userDetailService,
                  JWTFilter jwtFilter, Oauth2SuccessHandler oauth2SuccessHandler,
                  @Value("${frontend.url}")String frontendURL,@Value("${paytm.base-url}") String paytmBaseUrl) {
        this.userDetailService = userDetailService;
        this.jwtFilter = jwtFilter;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.frontendURL = frontendURL ;
        paytm_base_url = paytmBaseUrl;
    }


    @Bean
     public SecurityFilterChain securityFilterChain(HttpSecurity http) {


          http.csrf(AbstractHttpConfigurer::disable)
                  .authorizeHttpRequests( (request) ->
                              request.requestMatchers("/" , "/register",
                                              "/login" , "/user/create/**" , "/index.html" ,"/h2/**","/refresh" ,"/logout",
                                              "/billbuddy/**"
                                             ).permitAll()
                                      .requestMatchers("/merchant/**").hasRole("MERCHANT")
                                      .requestMatchers("/user/**" , "/dashboard.html" , "/ws/**" ).hasAnyRole("CUSTOMER" , "MERCHANT")
                                      .anyRequest().authenticated())
                  .sessionManagement(session ->
                          session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                  .cors(Customizer.withDefaults())
                  .authenticationProvider(provider())
                  .exceptionHandling(ex -> ex
                          .authenticationEntryPoint((request, response, authException) -> {
                              response.setContentType("application/json");
                              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                              response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + authException.getMessage() + "\"}");
                          })
                  )
                  .oauth2Login(oauth2 -> {
                    oauth2.successHandler(oauth2SuccessHandler)
                          .failureHandler(null) ; 
                  })
                  .logout(AbstractHttpConfigurer::disable)
                  .addFilterBefore(jwtFilter , UsernamePasswordAuthenticationFilter.class)
          ;
//                  .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
//                  .formLogin( form -> form.loginPage("/login.html").loginProcessingUrl("/do-login").defaultSuccessUrl("/dashboard.html" , true).permitAll()) ;

          return http.build() ;
     }



     @Bean
     public RestClient restClient (){
          return  RestClient.builder()
                  .baseUrl(paytm_base_url)
                  .build() ;
     }

     @Bean
     public AuthenticationProvider provider(){
          DaoAuthenticationProvider daoAuthenticationProvider
                  = new DaoAuthenticationProvider(userDetailService) ;
          daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder(12));
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
          configuration.setAllowedOrigins(List.of(frontendURL)); // For production, restrict to your frontends
          configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
          configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With" , "X-REFRESH-TOKEN"));
          configuration.setExposedHeaders(List.of("Authorization"));
          configuration.setAllowCredentials(true);
          configuration.setMaxAge(3600L);

          UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
          source.registerCorsConfiguration("/**", configuration);
          return source;
     }




}
