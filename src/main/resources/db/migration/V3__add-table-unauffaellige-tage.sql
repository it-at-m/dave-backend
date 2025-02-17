create table dave.unauffaellige_tage
(
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    mst_id integer NOT NULL,
    datum timestamp(6) without time zone NOT NULL,
    constraint unauffaellige_tage_pkey primary key (id),
    constraint unique_unauffaellige_tage_mst_id_datum UNIQUE (mst_id, datum)
);

create index index_unauffaellige_tage_mst_id_datum on dave.unauffaellige_tage using btree (mst_id, datum);



