INSERT INTO "configuration" ("id", "created_time", "version", "keyname", "valuefield", "category", "datatype")
VALUES (gen_random_uuid(), now(), 0, 'location_lon', '10.779998775029739', 'map', 'DOUBLE')
ON CONFLICT (keyname) DO NOTHING;

INSERT INTO "configuration" ("id", "created_time", "version", "keyname", "valuefield", "category", "datatype")
VALUES (gen_random_uuid(), now(), 0, 'location_lat', '52.41988232741599', 'map', 'DOUBLE')
ON CONFLICT (keyname) DO NOTHING;

INSERT INTO "configuration" ("id", "created_time", "version", "keyname", "valuefield", "category", "datatype")
VALUES (gen_random_uuid(), now(), 0, 'zoom', '12', 'map', 'INTEGER')
ON CONFLICT (keyname) DO NOTHING;

INSERT INTO "configuration" ("id", "created_time", "version", "keyname", "valuefield", "category", "datatype")
VALUES (gen_random_uuid(), now(), 0, 'zaehlstelleAutomaticNumberAssignment', 'true', 'dave', 'BOOLEAN')
ON CONFLICT (keyname) DO NOTHING;

INSERT INTO "configuration" ("id", "created_time", "version", "keyname", "valuefield", "category", "datatype")
VALUES (gen_random_uuid(), now(), 0, 'linkDocumentationCsvFileForUploadZaehlung', 'https://github.com/it-at-m/dave/blob/main/docs/src/de/documentation-csv-for-upload.md', 'dave', 'STRING')
ON CONFLICT (keyname) DO NOTHING;