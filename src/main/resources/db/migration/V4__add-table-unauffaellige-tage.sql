create table unauffaelliger_tag
(
    id character varying(36) NOT NULL,
    created_time timestamp without time zone NOT NULL,
    version bigint,
    kalendertag_id character varying(36) NOT NULL,
    mst_id integer NOT NULL,
    constraint unauffaelliger_tag_pkey primary key (id),
    constraint unique_unauffaelliger_tag_mst_id_kalendertag_id UNIQUE (mst_id, kalendertag_id)
);

alter table unauffaelliger_tag owner to dave;

alter table only unauffaelliger_tag
    add constraint kalendertag_id_fkey foreign key (kalendertag_id) references kalendertag (id);

create index index_unauffaelliger_tag_mst_id on unauffaelliger_tag using btree (mst_id);



