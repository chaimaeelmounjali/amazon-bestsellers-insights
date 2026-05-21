// ========== 2. CSVLoaderRunner.java - CommandLineRunner ==========
package com.example.amazonbestseller.config;

import com.example.amazonbestseller.service.CSVLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@org.springframework.core.annotation.Order(1)
@RequiredArgsConstructor
@Slf4j
public class CSVLoaderRunner implements CommandLineRunner {

    private final CSVLoaderService csvLoaderService;

    @Value("${app.load-csv-on-startup:false}")
    private boolean loadCsvOnStartup;

    @Override
    public void run(String... args) {
        if (loadCsvOnStartup) {
            log.info("🔄 Chargement automatique du CSV activé");
            try {
                csvLoaderService.chargerDonneesCSV();
            } catch (Exception e) {
                log.error("❌ Erreur lors du chargement automatique du CSV", e);
            }
        } else {
            log.info("⏸️  Chargement automatique du CSV désactivé");
            log.info("   Pour activer: app.load-csv-on-startup=true");
        }
    }
}
