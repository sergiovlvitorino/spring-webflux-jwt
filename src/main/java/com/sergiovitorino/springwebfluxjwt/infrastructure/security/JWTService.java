package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.Period;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JWTService implements Serializable {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${jwt.secret}")
    private String secret;
    public static final String TOKEN_PREFIX = "Bearer";

    public String extractUsername(String authToken) {
        return getClaimsFromToken(authToken).get("sub").toString();
    }

    public Map<String, Object> getClaimsFromToken(String authToken) {
        var parts = authToken.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        var payload = parts[1];
        var signature = parts[2];

        var expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!expectedSignature.equals(signature)) {
            throw new SecurityException("Invalid JWT signature");
        }

        try {
            var decoded = Base64.getUrlDecoder().decode(payload);
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
        var authoritiesString = authorities.toString();
        return authoritiesString
                .substring(1, authoritiesString.length() - 1)
                .replaceAll(" ", "");
    }

    private String sign(String data) {
        try {
            var mac = Mac.getInstance(HMAC_ALGO);
            var keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
            mac.init(keySpec);
            var hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64Url(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    private String base64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
