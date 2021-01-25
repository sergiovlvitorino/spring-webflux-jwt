package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Collection;
import java.util.Date;

@Service
public class JWTService {

    private String secret = "B7E1919472B4E2F6F7FEC38298B47AAD21B957FAC1DB2A3645E4B62BDEBED68F47C12A43BDFB17EA5FDFEE99CC84A49615BB6A55E5CC476E3C5C";
    private String expirationTime = "30000000";
    public static final String TOKEN_PREFIX = "Bearer";

    public String extractUsername(String authToken) {
        return getClaimsFromToken(authToken)
                .getSubject();
    }

    public Claims getClaimsFromToken(String authToken) {
        final var key = Base64
                .getEncoder()
                .encodeToString(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(authToken)
                .getBody();
    }

    public boolean validateToken(final String authToken) {
        return getClaimsFromToken(authToken)
                .getExpiration()
                .after(new Date());
    }


    public String generateToken(final UserDetails user) {
        final var claims = Jwts.claims();
        claims.put("authorities", extractAuthorities(user.getAuthorities()));
        claims.put("Username", user.getUsername());

        final long expirationSeconds = Long.parseLong(expirationTime);
        final Date creationDate = new Date();
        final Date expirationDate = new Date(creationDate.getTime() + expirationSeconds * 1000);

        return TOKEN_PREFIX + " " + Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(creationDate)
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    private String extractAuthorities(Collection<? extends GrantedAuthority> authorities) {
        final var stringBuilder = new StringBuilder();
        authorities.stream().forEach(authority -> stringBuilder.append(authority + ","));
        return removeComma(stringBuilder.toString());
    }

    public String removeComma(final String text) {
        if (text.isEmpty()) {
            return text;
        } else {
            return text.substring(0, text.length() - 1);
        }
    }
}
