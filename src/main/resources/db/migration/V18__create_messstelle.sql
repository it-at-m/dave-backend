
create table messfaehigkeit (
                                   created_time TIMESTAMP not null,
                                   gueltig_ab date,
                                   gueltig_bis date,
                                   version bigint,
                                   id varchar(36) not null,
                                   messstelle_id varchar(36),
                                   fahrzeugklasse varchar(255) check (fahrzeugklasse in ('RAD','SUMME_KFZ','ZWEI_PLUS_EINS','ACHT_PLUS_EINS')),
                                   intervall varchar(255) check (intervall in ('STUNDE_VIERTEL','STUNDE_VIERTEL_EINGESCHRAENKT','STUNDE_HALB','STUNDE_KOMPLETT')),
                                   primary key (id)
);

create table messquerschnitt (
                                   anzahl_detektoren integer,
                                   anzahl_fahrspuren integer,
                                   latitude float(53),
                                   longitude float(53),
                                   created_time TIMESTAMP not null,
                                   version bigint,
                                   id varchar(36) not null,
                                   messstelle_id varchar(36),
                                   fahrtrichtung varchar(255),
                                   lage_messquerschnitt varchar(255),
                                   mq_id varchar(255) unique,
                                   standort varchar(255),
                                   strassenname varchar(255),
                                   primary key (id)
);

create table messstelle (
                                   geprueft boolean,
                                   lageplan_vorhanden boolean,
                                   latitude float(53),
                                   longitude float(53),
                                   sichtbar_datenportal boolean,
                                   stadtbezirk_nummer integer,
                                   abbaudatum date,
                                   created_time TIMESTAMP not null,
                                   datum_letzte_plausible_messung date,
                                   realisierungsdatum date,
                                   version bigint,
                                   id varchar(36) not null,
                                   bemerkung varchar(1000),
                                   detektierte_verkehrsart varchar(255) check (detektierte_verkehrsart in ('KFZ','RAD','UNBEKANNT')),
                                   fahrzeugklasse varchar(255) check (fahrzeugklasse in ('RAD','SUMME_KFZ','ZWEI_PLUS_EINS','ACHT_PLUS_EINS')),
                                   hersteller varchar(255),
                                   kommentar varchar(255),
                                   mst_id varchar(255) not null unique,
                                   name varchar(255),
                                   standort varchar(255),
                                   status varchar(255) check (status in ('IN_PLANUNG','IN_BESTAND','AUSSER_BETRIEB','ABGEBAUT','UNBEKANNT')),
                                   custom_suchwoerter text[],
                                   suchwoerter text[],
                                   primary key (id)
);

alter table if exists messfaehigkeit
    add constraint FKj6p4q3h4laa0f4d2sn0jk232d
    foreign key (messstelle_id)
    references messstelle;

alter table if exists messquerschnitt
    add constraint FK9ddm78b00ej8g70ogcd54w2h0
    foreign key (messstelle_id)
    references messstelle;