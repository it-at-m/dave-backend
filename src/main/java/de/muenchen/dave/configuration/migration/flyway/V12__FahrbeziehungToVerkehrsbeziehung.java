package de.muenchen.dave.configuration.migration.flyway;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.ScriptLanguage;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import java.io.IOException;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

// spotless:off
/**
 * Das Migrationsscript führt das folgende Elasticsearch painless-Skript aus.
 *
 * POST /<INDEX>/_update_by_query
 * {
 *   "script": {
 *     "source":"""
 *    if (ctx._source.zaehlungen != null) {
 *      for (int i=0; i< ctx._source.zaehlungen.length; i++){
 *        if (ctx._source.zaehlungen[i].fahrbeziehungen != null) {
 *          ctx._source.zaehlungen[i].verkehrsbeziehungen = ctx._source.zaehlungen[i].fahrbeziehungen;
 *          ctx._source.zaehlungen[i].remove('fahrbeziehungen');
 *        }
 *      }
 *     }
 *     """
 *  },
 *   "query": {
 *     "match_all": {}
 *   }
 * }
 *
 */
// spotless:on
@Component
@RequiredArgsConstructor
@Slf4j
public class V12__FahrbeziehungToVerkehrsbeziehung extends BaseJavaMigration {

    private static final String SCRIPT_SOURCE_ZAEHLSTELLE_COPY_FAHRBEZIEHUNG_TO_VERKEHRSBEZIEHUNG_AND_REMOVE_FAHRBEZIEHUNGEN = "" +
            "if (ctx._source.zaehlungen != null) { \n" +
            "  for (int i=0; i< ctx._source.zaehlungen.length; i++){\n" +
            "    if (ctx._source.zaehlungen[i].fahrbeziehungen != null) {\n" +
            "      ctx._source.zaehlungen[i].verkehrsbeziehungen = ctx._source.zaehlungen[i].fahrbeziehungen;\n" +
            "      ctx._source.zaehlungen[i].remove('fahrbeziehungen');\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private final ElasticsearchOperations elasticsearchOperations;

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public void migrate(final Context context) throws SQLException {
        try {
            final var script = new Script.Builder()
                    .lang(ScriptLanguage.Painless)
                    .source(SCRIPT_SOURCE_ZAEHLSTELLE_COPY_FAHRBEZIEHUNG_TO_VERKEHRSBEZIEHUNG_AND_REMOVE_FAHRBEZIEHUNGEN)
                    .build();

            final var matchAllQuery = QueryBuilders.matchAll().build();

            final var indexName = elasticsearchOperations.getIndexCoordinatesFor(Zaehlstelle.class).getIndexName();

            final var updateByQueryRequest = new UpdateByQueryRequest.Builder()
                    .index(indexName)
                    .script(script)
                    .query(matchAllQuery)
                    .build();

            final var response = elasticsearchClient.updateByQuery(updateByQueryRequest);
            final var responseInfo = response.toString();
            if (CollectionUtils.isNotEmpty(response.failures())) {
                log.error(responseInfo);
                throw new IOException(responseInfo);
            }
            log.info(response.toString());
        } catch (final IOException e) {
            final var message = "Die Elasticsearchmigration V12 mittels flyway ist fehlgeschlagen.";
            throw new SQLException(message, e);
        }
    }

}
