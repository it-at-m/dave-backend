package de.muenchen.dave.configuration;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Die Klasse implementiert die Flyway Callback-Schnittstelle, um vor bestimmten Migrationsschritten
 * zusätzl. Vorarbeiten durchzuführen.
 */
@Component
@Slf4j
public class FlywaySchemaCallback implements Callback {

    private final String schema;

    public FlywaySchemaCallback(@Value("${db.schema}") final String schema) {
        this.schema = schema;
    }

    @Override
    public boolean supports(final Event event, final Context context) {
        // telling Flyway to only trigger callback for these events
        return event.equals(Event.BEFORE_MIGRATE);
    }

    @Override
    public boolean canHandleInTransaction(final Event event, final Context context) {
        return false;
    }

    @Override
    public void handle(final Event event, final Context context) {
        if (event.equals(org.flywaydb.core.api.callback.Event.BEFORE_MIGRATE)) {
            final Connection connection = context.getConnection();
            try (final Statement statement = connection.createStatement()) {
                statement.execute("SET search_path TO " + schema);
                statement.execute("CREATE SCHEMA IF NOT EXISTS " + schema + ";");
            } catch (SQLException e) {
                log.error("Fehler bei der Migrationsvorbereitung", e);
            }
        }
    }

    @Override
    public String getCallbackName() {
        return this.getClass().getSimpleName();
    }
}
