package com.example.e_commerce.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.entity.User;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtils.getUsername(token);
            } catch (JwtException ex) {
                logger.warn("JWT validation failed: " + ex.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Get token role ID and current role ID for comparison
            Integer tokenRoleId = jwtUtils.getUserRole(token);
            
            // Load current user from database to get current role
            User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Integer currentRoleId = currentUser.getRole().getRoleId();
            
            // Log role comparison information
            logger.info("User {} - Token role ID: {}, Current DB role ID: {}", 
                    username, tokenRoleId, currentRoleId);
            
            // Check if roles match
            boolean rolesMatch = tokenRoleId.equals(currentRoleId);
            
            // Only validate the token if the role hasn't changed
            if (jwtUtils.validateToken(token) && rolesMatch) {
                // Authenticate the user
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else if (!rolesMatch) {
                // If role is different, add header to signal token invalidation
                logger.warn("User {} role changed from {} to {}, token invalidated", 
                        username, tokenRoleId, currentRoleId);
                response.setHeader("X-Token-Invalid", "true");
                response.setHeader("X-Token-Invalid-Reason", "role-changed");
            }
        }

        filterChain.doFilter(request, response);
    }
}
