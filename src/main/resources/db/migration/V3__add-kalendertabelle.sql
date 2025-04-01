CREATE TABLE kalendertag (
                             id character varying(36) NOT NULL,
                             created_time timestamp without time zone NOT NULL,
                             version bigint,
                             datum timestamp(6) without time zone NOT NULL UNIQUE,
                             tagestyp character varying(255) NOT NULL
);

ALTER TABLE kalendertag OWNER TO dave;

ALTER TABLE ONLY kalendertag
    ADD CONSTRAINT kalendertag_pkey PRIMARY KEY (id);

ALTER TABLE ONLY kalendertag
    ADD CONSTRAINT unique_kalendertag_datum UNIQUE (datum);

CREATE INDEX index_kalendertag_datum ON kalendertag USING BTREE(datum);