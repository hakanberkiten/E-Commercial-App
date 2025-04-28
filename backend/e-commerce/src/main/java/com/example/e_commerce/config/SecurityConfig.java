// src/main/java/com/example/e_commerce/config/SecurityConfig.java
package com.example.e_commerce.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        // open signup & login
        .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
        // open public GET products
        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
        // everything else needs auth
        .anyRequest().authenticated());
      
      
    return http.build();
  }
}
