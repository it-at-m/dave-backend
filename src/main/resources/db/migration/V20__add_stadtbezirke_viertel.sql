CREATE TABLE IF NOT EXISTS stadtbezirk
(
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    nummer integer NOT NULL,
    name character varying(255) NOT NULL,
    stadt character varying(40) NOT NULL,
    PRIMARY KEY ("id")
);

CREATE INDEX idx_stadtbezirk_stadt ON stadtbezirk (stadt);

CREATE TABLE IF NOT EXISTS stadtbezirk_viertel
(
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    nummer character varying(20) NOT NULL,
    name character varying(255),
    stadtbezirk_id character varying(36) NOT NULL,
    PRIMARY KEY ("id")
);

alter table if exists stadtbezirk_viertel
    add constraint FK7abqxhfbfywi9hvrr5odh2i3a
    foreign key (stadtbezirk_id)
    references stadtbezirk;
