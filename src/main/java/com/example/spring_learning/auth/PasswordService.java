package com.example.spring_learning.auth;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordService {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(String rawPassword) {
        try {
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);
            byte[] hash = pbkdf2(rawPassword, salt, ITERATIONS);

            return ITERATIONS
                    + ":"
                    + Base64.getEncoder().encodeToString(salt)
                    + ":"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash password", exception);
        }
    }

    public boolean matches(String rawPassword, String storedPassword) {
        try {
            String[] parts = storedPassword.split(":");
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
            byte[] actualHash = pbkdf2(rawPassword, salt, iterations);

            if (actualHash.length != expectedHash.length) {
                return false;
            }

            int result = 0;
            for (int index = 0; index < actualHash.length; index++) {
                result |= actualHash[index] ^ expectedHash[index];
            }
            return result == 0;
        } catch (Exception exception) {
            return false;
        }
    }

    private byte[] pbkdf2(String password, byte[] salt, int iterations) throws Exception {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return keyFactory.generateSecret(keySpec).getEncoded();
    }
}
