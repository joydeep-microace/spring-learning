package com.example.spring_learning.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    public static final String COOKIE_NAME = "SCHOOL_JWT";

    private final String secret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret:change-this-secret-for-real-projects}") String secret,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds
    ) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(AuthenticatedUser user) {
        try {
            String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payloadJson = "{"
                    + "\"sub\":\"" + escapeJson(user.userId()) + "\","
                    + "\"email\":\"" + escapeJson(user.email()) + "\","
                    + "\"name\":\"" + escapeJson(user.displayName()) + "\","
                    + "\"role\":\"" + user.role().name() + "\","
                    + "\"exp\":" + (Instant.now().getEpochSecond() + expirationSeconds)
                    + "}";
            String unsignedToken = base64Url(headerJson.getBytes(StandardCharsets.UTF_8))
                    + "."
                    + base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));

            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create JWT", exception);
        }
    }

    public AuthenticatedUser verifyToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("JWT must have header, payload, and signature");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new IllegalArgumentException("JWT signature is invalid");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, String> payload = parseFlatJson(payloadJson);

            long expiresAt = Long.parseLong(payload.get("exp"));
            if (Instant.now().getEpochSecond() > expiresAt) {
                throw new IllegalArgumentException("JWT has expired");
            }

            return new AuthenticatedUser(
                    payload.get("sub").toString(),
                    payload.get("email").toString(),
                    payload.get("name").toString(),
                    Role.valueOf(payload.get("role").toString())
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid JWT: " + exception.getMessage(), exception);
        }
    }

    public String cookieValue(String token) {
        /*
         * HttpOnly means JavaScript cannot read the cookie.
         * SameSite=Strict means browsers do not send it on cross-site requests.
         * Postman can still send it using this header:
         * Cookie: SCHOOL_JWT=<token>
         */
        return COOKIE_NAME + "=" + token + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=" + expirationSeconds;
    }

    private String sign(String unsignedToken) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return base64Url(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    /*
     * This parser is intentionally small because our JWT payload is a simple,
     * flat JSON object created by this same class. For production APIs, use a
     * mature JWT library instead of maintaining JWT code yourself.
     */
    private Map<String, String> parseFlatJson(String json) {
        Map<String, String> result = new HashMap<>();
        String content = json.trim();
        if (content.startsWith("{")) {
            content = content.substring(1);
        }
        if (content.endsWith("}")) {
            content = content.substring(0, content.length() - 1);
        }

        for (String pair : content.split(",")) {
            String[] keyAndValue = pair.split(":", 2);
            String key = unquote(keyAndValue[0].trim());
            String value = unquote(keyAndValue[1].trim());
            result.put(key, value);
        }
        return result;
    }

    private String unquote(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private boolean constantTimeEquals(String first, String second) {
        if (first.length() != second.length()) {
            return false;
        }

        int result = 0;
        for (int index = 0; index < first.length(); index++) {
            result |= first.charAt(index) ^ second.charAt(index);
        }
        return result == 0;
    }
}
