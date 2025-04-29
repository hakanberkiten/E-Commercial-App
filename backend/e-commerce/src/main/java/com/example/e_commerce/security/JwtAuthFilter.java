package com.example.e_commerce.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.e_commerce.service.impl.CustomUserDetailsService;

import java.io.IOException;

/**
 * Bu filtre:
 * 1. Authorization header’daki "Bearer <token>" bilgisini okur.
 * 2. JwtUtils ile token’ın geçerli olup olmadığını kontrol eder.
 * 3. Geçerliyse token’dan çıkarılan username ile
 *    CustomUserDetailsService.loadUserByUsername(...) çağrılır.
 * 4. UserDetails’ı Spring Security context’e set eder.
 * 5. Sonra isteği devam ettirir.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest  request,
            HttpServletResponse response,
            FilterChain         filterChain
    ) throws ServletException, IOException {

        // 1️⃣ Header’dan token’ı çek
        String authHeader = request.getHeader("Authorization");
        String token      = null;
        String username   = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token    = authHeader.substring(7);       // “Bearer ” kısmını at
            try {
                username = jwtUtils.getUsername(token); // Token’dan subject (=email) al
            } catch (JwtException ex) {
                // Token geçersiz veya süresi dolmuş
                logger.warn("JWT validation failed: " + ex.getMessage());
            }
        }

        // 2️⃣ Eğer token geçerliyse ve henüz authenticate edilmediyse...
        if (username != null &&
            SecurityContextHolder.getContext().getAuthentication() == null &&
            jwtUtils.validateToken(token)
        ) {
            // 3️⃣ Gerçek UserDetails’ı DB’den (veya cache’den) yükle
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 4️⃣ Spring’e “bu istek, bu user tarafından geliyor” de
            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,           // principal
                    null,                  // credentials zaten token’la doğrulandı
                    userDetails.getAuthorities()
            );
            authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // 5️⃣ İsteği zincirde devam ettir
        filterChain.doFilter(request, response);
    }
}
