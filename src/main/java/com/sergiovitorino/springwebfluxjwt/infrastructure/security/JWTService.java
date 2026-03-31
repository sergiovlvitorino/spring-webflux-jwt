package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class JWTService {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration TOKEN_EXPIRATION = Duration.ofHours(1);
    private static final String ISSUER = "spring-webflux-jwt";

    @Value("${jwt.secret}")
    private String secret;

    private SecretKeySpec cachedKeySpec;

    @PostConstruct
    void init() {
        cachedKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
    }

    public String extractUsername(String authToken) {
        return getClaimsFromToken(authToken).get("sub").toString();
    }

    public Map<String, Object> getClaimsFromToken(String authToken) {
        var parts = authToken.split("\\.", 4);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        var expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Invalid JWT signature");
        }

        try {
            var decoded = Base64.getUrlDecoder().decode(parts[1]);
            @SuppressWarnings("unchecked")
            var claims = MAPPER.readValue(decoded, Map.class);
            return claims;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT payload", e);
        }
    }

    public boolean isValid(Map<String, Object> claims) {
        var exp = ((Number) claims.get("exp")).longValue();
        if (Instant.ofEpochSecond(exp).isBefore(Instant.now())) {
            return false;
        }
        var iss = claims.get("iss");
        return iss != null && ISSUER.equals(iss.toString());
    }

    public boolean validateToken(final String authToken) {
        if (authToken == null) return false;
        try {
            var claims = getClaimsFromToken(authToken);
            return isValid(claims);
        } catch (Exception e) {
            return false;
        }
    }

    public String generateToken(final UserDetails user) {
        var now = Instant.now();
        var exp = now.plus(TOKEN_EXPIRATION);

        var claims = new LinkedHashMap<String, Object>();
        claims.put("sub", user.getUsername());
        claims.put("iss", ISSUER);
        claims.put("authorities", extractAuthorities(user.getAuthorities()));
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", exp.getEpochSecond());

        var header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        try {
            var payloadBytes = MAPPER.writeValueAsBytes(claims);
            var payload = base64Url(payloadBytes);
            var signature = sign(header + "." + payload);
            return header + "." + payload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    private String extractAuthorities(final Collection<? extends GrantedAuthority> authorities) {
        var sb = new StringBuilder();
        boolean first = true;
        for (GrantedAuthority auth : authorities) {
            if (!first) sb.append(',');
            sb.append(auth.getAuthority());
            first = false;
        }
        return sb.toString();
    }

    private String sign(String data) {
        try {
            var mac = Mac.getInstance(HMAC_ALGO);
            mac.init(cachedKeySpec);
            var hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64Url(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    private String base64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
