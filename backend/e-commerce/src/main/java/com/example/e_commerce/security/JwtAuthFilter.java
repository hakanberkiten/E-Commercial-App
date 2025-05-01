package com.example.e_commerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
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
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip token validation for OPTIONS requests (CORS preflight)
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String token = extractToken(request);
            if (token != null && jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsername(token);
                Integer tokenRoleId = jwtUtils.getUserRole(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                User currentUser = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                
                // Check if user is still active
                if (!currentUser.getActive()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Account has been deactivated\"}");
                    return;
                }
                
                Integer currentRoleId = currentUser.getRole().getRoleId();
                
                // Log role comparison information
                logger.info("User {} - Token role ID: {}, Current DB role ID: {}", 
                        username, tokenRoleId, currentRoleId);
                
                // Check if roles match
                boolean rolesMatch = tokenRoleId.equals(currentRoleId);
                
                // Only validate the token if the role hasn't changed
                if (rolesMatch) {
                    // Authenticate the user
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Log the role mismatch
                    logger.warn("User {} has different role in token ({}) vs database ({})", 
                            username, tokenRoleId, currentRoleId);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"User role has changed\"}");
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } catch (DisabledException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Account has been deactivated\"}");
        } catch (Exception e) {
            logger.error("Authentication error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
