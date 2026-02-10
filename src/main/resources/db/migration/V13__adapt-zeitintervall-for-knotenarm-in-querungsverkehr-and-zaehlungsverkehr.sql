
ALTER TABLE zeitintervall
    ADD COLUMN laengsverkehr_knotenarm integer,
    ADD COLUMN querungsverkehr_knotenarm integer,
    ADD COLUMN verkehrsbeziehung_strassenseite character varying(255);
