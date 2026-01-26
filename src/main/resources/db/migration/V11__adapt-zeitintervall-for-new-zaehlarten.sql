
ALTER TABLE zeitintervall
    DROP CONSTRAINT zeitintervall_fahrbeziehung_fahrbewegungkreisverkehr_check;

DROP INDEX IF EXISTS index_bewegungsbeziehung;

DROP INDEX IF EXISTS index_combined_1;

DROP INDEX IF EXISTS index_combined_2;

DROP INDEX IF EXISTS index_combined_3;

ALTER TABLE zeitintervall
    RENAME COLUMN fahrbeziehung_id TO bewegungsbeziehung_id;

ALTER TABLE zeitintervall
    RENAME COLUMN fahrbeziehung_von TO verkehrsbeziehung_von;

ALTER TABLE zeitintervall
    RENAME COLUMN fahrbeziehung_nach TO verkehrsbeziehung_nach;

ALTER TABLE zeitintervall
    RENAME COLUMN fahrbeziehung_fahrbewegungkreisverkehr TO verkehrsbeziehung_fahrbewegungkreisverkehr;

ALTER TABLE zeitintervall
    ADD COLUMN querungsverkehr_richtung character varying(255),
    ADD COLUMN laengsverkehr_richtung character varying(255),
    ADD COLUMN laengsverkehr_strassenseite character varying(255),
    ADD CONSTRAINT zeitintervall_querungsverkehr_richtung_check CHECK (((querungsverkehr_richtung)::text = ANY ((ARRAY['N'::character varying, 'NO'::character varying, 'O'::character varying, 'SO'::character varying, 'S'::character varying, 'SW'::character varying, 'W'::character varying, 'NW'::character varying])::text[]))),
    ADD CONSTRAINT zeitintervall_laengsverkehr_richtung_check CHECK (((laengsverkehr_richtung)::text = ANY ((ARRAY['EIN'::character varying, 'AUS'::character varying])::text[]))),
    ADD CONSTRAINT zeitintervall_laengsverkehr_strassenseite_check CHECK (((laengsverkehr_strassenseite)::text = ANY ((ARRAY['N'::character varying, 'NO'::character varying, 'O'::character varying, 'SO'::character varying, 'S'::character varying, 'SW'::character varying, 'W'::character varying, 'NW'::character varying])::text[]))),
    ADD CONSTRAINT zeitintervall_verkehrsbeziehung_fahrbewegungkreisverkehr_check CHECK (((verkehrsbeziehung_fahrbewegungkreisverkehr)::text = ANY ((ARRAY['HINEIN'::character varying, 'HERAUS'::character varying, 'VORBEI'::character varying])::text[])));

CREATE INDEX index_zeitintervall_bewegungsbeziehung_id ON zeitintervall USING btree (bewegungsbeziehung_id);

CREATE INDEX index_zeitintervall_combined_1 ON zeitintervall USING btree (zaehlung_id, type, verkehrsbeziehung_von, verkehrsbeziehung_nach);

CREATE INDEX index_zeitintervall_combined_2 ON zeitintervall USING btree (zaehlung_id, startuhrzeit, endeuhrzeit, verkehrsbeziehung_von, type);

CREATE INDEX index_zeitintervall_combined_3 ON zeitintervall USING btree (zaehlung_id, startuhrzeit, endeuhrzeit, verkehrsbeziehung_von, verkehrsbeziehung_nach, verkehrsbeziehung_fahrbewegungkreisverkehr, type);