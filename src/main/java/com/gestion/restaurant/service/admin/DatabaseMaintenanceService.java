package com.gestion.restaurant.service.admin;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Opérations de maintenance dangereuses sur la base de données
 * (vidage complet, reset des séquences).
 *
 * ATTENTION : toutes les méthodes de ce service sont IRRÉVERSIBLES.
 * L'accès est contrôlé au niveau du controller (voir AdminMaintenanceController),
 * qui exige une authentification (déjà imposée globalement par SecurityConfig)
 * ainsi qu'une confirmation textuelle explicite avant tout appel.
 */
@Service
public class DatabaseMaintenanceService {

    /**
     * Tables techniques à ne jamais toucher (migrations, etc.).
     * Ajoute ici tout nom de table que tu veux exclure du reset.
     */
    private static final List<String> TABLES_EXCLUES = List.of(
            "flyway_schema_history",
            "databasechangelog",
            "databasechangeloglock"
    );

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMaintenanceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Liste les tables "métier" du schéma public (hors tables techniques).
     * Utile pour afficher un récapitulatif avant confirmation.
     */
    public List<String> listUserTables() {
        String sql = "SELECT tablename FROM pg_tables WHERE schemaname = 'public' "
                + "AND tablename NOT IN (" + inClausePlaceholders(TABLES_EXCLUES.size()) + ") "
                + "ORDER BY tablename";
        return jdbcTemplate.queryForList(sql, String.class, TABLES_EXCLUES.toArray());
    }

    /**
     * Vide TOUTES les tables métier du schéma public et remet les séquences
     * (colonnes SERIAL/IDENTITY) à zéro, via TRUNCATE ... RESTART IDENTITY CASCADE.
     * <p>
     * Action IRRÉVERSIBLE : toutes les données de l'application sont perdues.
     */
    @Transactional
    public int truncateAllTables() {
        List<String> tables = listUserTables();
        if (tables.isEmpty()) {
            return 0;
        }
        String quoted = String.join(", ", tables.stream().map(t -> "\"" + t + "\"").toList());
        jdbcTemplate.execute("TRUNCATE TABLE " + quoted + " RESTART IDENTITY CASCADE");
        return tables.size();
    }

    /**
     * Remet à zéro (RESTART WITH 1) toutes les séquences du schéma public,
     * SANS effacer les données. Utile si les tables sont déjà vides et que
     * tu veux juste que le prochain ID reparte de 1.
     * <p>
     * Si des lignes existent encore, les prochains INSERT risquent de créer
     * des conflits de clé primaire : à utiliser normalement juste après un
     * truncate, ou sur une base vide.
     */
    @Transactional
    public int resetAllSequencesToZero() {
        List<String> sequences = jdbcTemplate.queryForList(
                "SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public'",
                String.class);
        for (String seq : sequences) {
            jdbcTemplate.execute("ALTER SEQUENCE \"" + seq + "\" RESTART WITH 1");
        }
        return sequences.size();
    }

    private String inClausePlaceholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(", ");
            sb.append("?");
        }
        return sb.toString();
    }
}