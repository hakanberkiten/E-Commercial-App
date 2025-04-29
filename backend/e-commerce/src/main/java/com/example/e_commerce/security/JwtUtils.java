// JwtUtils.java
package com.example.e_commerce.security;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtils {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long validityMs = 3600_000; // 1 saat
    private final UserRepository userRepository;

    public String generateToken(String username) {
        // Kullanıcıyı veritabanından al
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Claims (token içeriği) oluştur
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getRoleId());
        claims.put("userId", user.getUserId());

        Date now = new Date();
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + validityMs))
            .signWith(key)
            .compact();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Integer getUserRole(String token) {
        return getClaims(token).get("role", Integer.class);
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
}