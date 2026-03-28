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
import java.time.Instant;
import java.time.Period;
import java.util.*;

@Service
public class JWTService {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${jwt.secret}")
    private String secret;
    public static final String TOKEN_PREFIX = "Bearer";

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
        if (!expectedSignature.equals(parts[2])) {
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
        return Instant.ofEpochSecond(exp).isAfter(Instant.now());
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
        var exp = now.plus(Period.ofDays(10));

        var claims = new LinkedHashMap<String, Object>();
        claims.put("sub", user.getUsername());
        claims.put("authorities", extractAuthorities(user.getAuthorities()));
        claims.put("Username", user.getUsername());
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", exp.getEpochSecond());

        var header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        try {
            var payloadBytes = MAPPER.writeValueAsBytes(claims);
            var payload = base64Url(payloadBytes);
            var signature = sign(header + "." + payload);
            return TOKEN_PREFIX + " " + header + "." + payload + "." + signature;
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
