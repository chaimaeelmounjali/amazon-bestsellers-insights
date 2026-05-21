package com.example.amazonbestseller;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordTest {
    @Test
    public void testPasswordMatch() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("VALID_HASH_FOR_PASSWORD: " + encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword), "Password should match");
    }
}
