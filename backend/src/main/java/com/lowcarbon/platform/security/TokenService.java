package com.lowcarbon.platform.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final String TOKEN_TYPE = "JWT";
    private static final String ALGORITHM = "HS256";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final SecretKeySpec signingKey;
    private final long ttlSeconds;
    private final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();

    public TokenService(ObjectMapper objectMapper,
                        @Value("${app.auth.jwt-secret:lowcarbon-dev-secret-change-me-please-2026}") String secret,
                        @Value("${app.auth.jwt-ttl-seconds:86400}") long ttlSeconds) {
        this.objectMapper = objectMapper;
        this.signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        this.ttlSeconds = ttlSeconds;
    }

    public String createToken(Long userId) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + ttlSeconds;

        Map<String, Object> header = new HashMap<>();
        header.put("typ", TOKEN_TYPE);
        header.put("alg", ALGORITHM);

        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", String.valueOf(userId));
        payload.put("iat", issuedAt);
        payload.put("exp", expiresAt);
        payload.put("jti", UUID.randomUUID().toString());

        String encodedHeader = encode(header);
        String encodedPayload = encode(payload);
        String signingInput = encodedHeader + "." + encodedPayload;
        return signingInput + "." + sign(signingInput);
    }

    public Long resolveUserId(String token) {
        cleanupRevokedTokens();
        if (!StringUtils.hasText(token) || revokedTokens.containsKey(token)) {
            return null;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String signingInput = parts[0] + "." + parts[1];
            byte[] expectedSignature = sign(signingInput).getBytes(StandardCharsets.UTF_8);
            byte[] actualSignature = parts[2].getBytes(StandardCharsets.UTF_8);
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                return null;
            }

            Map<String, Object> payload = decode(parts[1]);
            long expiresAt = getLong(payload.get("exp"));
            if (Instant.now().getEpochSecond() >= expiresAt) {
                return null;
            }

            return Long.parseLong(String.valueOf(payload.get("sub")));
        } catch (Exception ex) {
            return null;
        }
    }

    public void invalidate(String token) {
        Long expiresAt = resolveExpiration(token);
        if (expiresAt == null) {
            return;
        }
        revokedTokens.put(token, expiresAt);
        cleanupRevokedTokens();
    }

    public String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7).trim();
    }

    private Long resolveExpiration(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            Map<String, Object> payload = decode(parts[1]);
            return getLong(payload.get("exp"));
        } catch (Exception ex) {
            return null;
        }
    }

    private String encode(Map<String, Object> source) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(source);
            return URL_ENCODER.encodeToString(json);
        } catch (Exception ex) {
            throw new IllegalStateException("Token encoding failed", ex);
        }
    }

    private Map<String, Object> decode(String encoded) {
        try {
            byte[] json = URL_DECODER.decode(encoded);
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Token decoding failed", ex);
        }
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(signingKey);
            byte[] signature = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return URL_ENCODER.encodeToString(signature);
        } catch (Exception ex) {
            throw new IllegalStateException("Token signing failed", ex);
        }
    }

    private long getLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private void cleanupRevokedTokens() {
        long now = Instant.now().getEpochSecond();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue() <= now);
    }
}
