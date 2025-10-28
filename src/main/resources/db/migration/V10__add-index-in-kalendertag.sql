DROP INDEX IF EXISTS index_kalendertag_datum;
CREATE INDEX index_combined_datum_tagestyp ON kalendertag USING BTREE(datum, tagestyp);