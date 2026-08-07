package com.gestion.restaurant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Refuse de démarrer en production avec un mot de passe admin faible ou d'exemple.
 */
@Component
@Profile("prod")
public class ProdSecurityGuard implements ApplicationRunner {

    private final String adminPassword;

    public ProdSecurityGuard(@Value("${app.admin.password}") String adminPassword) {
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        AdminCredentials.assertProductionPasswordAcceptable(adminPassword);
    }
}
