package com.example.e_commerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Header'dan token bilgisini al
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Token'ı ayıkla
        final String jwt = authHeader.substring(7);
        
        if (jwtUtils.validateToken(jwt)) {
            // Token geçerliyse kullanıcı bilgilerini al
            String username = jwtUtils.getUsername(jwt);
            Integer roleId = jwtUtils.getUserRole(jwt);
            
            // Kullanıcıyı yükle
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Rol yetkisini oluştur (ROLE_ADMIN, ROLE_SELLER, ROLE_CUSTOMER)
            String roleName = getRoleName(roleId);
            
            // Kimlik bilgisini oluştur (kullanıcı adı, şifre, yetkiler)
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, 
                    List.of(new SimpleGrantedAuthority(roleName))
                );
            
            // Güvenlik bağlamına kimliği kaydet
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getRoleName(Integer roleId) {
        switch (roleId) {
            case 1: return "ROLE_ADMIN";
            case 2: return "ROLE_SELLER";
            case 3: return "ROLE_CUSTOMER";
            default: return "ROLE_USER";
        }
    }
}