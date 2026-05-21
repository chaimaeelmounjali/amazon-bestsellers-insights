// ========== 2. SecurityConfig.java - Configuration Sécurité ==========
package com.example.amazonbestseller.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // Routes publiques
                                                .requestMatchers(
                                                                "/api/auth/**",
                                                                "/api/produits/**",
                                                                "/api/recherche/**",
                                                                "/api/dashboard/global",
                                                                "/api/filtrage/**",
                                                                "/api/avis/produit/**",
                                                                "/api/avis/note-moyenne/**",
                                                                "/api/diag/**")
                                                .permitAll()
                                                // Routes protégées
                                                .requestMatchers(
                                                                "/api/ventes/**",
                                                                "/api/commandes/**",
                                                                "/api/alertes/**",
                                                                "/api/dashboard/acheteur/**",
                                                                "/api/dashboard/vendeur/**")
                                                .authenticated()
                                                .requestMatchers("/api/dashboard/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/export/**").hasAnyRole("ADMIN", "VENDEUR")
                                                .requestMatchers("/api/analyse-predictive/**")
                                                .hasAnyRole("ADMIN", "INVESTISSEUR", "VENDEUR")
                                                .anyRequest().authenticated())
                                .httpBasic(httpBasic -> httpBasic.realmName("AmazonBestSeller API"));

                return http.build();
        }
}