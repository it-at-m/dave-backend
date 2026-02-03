CREATE TABLE "configuration"
(
    "id" character varying(36) NOT NULL,
    "created_time" timestamp without time zone NOT NULL,
    "version" bigint,
    "keyname" VARCHAR(255) NOT NULL,
	"valuefield" VARCHAR(1024) NOT NULL,
	"category" VARCHAR(255) NOT NULL,
    "datatype" VARCHAR(50) NOT NULL,
    CONSTRAINT "configuration_unique_key" UNIQUE("keyname"),
    CONSTRAINT "configuration_pkey" PRIMARY KEY ("id")
);

ALTER TABLE "configuration" OWNER TO dave;

INSERT INTO "configuration" ("id", "created_time", "version", "keyname", "valuefield", "category", "datatype")
VALUES
    (gen_random_uuid(), now(), 0, 'location_lon', '10.779998775029739', 'map', 'DOUBLE'),
    (gen_random_uuid(), now(), 0, 'location_lat', '52.41988232741599', 'map', 'DOUBLE'),
    (gen_random_uuid(), now(), 0, 'city', 'Wolfsburg', 'general', 'STRING');