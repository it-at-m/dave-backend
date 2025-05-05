ALTER TABLE IF EXISTS kalendertag ADD COLUMN next_startday_to_load_unauffaellige_tage boolean DEFAULT NULL;

ALTER TABLE ONLY kalendertag
    ADD CONSTRAINT unique_kalendertag_next_startday UNIQUE (next_startday_to_load_unauffaellige_tage);

CREATE INDEX index_kalendertag_next_startday ON kalendertag USING BTREE(next_startday_to_load_unauffaellige_tage);