DELETE INDEX index_kalendertag_datum ON kalendertag;
CREATE INDEX index_combined_datum_tagestyp ON kalendertag USING BTREE(datum, tagestyp);