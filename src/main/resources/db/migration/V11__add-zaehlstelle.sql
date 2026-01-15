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
    kategorien character varying(1000),
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
    zaehlstelle character varying(36) NOT NULL,
    dienstleisterkennung character varying(255)
);

ALTER TABLE zaehlung OWNER TO dave;

ALTER TABLE ONLY zaehlung
    ADD CONSTRAINT zaehlung_pkey PRIMARY KEY (id);

ALTER TABLE ONLY zaehlung
    ADD CONSTRAINT fk_zaehlung_zaehlstelle FOREIGN KEY (zaehlstelle) REFERENCES zaehlstelle(id);

CREATE INDEX idx_zaehlung_zaehlstelle ON zaehlung(zaehlstelle);

ALTER TABLE ONLY zaehlung
    ADD CONSTRAINT fk_zaehlung_pkw_einheit FOREIGN KEY (pkw_einheit) REFERENCES pkweinheit(id);

CREATE INDEX idx_zaehlung_pkw_einheit ON zaehlung(pkw_einheit);

CREATE TABLE knotenarm (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    nummer integer,
    strassenname character varying(255),
    filename character varying(255),
    zaehlung character varying(36) NOT NULL
);

ALTER TABLE knotenarm OWNER TO dave;

ALTER TABLE ONLY knotenarm
    ADD CONSTRAINT knotenarm_pkey PRIMARY KEY (id);

ALTER TABLE ONLY knotenarm
    ADD CONSTRAINT fk_knotenarm_zaehlung FOREIGN KEY (zaehlung) REFERENCES zaehlung(id);

CREATE INDEX idx_knotenarm_zaehlung ON knotenarm(zaehlung);

-- Table for Fahrbeziehung (driving relationship/traffic flow)
CREATE TABLE fahrbeziehung (
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    
    -- Common fields
    is_kreuzung boolean,
    
    -- Kreuzung (intersection) fields
    von integer,
    nach integer,
    
    -- Kreisverkehr (roundabout) fields
    knotenarm integer,
    hinein boolean,
    heraus boolean,
    vorbei boolean,
    
    -- Knoten-Kanten-Modell (node-edge model) fields
    vonknotvonstrnr character varying(255),
    nachknotvonstrnr character varying(255),
    von_strnr character varying(255),
    vonknotennachstrnr character varying(255),
    nachknotnachstrnr character varying(255),
    nach_strnr character varying(255),
    
    hochrechnungsfaktor character varying(36),
    
    zaehlung character varying(36) NOT NULL
);

ALTER TABLE fahrbeziehung OWNER TO dave;

ALTER TABLE ONLY fahrbeziehung
    ADD CONSTRAINT fahrbeziehung_pkey PRIMARY KEY (id);

ALTER TABLE ONLY fahrbeziehung
    ADD CONSTRAINT fk_fahrbeziehung_zaehlung FOREIGN KEY (zaehlung) REFERENCES zaehlung(id);

-- Index on foreign key for better join performance
CREATE INDEX idx_fahrbeziehung_zaehlung ON fahrbeziehung(zaehlung);

ALTER TABLE ONLY fahrbeziehung
    ADD CONSTRAINT fk_fahrbeziehung_hochrechnungsfaktor FOREIGN KEY (hochrechnungsfaktor) REFERENCES hochrechnungsfaktor(id);

CREATE INDEX idx_fahrbeziehung_hochrechnungsfaktor ON fahrbeziehung(hochrechnungsfaktor);