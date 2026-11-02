CREATE TABLE zaehlstelle (
                             id varchar(36) NOT NULL,
                             nummer varchar(255),
                             stadtbezirk varchar(255),
                             stadtbezirk_nummer integer,
                             kommentar text,
                             latitude double precision,
                             longitude double precision,
                             letzte_zaehlung_monat_nummer integer,
                             letzte_zaehlung_monat varchar(255),
                             letzte_zaehlung_jahr integer,
                             grund_letzte_zaehlung varchar(255),
                             sichtbar_datenportal boolean,
                             suchwoerter jsonb,
                             custom_suchwoerter jsonb,
                             CONSTRAINT zaehlstelle_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX zaehlstelle_nummer_idx
    ON zaehlstelle (nummer);

CREATE TABLE zaehlung (
    id varchar(36) NOT NULL PRIMARY KEY,
    zaehlstelle_id varchar(36) NOT NULL REFERENCES zaehlstelle(id),
    datum date,
    jahr varchar(255),
    monat varchar(255),
    jahreszeit varchar(255),
    zaehlart varchar(255),
    latitude double precision,
    longitude double precision,
    tages_typ varchar(255),
    projekt_nummer varchar(255),
    projekt_name varchar(255),
    kreuzungsname varchar(255),
    sonderzaehlung boolean,
    kreisverkehr boolean,
    kategorien jsonb,
    status varchar(255),
    zaehlsituation varchar(255),
    zaehlsituation_erweitert varchar(255),
    zaehl_intervall integer,
    wetter varchar(255),
    zaehldauer varchar(255),
    quelle varchar(255),
    kommentar text,
    schul_zeiten varchar(255),
    suchwoerter jsonb,
    custom_suchwoerter jsonb,
    pkw_einheit jsonb,
    geographie jsonb,
    knotenarme jsonb,
    laengsverkehr jsonb,
    querungsverkehr jsonb,
    verkehrsbeziehungen jsonb,
    unread_messages_mobilitaetsreferat boolean,
    unread_messages_dienstleister boolean,
    dienstleisterkennung varchar(255)
);

CREATE TABLE messstelle (
                            id varchar(36) NOT NULL PRIMARY KEY,
                            mst_id varchar(255),
                            name varchar(255),
                            status varchar(255),
                            realisierungsdatum date,
                            abbaudatum date,
                            stadtbezirk_nummer integer,
                            bemerkung text,
                            fahrzeugklasse varchar(255),
                            detektierte_verkehrsart varchar(255),
                            hersteller varchar(255),
                            datum_letzte_plausible_messung date,
                            latitude double precision,
                            longitude double precision,
                            sichtbar_datenportal boolean,
                            geprueft boolean,
                            kommentar text,
                            standort text,
                            suchwoerter jsonb,
                            custom_suchwoerter jsonb,
                            lageplan_vorhanden boolean
);

CREATE TABLE messquerschnitt (
                                 id varchar(36) NOT NULL PRIMARY KEY,
                                 messstelle_id varchar(36) NOT NULL REFERENCES messstelle(id),
                                 mq_id varchar(255),
                                 latitude double precision,
                                 longitude double precision,
                                 strassenname varchar(255),
                                 lage_messquerschnitt varchar(255),
                                 fahrtrichtung varchar(255),
                                 anzahl_fahrspuren integer,
                                 anzahl_detektoren integer,
                                 standort varchar(255)
);

CREATE TABLE messfaehigkeit (
                                id varchar(36) NOT NULL PRIMARY KEY,
                                messstelle_id varchar(36) NOT NULL REFERENCES messstelle(id),
                                gueltig_ab date,
                                gueltig_bis date,
                                fahrzeugklasse varchar(255),
                                intervall varchar(255)
);
