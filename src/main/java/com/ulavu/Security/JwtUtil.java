package com.ulavu.Security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    // No insecure default here on purpose: if jwt.secret is missing or weak,
    // the app must fail to start rather than silently sign tokens with a
    // publicly-known value.
    @Value("${jwt.secret}")
    private String SECRET;

    private static final int MIN_SECRET_LENGTH = 32;

    private Key key;

    private final long expiration = 1000 * 60 * 60;

    @PostConstruct
    public void init() {
        if (SECRET == null || SECRET.isBlank() || SECRET.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                "jwt.secret is missing or too short. Configure a random secret of at least "
                + MIN_SECRET_LENGTH + " characters via the JWT_SECRET environment variable.");
        }
        this.key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String generateToken(String username, Integer roleId, String emailId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roleId", roleId)
                .claim("emailId", emailId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token){
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token, String username) {
        String extract = extractUsername(token);
        return extract.equals(username) && isTokenValid(token);
    }

    public boolean isTokenValid(String token) {
        return getClaims(token).getExpiration().after(new Date());
    }

    public Claims getClaims(String token){

        return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
    }

}
