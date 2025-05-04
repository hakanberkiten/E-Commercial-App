package com.example.e_commerce.config;

import com.example.e_commerce.security.JwtAuthFilter;
import com.example.e_commerce.security.oauth2.OAuth2LoginSuccessHandler;
import com.example.e_commerce.security.oauth2.CustomOAuth2UserService;
import com.example.e_commerce.service.impl.CustomUserDetailsService;

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
    private final CustomUserDetailsService userDetailsService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService; // Inject the service directly
    
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
                
                // OAuth2 endpoints
                .requestMatchers("/login/**", "/oauth2/**").permitAll()
                
                // Current user endpoint - authenticated but no specific role needed
                .requestMatchers("/api/auth/user/current").authenticated()
                
                // Public GET requests for products, categories, reviews
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/reviews/**").permitAll()
                
                // Allow public access to admin contacts
                .requestMatchers("/api/users/admin-contacts").permitAll()
                
                // Make address endpoints more permissive - use wildcards for all address operations
                .requestMatchers("/api/addresses/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users/{id}/addresses").authenticated()
                
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}/addresses/default").authenticated()
                .requestMatchers("/api/user-addresses/**").authenticated()
                
                // Allow users to update their own profile and change password
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/change-password").authenticated()
                
                // Notification endpoints
                .requestMatchers(HttpMethod.GET, "/api/notifications/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/notifications/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/notifications/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/notifications/**").authenticated()
                
                // Admin endpoints - these come after the more specific rules above
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/roles/**").hasAuthority("ROLE_ADMIN")   // Role management
                .requestMatchers(HttpMethod.GET, "/api/orders/all").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER")
                .requestMatchers(HttpMethod.PATCH, "/api/orders/**/status").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER")
                .requestMatchers(HttpMethod.POST, "/api/orders/*/status-update").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER")
                
                // Seller endpoints
                .requestMatchers(HttpMethod.POST, "/api/products/save").hasAuthority("ROLE_SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyAuthority("ROLE_SELLER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/products/seller/**").hasAuthority("ROLE_SELLER")
                .requestMatchers(HttpMethod.GET, "/api/orders/seller/**").hasAuthority("ROLE_SELLER")

                // Customer endpoints (allowing both ROLE_CUSTOMER and ROLE_SELLER)
                .requestMatchers("/api/cart/**").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_SELLER")
                .requestMatchers(HttpMethod.POST, "/api/orders/place").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_SELLER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/orders/{id}").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_SELLER")
                .requestMatchers("/api/reviews/save").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_SELLER")

                // Payment endpoints with explicit role permissions
                .requestMatchers("/api/payments/stripe/customers/**").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_SELLER", "ROLE_ADMIN")
                .requestMatchers("/api/payments/process").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_SELLER", "ROLE_ADMIN")
                .requestMatchers("/api/payments/user/**").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_SELLER", "ROLE_ADMIN")

                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                
                // Add this line to debug specific request permissions
                .requestMatchers(request -> {
                    System.out.println("Checking request: " + request.getRequestURI());
                    return false;
                }).denyAll()

                // Fallback: Any other authenticated request
                .anyRequest().authenticated()
            )
            
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Configure OAuth2 login
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(oAuth2LoginSuccessHandler)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService) // Use the injected service directly
                )
            )
            
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            
        return http.build();
    }
}