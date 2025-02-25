create table unauffaellige_tage
(
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    kalendertag_id character varying(36) NOT NULL,
    mst_id integer NOT NULL,
    constraint unauffaellige_tage_pkey primary key (id),
    constraint unique_unauffaellige_tage_mst_id_kalendertag_id UNIQUE (mst_id, kalendertag_id)
);

alter table only unauffaellige_tage
    add constraint kalendertag_id_fkey foreign key (kalendertag_id) references kalendertag (id);

create index index_unauffaellige_tage_mst_id on unauffaellige_tage using btree (mst_id);



