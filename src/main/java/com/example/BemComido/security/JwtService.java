package com.example.BemComido.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        byte[] raw;
        try {
            raw = Decoders.BASE64.decode(secret);
        } catch (Exception ignored) {
            raw = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (raw.length < 32) {
            try {
                raw = MessageDigest.getInstance("SHA-256").digest(raw);
            } catch (Exception e) {
                byte[] padded = new byte[32];
                System.arraycopy(raw, 0, padded, 0, Math.min(raw.length, 32));
                raw = padded;
            }
        }
        this.key = Keys.hmacShaKeyFor(raw);
    }

    public String generateToken(String subject, Map<String, Object> claims, long expirationMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    // Adicione métodos para uso no filtro
    public String extractUsername(String token) {
        try {
            return parse(token).getBody().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenValid(String token, String username) {
        try {
            Claims claims = parse(token).getBody();
            return claims.getSubject().equals(username) && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
