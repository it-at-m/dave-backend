
create table knotenarm (
                                   nummer integer not null,
                                   created_time TIMESTAMP not null,
                                   version bigint,
                                   id varchar(36) not null,
                                   zaehlung_id varchar(36) not null,
                                   filename varchar(255),
                                   strassenname varchar(255),
                                   primary key (id)
);

create table laengsverkehr (
                                   knotenarm integer,
                                   created_time TIMESTAMP not null,
                                   version bigint,
                                   hochrechnungsfaktor_id varchar(36),
                                   id varchar(36) not null,
                                   zaehlung_id varchar(36),
                                   richtung varchar(255) check (richtung in ('EIN','AUS')),
                                   strassenseite varchar(255) check (strassenseite in ('N','NO','O','SO','S','SW','W','NW')),
                                   primary key (id)
);

create table querungsverkehr (
                                   knotenarm integer,
                                   created_time TIMESTAMP not null,
                                   version bigint,
                                   hochrechnungsfaktor_id varchar(36),
                                   id varchar(36) not null,
                                   zaehlung_id varchar(36),
                                   richtung varchar(255) check (richtung in ('N','NO','O','SO','S','SW','W','NW')),
                                   primary key (id)
);

create table verkehrsbeziehung (
                                   heraus boolean,
                                   hinein boolean,
                                   is_kreuzung boolean,
                                   knotenarm integer,
                                   nach integer,
                                   von integer,
                                   vorbei boolean,
                                   created_time TIMESTAMP not null,
                                   version bigint,
                                   hochrechnungsfaktor_id varchar(36),
                                   id varchar(36) not null,
                                   zaehlung_id varchar(36),
                                   nach_strnr varchar(255),
                                   nachknotnachstrnr varchar(255),
                                   nachknotvonstrnr varchar(255),
                                   strassenseite varchar(255) check (strassenseite in ('N','NO','O','SO','S','SW','W','NW')),
                                   von_strnr varchar(255),
                                   vonknotennachstrnr varchar(255),
                                   vonknotvonstrnr varchar(255),
                                   primary key (id)
);

create table zaehlstelle (
                                   latitude float(53),
                                   letzte_zaehlung_jahr integer,
                                   letzte_zaehlung_monat_nummer integer,
                                   longitude float(53),
                                   sichtbar_datenportal boolean,
                                   stadtbezirk_nummer integer,
                                   created_time TIMESTAMP not null,
                                   version bigint,
                                   id varchar(36) not null,
                                   grund_letzte_zaehlung varchar(255),
                                   kommentar varchar(255),
                                   letzte_zaehlung_monat varchar(255),
                                   nummer varchar(255) not null,
                                   stadtbezirk varchar(255),
                                   custom_suchwoerter text[],
                                   suchwoerter text[],
                                   primary key (id)
);

create table zaehlung (
                                   jahr varchar(4),
                                   kreisverkehr boolean,
                                   latitude float(53),
                                   longitude float(53),
                                   monat varchar(255),
                                   sichtbar_datenportal boolean,
                                   sonderzaehlung boolean,
                                   unread_messages_dienstleister boolean,
                                   unread_messages_mobilitaetsreferat boolean,
                                   zaehl_intervall integer,
                                   created_time TIMESTAMP not null,
                                   datum date,
                                   version bigint,
                                   id varchar(36) not null,
                                   zaehlstelle_id varchar(36),
                                   dienstleisterkennung varchar(255),
                                   jahreszeit varchar(255),
                                   kommentar varchar(255),
                                   kreuzungsname varchar(255),
                                   projekt_name varchar(255),
                                   projekt_nummer varchar(255),
                                   quelle varchar(255) check (quelle in ('MANUALLY','DETECTOR','RADAR','VIDEO')),
                                   schul_zeiten varchar(255),
                                   status varchar(255) check (status in ('CREATED','INSTRUCTED','COUNTING','ACCOMPLISHED','CORRECTION','ACTIVE','INACTIVE')),
                                   tages_typ varchar(255) check (tages_typ in ('Werktag','Wochenende')),
                                   wetter varchar(255) check (wetter in ('SUNNY','SUNNY_COLD','CLOUDY','RAINY','CONTINUOUS_RAINY','FOGGY','SNOWY','NO_INFORMATION')),
                                   zaehlart varchar(255) check (zaehlart in ('N','H','Q','Q_','QB','QH','QI','QS','QT','QR','R','QJS','FJS','QU','T','TK')),
                                   zaehldauer varchar(255) check (zaehldauer in ('DAUER_2_X_4_STUNDEN','DAUER_24_STUNDEN','DAUER_16_STUNDEN','DAUER_13_STUNDEN','SONSTIGE')),
                                   zaehlsituation varchar(255),
                                   zaehlsituation_erweitert varchar(255),
                                   custom_suchwoerter text[],
                                   geographie text[],
                                   kategorien text[],
                                   suchwoerter text[],
                                   primary key (id)
);

alter table if exists knotenarm
    add constraint FK7abqxhfbfywi9hvrr5odh2i3a
    foreign key (zaehlung_id)
    references zaehlung;

alter table if exists laengsverkehr
    add constraint FKm1hrb05ggltuhimjirkwqwrlj
    foreign key (hochrechnungsfaktor_id)
    references Hochrechnungsfaktor;

alter table if exists laengsverkehr
    add constraint FK3uhddigm56p3jsmpbyxy68fgg
    foreign key (zaehlung_id)
    references zaehlung;

alter table if exists querungsverkehr
    add constraint FKmylvb5lbj7sf2y4545mcitmvy
    foreign key (hochrechnungsfaktor_id)
    references Hochrechnungsfaktor;

alter table if exists querungsverkehr
    add constraint FK2y3brl7oq80mm38h60c1ckr7i
    foreign key (zaehlung_id)
    references zaehlung;

alter table if exists verkehrsbeziehung
    add constraint FKjko55o3jxrulvde68pynglcnd
    foreign key (hochrechnungsfaktor_id)
    references Hochrechnungsfaktor;

alter table if exists verkehrsbeziehung
    add constraint FKa4r56octl7qcb74nup1cw2ifj
    foreign key (zaehlung_id)
    references zaehlung;

alter table if exists zaehlung
    add constraint FKggbhqg8htaryltgw2duhjjmvh
    foreign key (zaehlstelle_id)
    references zaehlstelle;
