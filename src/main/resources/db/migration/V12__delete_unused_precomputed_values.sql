-- Alle Datensätze von Zeitintervall mit TypeZeitintervall= SPITZENSTUNDE_X löschen
DELETE FROM zeitintervall WHERE TYPE = 'SPITZENSTUNDE_KFZ' OR TYPE = 'SPITZENSTUNDE_RAD';

-- Alle Datensätze von Zeitintervall, wo Zeitintervall.Fahrbeziehung.von=NULL oder Zeitintervall.Fahrbeziehung.nach=NULL (kein Kreisverkehr) löschen
DELETE FROM zeitintervall
WHERE (fahrbeziehung_von IS NULL OR fahrbeziehung_nach IS NULL) AND fahrbeziehung_fahrbewegungkreisverkehr IS NULL;