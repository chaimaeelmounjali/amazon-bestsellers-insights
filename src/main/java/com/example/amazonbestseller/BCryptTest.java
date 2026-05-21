package com.example.amazonbestseller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password";
        String encodedPassword = "$2a$10$8.UnVuG9HHgffUDAlk8q7uy5qFE7Ec.pXqG0i6pGg6QO8qW.N5mP6";

        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("Password matches: " + matches);
    }
}
