package com.gestion.restaurant.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

/**
 * Normalisation / validation des identifiants admin (mémoire).
 */
public final class AdminCredentials {

    private static final Set<String> WEAK_PLAIN = Set.of(
            "changeme",
            "password",
            "admin",
            "123456",
            "12345678",
            "changez-moi-mot-de-passe-fort"
    );

    private AdminCredentials() {
    }

    /**
     * Encode un mot de passe clair en {@code {bcrypt}...}.
     * Si la valeur commence déjà par {@code {}}, elle est conservée (ex. {@code {noop}test} en tests).
     */
    public static String encodeForStorage(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new IllegalStateException("APP_ADMIN_PASSWORD est vide");
        }
        String password = rawPassword.trim();
        if (password.startsWith("{")) {
            return password;
        }
        PasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return "{bcrypt}" + bcrypt.encode(password);
    }

    /**
     * En production : refuse les mots de passe trop faibles ou d'exemple.
     */
    public static void assertProductionPasswordAcceptable(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new IllegalStateException(
                    "APP_ADMIN_PASSWORD doit être défini en production (profil prod).");
        }
        String password = rawPassword.trim();
        String plain = password.startsWith("{")
                ? stripEncodingId(password)
                : password;

        if (plain.length() < 12) {
            throw new IllegalStateException(
                    "APP_ADMIN_PASSWORD trop court en production (minimum 12 caractères).");
        }
        if (WEAK_PLAIN.contains(plain.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException(
                    "APP_ADMIN_PASSWORD trop faible / valeur d'exemple — choisissez un mot de passe fort.");
        }
    }

    private static String stripEncodingId(String encoded) {
        int end = encoded.indexOf('}');
        if (end < 0 || end + 1 >= encoded.length()) {
            return encoded;
        }
        return encoded.substring(end + 1);
    }
}
