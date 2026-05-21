// ========== 8. SchedulingConfig.java - Configuration Tâches Planifiées ==========
package com.example.amazonbestseller.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Cette configuration active les @Scheduled dans AlerteService
}