package de.muenchen.dave.util;

import java.time.LocalDate;
import java.time.Month;

public interface DaveConstants {

    public static final String DATE_FORMAT = "dd.MM.yyyy";

    public static final String ZEITINTERVALL_TIME_FORMAT = "HH:mm";

    /**
     * Oracle-DBs koennen nicht mit der columnDefintion 'TIME' umgehen. Daher ist es noetig gewesen, die Zeitintervalle mit einem konkreten Datum zu speichern.
     * Per Default wird sonst immer das aktuelle Datum verwendet, was dazu fuehrt, dass die Intervalle nicht mehr anhand des Uhrzeit gefunden werden koennen.
     * Daher wird nun immer nach dem #DaveConstants.DEFAULT_LOCALDATE + der Zeit des Intervalls gesucht.
     */
    // Vorstellung Zuse Z3 (12.05.1941)
    public static final LocalDate DEFAULT_LOCALDATE = LocalDate.of(1941, Month.MAY, 12);
}
