CREATE TABLE IF NOT EXISTS zaehlstelle (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    nummer character varying(255),
    stadtbezirk character varying(255),
    stadtbezirk_nummer integer,
    kommentar text,
    letzte_zaehlung_monat_nummer integer,
    letzte_zaehlung_monat character varying(255),
    letzte_zaehlung_jahr integer,
    grund_letzte_zaehlung character varying(255),
    sichtbar_datenportal boolean
);

ALTER TABLE zaehlstelle OWNER TO dave;

ALTER TABLE ONLY zaehlstelle
    ADD CONSTRAINT zaehlstelle_pkey PRIMARY KEY (id);



