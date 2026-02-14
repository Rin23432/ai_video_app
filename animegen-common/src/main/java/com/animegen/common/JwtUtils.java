package com.animegen.common;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public final class JwtUtils {
    private JwtUtils() {
    }

    public static String issueToken(String userId, String secret, long ttlSeconds) {
        long exp = Instant.now().getEpochSecond() + ttlSeconds;
        String payload = userId + "." + exp;
        String sig = hmacSha256(payload, secret);
        return Base64.getUrlEncoder().withoutPadding().encodeToString((payload + "." + sig).getBytes(StandardCharsets.UTF_8));
    }

    public static String verifyAndGetUserId(String token, String secret) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(token);
            String raw = new String(decoded, StandardCharsets.UTF_8);
            String[] parts = raw.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String payload = parts[0] + "." + parts[1];
            String expected = hmacSha256(payload, secret);
            if (!expected.equals(parts[2])) {
                return null;
            }
            long exp = Long.parseLong(parts[1]);
            if (Instant.now().getEpochSecond() > exp) {
                return null;
            }
            return parts[0];
        } catch (Exception ex) {
            return null;
        }
    }

    private static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("failed to generate signature", ex);
        }
    }
}
