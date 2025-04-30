package com.example.e_commerce.config;

import com.example.e_commerce.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public authentication endpoints
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/signup").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
                
                // Public GET requests for products, categories, reviews
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/reviews/**").permitAll()

                // Admin endpoints
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/roles/**").hasAuthority("ROLE_ADMIN")   // Role management
                .requestMatchers(HttpMethod.GET, "/api/orders/all").hasAuthority("ROLE_ADMIN") 
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("ROLE_ADMIN") // Admin can delete any product
                .requestMatchers(HttpMethod.PATCH, "/api/orders/**/status").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER") // Admin and Seller can update status
                
                // Password change should be allowed for all authenticated users
                .requestMatchers(HttpMethod.PUT, "/api/users/change-password").authenticated()

                // Seller endpoints
                .requestMatchers(HttpMethod.POST, "/api/products/save").hasAuthority("ROLE_SELLER") // Seller creates product
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("ROLE_SELLER") // Seller updates their product
                .requestMatchers(HttpMethod.GET, "/api/products/seller/**").hasAuthority("ROLE_SELLER") // Seller views their products
                .requestMatchers(HttpMethod.GET, "/api/orders/seller/**").hasAuthority("ROLE_SELLER") // Seller views their orders

                // Customer endpoints
                .requestMatchers("/api/cart/**").hasAuthority("ROLE_CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/orders/place").hasAuthority("ROLE_CUSTOMER") // Placing an order
                .requestMatchers(HttpMethod.GET, "/api/orders/{id}").hasAuthority("ROLE_CUSTOMER") // Customer views their own order
                .requestMatchers("/api/reviews/save").hasAuthority("ROLE_CUSTOMER") // Customer saves a review
                .requestMatchers("/api/addresses/**").hasAuthority("ROLE_CUSTOMER") // Customer manages addresses
                .requestMatchers("/api/user-addresses/**").hasAuthority("ROLE_CUSTOMER") // Customer manages addresses

                // Fallback: Any other authenticated request
                .anyRequest().authenticated()
            )
            
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            
        return http.build();
    }
}