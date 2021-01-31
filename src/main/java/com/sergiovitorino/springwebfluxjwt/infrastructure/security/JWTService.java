package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.Instant;
import java.time.Period;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

@Service
public class JWTService implements Serializable {

    private String secret = "B7E1919472B4E2F6F7FEC38298B47AAD21B957FAC1DB2A3645E4B62BDEBED68F47C12A43BDFB17EA5FDFEE99CC84A49615BB6A55E5CC476E3C5C";
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

    public boolean isValid(Claims claims){
        return claims.getExpiration().after(Date.from(Instant.now()));
    }

    public boolean validateToken(final String authToken) {
        return authToken != null && getClaimsFromToken(authToken)
                .getExpiration()
                .after(Date.from(Instant.now()));
    }


    public String generateToken(final UserDetails user) {
        final var claims = Jwts.claims();
        claims.put("authorities", extractAuthorities(user.getAuthorities()));
        claims.put("Username", user.getUsername());
        final var instantNow = Instant.now();
        return TOKEN_PREFIX + " " + Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(Date.from(instantNow))
                .setExpiration(Date.from(instantNow.plus(Period.ofDays(10))))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    private String extractAuthorities(final Collection<? extends GrantedAuthority> authorities) {
        final var authoritiesString = authorities.toString();
        return authoritiesString
                .substring(1, authoritiesString.length() - 1)
                .replaceAll(" ", "");
    }

}
