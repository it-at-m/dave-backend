INSERT INTO "configuration" ("id", "created_time", "version", "keyname", "valuefield", "category", "datatype")
VALUES
    (gen_random_uuid(), now(), 0, 'location_lon', '10.779998775029739', 'map', 'DOUBLE'),
    (gen_random_uuid(), now(), 0, 'location_lat', '52.41988232741599', 'map', 'DOUBLE'),
    (gen_random_uuid(), now(), 0, 'city', 'Wolfsburg', 'general', 'STRING');