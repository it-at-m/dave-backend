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

@Component
@Slf4j
public class FlywaySchemaCallback implements Callback {

    private final String schema;

    public FlywaySchemaCallback(@Value("${db.schema}") final String schema) {
        this.schema = schema;
    }

    @Override
    public boolean supports(Event event, Context context) {
        // telling Flyway to only trigger callback for these events
        return event.equals(Event.BEFORE_EACH_MIGRATE) || event.equals(Event.AFTER_EACH_MIGRATE);
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return false;
    }

    @Override
    public void handle(Event event, Context context) {
        if (event.equals(org.flywaydb.core.api.callback.Event.BEFORE_MIGRATE)) {
            try (Connection connection = context.getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("SET search_path TO " + schema);
                }
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
