DROP INDEX IF EXISTS index_zaehlung;

DROP INDEX IF EXISTS index_zeitintervall_combined_1;

DROP INDEX IF EXISTS index_zeitintervall_combined_2;

DROP INDEX IF EXISTS index_zeitintervall_combined_3;

ALTER TABLE zeitintervall
    ADD COLUMN laengsverkehr_knotenarm integer,
    ADD COLUMN querungsverkehr_knotenarm integer,
    ADD COLUMN verkehrsbeziehung_strassenseite character varying(255),
    ADD CONSTRAINT zeitintervall_verkehrsbeziehung_strassenseite_check CHECK (((querungsverkehr_richtung)::text = ANY ((ARRAY['N'::character varying, 'NO'::character varying, 'O'::character varying, 'SO'::character varying, 'S'::character varying, 'SW'::character varying, 'W'::character varying, 'NW'::character varying])::text[])));

CREATE INDEX index_zeitintervall_combined_1 ON zeitintervall USING btree (zaehlung_id, startuhrzeit, endeuhrzeit, type, verkehrsbeziehung_nach, verkehrsbeziehung_von, verkehrsbeziehung_strassenseite);

CREATE INDEX index_zeitintervall_combined_2 ON zeitintervall USING btree (zaehlung_id, startuhrzeit, endeuhrzeit, type, verkehrsbeziehung_von, verkehrsbeziehung_fahrbewegungkreisverkehr);

CREATE INDEX index_zeitintervall_combined_3 ON zeitintervall USING btree (zaehlung_id, startuhrzeit, endeuhrzeit, type, laengsverkehr_knotenarm, laengsverkehr_richtung, laengsverkehr_strassenseite);

CREATE INDEX index_zeitintervall_combined_4 ON zeitintervall USING btree (zaehlung_id, startuhrzeit, endeuhrzeit, type, querungsverkehr_knotenarm, querungsverkehr_richtung);
