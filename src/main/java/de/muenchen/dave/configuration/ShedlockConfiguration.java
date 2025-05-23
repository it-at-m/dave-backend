/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.configuration;

import de.muenchen.dave.domain.Shedlock;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ShedlockConfiguration {

    @Value(value = "${db.schema}")
    private String dbschema;

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {

        JdbcTemplateLockProvider.Configuration configuration = JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime() // Works on Postgres, MySQL, MariaDb, MS SQL, Oracle, DB2, HSQL and H2
                .withTableName(String.format("\"%s\".%s", dbschema, Shedlock.TABLE_NAME))
                .build();
        return new JdbcTemplateLockProvider(
                configuration);
    }
}
