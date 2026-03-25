package com.sergiovitorino.springwebfluxjwt.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JWTServiceTest {

    private JWTService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JWTService();
        Field secretField = JWTService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, "TEST_ONLY_SECRET_DO_NOT_USE_IN_PRODUCTION_12345678901234567890123456789012345678901234567890");
    }

    private String generateValidToken() {
        var userDetails = new User("test@email.com", "password",
                AuthorityUtils.createAuthorityList("ROLE_USER", "SAVE_USER"));
        return jwtService.generateToken(userDetails).substring("Bearer ".length());
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        var token = generateValidToken();
        assertEquals("test@email.com", jwtService.extractUsername(token));
    }

    @Test
    void testValidateTokenReturnsTrue() {
        var token = generateValidToken();
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testValidateTokenReturnsFalseForNull() {
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    void testValidateTokenReturnsFalseForInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.here"));
    }

    @Test
    void testGetClaimsFromTokenReturnsValidClaims() {
        var token = generateValidToken();
        Map<String, Object> claims = jwtService.getClaimsFromToken(token);

        assertEquals("test@email.com", claims.get("sub"));
        assertEquals("test@email.com", claims.get("Username"));
        assertNotNull(claims.get("authorities"));
        assertNotNull(claims.get("iat"));
        assertNotNull(claims.get("exp"));
    }

    @Test
    void testIsValidReturnsTrueForValidClaims() {
        var token = generateValidToken();
        var claims = jwtService.getClaimsFromToken(token);
        assertTrue(jwtService.isValid(claims));
    }

    @Test
    void testGetClaimsFromTokenThrowsForInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> jwtService.getClaimsFromToken("not-a-jwt"));
    }

    @Test
    void testGetClaimsFromTokenThrowsForTamperedSignature() {
        var token = generateValidToken();
        var tampered = token.substring(0, token.lastIndexOf('.')) + ".invalidsignature";
        assertThrows(SecurityException.class, () -> jwtService.getClaimsFromToken(tampered));
    }

    @Test
    void testGetClaimsFromTokenThrowsForTamperedPayload() {
        var token = generateValidToken();
        var parts = token.split("\\.");
        var tampered = parts[0] + ".dGFtcGVyZWQ" + "." + parts[2];
        assertThrows(SecurityException.class, () -> jwtService.getClaimsFromToken(tampered));
    }

    @Test
    void testGenerateTokenContainsBearerPrefix() {
        var userDetails = new User("test@email.com", "password",
                AuthorityUtils.createAuthorityList("ROLE_USER"));
        var token = jwtService.generateToken(userDetails);
        assertTrue(token.startsWith("Bearer "));
    }
}
