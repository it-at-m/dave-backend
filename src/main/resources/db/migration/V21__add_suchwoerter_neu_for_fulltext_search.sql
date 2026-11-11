ALTER TABLE zaehlstelle
    ADD COLUMN suchwoerter_neu tsvector;

UPDATE zaehlstelle zs
SET suchwoerter_neu = to_tsvector(
    'simple'::regconfig,
    concat_ws(
        ' ',
        zs.nummer,
        zs.kommentar,
        array_to_string(zs.suchwoerter, ' '),
        (
            SELECT string_agg(
                concat_ws(
                    ' ',
                    z.projekt_name,
                    z.projekt_nummer,
                    z.kreuzungsname,
                    z.kommentar,
                    z.zaehlsituation,
                    z.zaehlsituation_erweitert
                ),
                ' '
            )
            FROM zaehlung z
            WHERE z.zaehlstelle_id = zs.id
        ),
        (
            SELECT string_agg(ka.strassenname, ' ')
            FROM knotenarm ka
            JOIN zaehlung z ON z.id = ka.zaehlung_id
            WHERE z.zaehlstelle_id = zs.id
        )
    )
);

CREATE INDEX idx_zaehlstelle_suchwoerter_neu
    ON zaehlstelle USING GIN (suchwoerter_neu);


ALTER TABLE messstelle
    ADD COLUMN suchwoerter_neu tsvector;

UPDATE messstelle
SET suchwoerter_neu = to_tsvector('simple'::regconfig, concat_ws(' ', mst_id, name));

CREATE INDEX idx_messstelle_suchwoerter_neu
    ON messstelle USING GIN (suchwoerter_neu);
