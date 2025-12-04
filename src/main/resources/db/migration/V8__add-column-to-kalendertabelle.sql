ALTER TABLE IF EXISTS kalendertag ADD COLUMN next_start_date_to_load_unauffaellige_tage boolean DEFAULT NULL;

ALTER TABLE ONLY kalendertag
    ADD CONSTRAINT unique_kalendertag_next_start_date UNIQUE (next_start_date_to_load_unauffaellige_tage);

CREATE INDEX index_kalendertag_next_start_date ON kalendertag USING BTREE(next_start_date_to_load_unauffaellige_tage);