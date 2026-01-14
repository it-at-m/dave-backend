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
    latitude double precision,
    longitude double precision,
    suchwoerter character varying(1000),
    custom_suchwoerter character varying(1000),
    sichtbar_datenportal boolean
);

ALTER TABLE zaehlstelle OWNER TO dave;

ALTER TABLE ONLY zaehlstelle
    ADD CONSTRAINT zaehlstelle_pkey PRIMARY KEY (id);

-- Create table for Zaehlung entity
CREATE TABLE zaehlung (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    datum  timestamp without time zone NOT NULL,
    jahr integer,
    monat character varying(100),
    jahreszeit character varying(255),
    zaehlart character varying(255),
    
    -- GeoPoint embedded fields
    latitude double precision,
    longitude double precision,
    
    tages_typ character varying(255),
    projekt_nummer character varying(255),
    projekt_name character varying(255),
    kreuzungsname character varying(255),
    sonderzaehlung boolean,
    kreisverkehr boolean,
    status character varying(255),
    zaehlsituation character varying(255),
    zaehlsituation_erweitert character varying(255),
    zaehlintervall integer NOT NULL,
    wetter character varying(255),
    zaehldauer character varying(255),
    quelle character varying(255),
    kommentar TEXT,
    schul_zeiten character varying(255),
    suchwoerter character varying(1000),
    custom_suchwoerter character varying(1000),
    pkw_einheit character varying(36),
    geographie character varying(500),
    unread_messages_mobilitaetsreferat boolean,
    unread_messages_dienstleister boolean,
    zaehlstelle character varying(36),
    dienstleisterkennung character varying(255)
);

ALTER TABLE zaehlung OWNER TO dave;

ALTER TABLE ONLY zaehlung
    ADD CONSTRAINT zaehlung_pkey PRIMARY KEY (id);