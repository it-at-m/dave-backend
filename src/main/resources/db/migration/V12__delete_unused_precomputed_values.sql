-- Alle Datensätze von Zeitintervall mit TypeZeitintervall= SPITZENSTUNDE_X löschen
DELETE FROM zeitintervall WHERE TYPE = 'SPITZENSTUNDE_KFZ' OR TYPE = 'SPITZENSTUNDE_RAD';

-- Alle Datensätze von Zeitintervall, wo Zeitintervall.Fahrbeziehung.von=NULL oder Zeitintervall.Fahrbeziehung.nach=NULL (kein Kreisverkehr) löschen
DELETE FROM zeitintervall
WHERE (verkehrsbeziehung_von IS NULL OR verkehrsbeziehung_nach IS NULL) AND verkehrsbeziehung_fahrbewegungkreisverkehr IS NULL;