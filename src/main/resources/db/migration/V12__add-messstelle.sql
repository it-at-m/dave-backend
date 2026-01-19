CREATE TABLE IF NOT EXISTS messstelle (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    mst_id character varying(255),
    name character varying(255),
    status character varying(50),
    realisierungsdatum date,
    abbaudatum date,
    stadtbezirk_nummer integer,
    bemerkung text,
    fahrzeugklasse character varying(50),
    detektierte_verkehrsart character varying(50),
    hersteller character varying(255),
    datum_letzte_plausible_messung date,
    latitude double precision,
    longitude double precision,
    sichtbar_datenportal boolean,
    geprueft boolean DEFAULT false,
    kommentar text,
    standort character varying(255),
    suchwoerter character varying(1000),
    custom_suchwoerter character varying(1000),
    lageplan_vorhanden boolean DEFAULT false
);

ALTER TABLE messstelle OWNER TO dave;

ALTER TABLE ONLY messstelle
    ADD CONSTRAINT messstelle_pkey PRIMARY KEY (id);

CREATE INDEX idx_messstelle_mst_id ON messstelle(mst_id);
CREATE INDEX idx_messstelle_status ON messstelle(status);
CREATE INDEX idx_messstelle_stadtbezirk ON messstelle(stadtbezirk_nummer);

-- Create table for Messquerschnitt entity
CREATE TABLE messquerschnitt (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    mq_id character varying(255),
    latitude double precision,
    longitude double precision,
    strassenname character varying(255),
    lage_messquerschnitt character varying(255),
    fahrtrichtung character varying(255),
    anzahl_fahrspuren integer,
    anzahl_detektoren integer,
    standort character varying(255),
    messstelle character varying(36) NOT NULL
);

ALTER TABLE messquerschnitt OWNER TO dave;

ALTER TABLE ONLY messquerschnitt
    ADD CONSTRAINT messquerschnitt_pkey PRIMARY KEY (id);

ALTER TABLE ONLY messquerschnitt
    ADD CONSTRAINT fk_messquerschnitt_messstelle FOREIGN KEY (messstelle) REFERENCES messstelle(id);

CREATE INDEX idx_messquerschnitt_mq_id ON messquerschnitt(mq_id);
CREATE INDEX idx_messquerschnitt_messstelle ON messquerschnitt(messstelle);

-- Create table for Messfaehigkeit entity
CREATE TABLE messfaehigkeit (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    gueltig_ab date,
    gueltig_bis date,
    fahrzeugklasse character varying(50),
    intervall character varying(50),
    messstelle character varying(36) NOT NULL
);

ALTER TABLE messfaehigkeit OWNER TO dave;

ALTER TABLE ONLY messfaehigkeit
    ADD CONSTRAINT messfaehigkeit_pkey PRIMARY KEY (id);

ALTER TABLE ONLY messfaehigkeit
    ADD CONSTRAINT fk_messfaehigkeit_messstelle FOREIGN KEY (messstelle) REFERENCES messstelle(id);

CREATE INDEX idx_messfaehigkeit_messstelle ON messfaehigkeit(messstelle);
CREATE INDEX idx_messfaehigkeit_dates ON messfaehigkeit(gueltig_ab, gueltig_bis);
