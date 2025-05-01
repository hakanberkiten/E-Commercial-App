// JwtUtils.java
package com.example.e_commerce.security;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtils {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long validityMs = 3600_000; // 1 saat
    private final UserRepository userRepository;

    public String generateToken(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole().getRoleId());
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + validityMs))
            .signWith(key)
            .compact();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Integer getUserRole(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", Integer.class);
    }

    public Long getUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaims(token);
        
        // Check how roles are stored in your token
        // Modify this based on your token structure
        String roleName = claims.get("roleName", String.class);
        if (roleName != null) {
            // Make sure roles have the ROLE_ prefix required by Spring Security
            String roleWithPrefix = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            return Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix));
        }
        
        return Collections.emptyList();
    }
}