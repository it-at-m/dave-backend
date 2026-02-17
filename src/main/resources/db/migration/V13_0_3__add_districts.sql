CREATE TABLE IF NOT EXISTS "city_district"
(
    "id" character varying(36) NOT NULL,
    "created_time" timestamp without time zone NOT NULL,
    "version" bigint,
    "name" character varying(255) NOT NULL,
    "number" integer NOT NULL,
    "city" character varying(40) NOT NULL,
    CONSTRAINT "city_district_pkey" PRIMARY KEY ("id")
);

Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Altstadt-Lehel', 1, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Ludwigsvorstadt-Isarvorstadt', 2, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Maxvorstadt', 3, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Schwabing-West', 4, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Au-Haidhausen', 5, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Sendling', 6, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Sendling-Westpark', 7, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Schwanthalerhöhe', 8, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Neuhausen-Nymphenburg', 9, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Moosach', 10, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Milbertshofen-Am Hart', 11, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Schwabing-Freimann', 12, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Bogenhausen', 13, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Berg am Laim', 14, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Trudering-Riem', 15, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Ramersdorf-Perlach', 16, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Obergiesing-Fasangarten', 17, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Untergiesing-Harlaching', 18, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Thalkirchen-Obersendling-Forstenried-Fürstenried-Solln', 19, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Hadern', 20, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Pasing-Obermenzing', 21, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Aubing-Lochhausen-Langwied', 22, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Allach-Untermenzing', 23, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Feldmoching-Hasenbergl', 24, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Laim', 25, 'München');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Außerhalb der Stadtgrenze', 32, 'München');

Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Almke', 100, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Alt-Wolfsburg', 101, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Barnstorf', 102, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Brackstedt', 103, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Detmerode', 104, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Ehmen', 105, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Eichelkamp', 106, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Fallersleben', 107, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Hageberg', 108, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Hattorf', 109, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Hehlingen', 110, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Heiligendorf', 111, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Hellwinkel', 112, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Heßlingen', 113, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Hohenstein', 114, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Kästorf', 115, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Klieversberg', 116, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Kreuzheide', 117, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Köhlerberg', 118, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Laagberg', 119, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Mörse', 120, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Neindorf', 121, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Neuhaus', 122, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Nordstadt', 123, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Nordsteimke', 124, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Rabenberg', 125, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Reislingen', 126, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Rothenfelde', 127, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Sandkamp', 128, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Schillerteich', 129, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Stadtmitte', 130, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Steimker Berg', 131, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Steimker Gärten', 132, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Sülfeld', 133, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Teichbreite', 134, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Tiergartenbreite', 135, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Velstove', 136, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Vorsfelde', 137, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Wendschott', 138, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Westhagen', 139, 'Wolfsburg');
Insert into city_district (id, created_time, version, name, number, city) values (gen_random_uuid(), now(), 0, 'Wohltberg', 140, 'Wolfsburg');

INSERT INTO "configuration" ("id", "created_time", "version", "keyname", "valuefield", "category", "datatype")
VALUES (gen_random_uuid(), now(), 0, 'city', 'München', 'general', 'STRING')
ON CONFLICT (keyname) DO NOTHING;


