CREATE TABLE dave.kalendertag (
                             id character varying(36) NOT NULL,
                             created_time timestamp without time zone NOT NULL,
                             version bigint,
                             datum timestamp(6) without time zone NOT NULL UNIQUE,
                             tagestyp character varying(255) NOT NULL,
                             feiertag character varying(255),
                             ferientyp character varying(255)
);

ALTER TABLE dave.kalendertag OWNER TO dave;

ALTER TABLE ONLY dave.kalendertag
    ADD CONSTRAINT kalendertag_pkey PRIMARY KEY (id);