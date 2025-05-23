package de.muenchen.dave.domain.pdf.helper;

import java.util.List;
import lombok.Data;

@Data
public class DatentabellePdfZaehldaten {

    private List<DatentabellePdfZaehldatum> zaehldatenList;

    private int activeTabsFahrzeugtypen;

    private int activeTabsFahrzeugklassen;

    private int activeTabsAnteile;

    private boolean showPersonenkraftwagen;

    private boolean showLastkraftwagen;

    private boolean showLastzuege;

    private boolean showLieferwagen;

    private boolean showBusse;

    private boolean showKraftraeder;

    private boolean showRadverkehr;

    private boolean showFussverkehr;

    private boolean showKraftfahrzeugverkehr;

    private boolean showSchwerverkehr;

    private boolean showGueterverkehr;

    private boolean showSchwerverkehrsanteilProzent;

    private boolean showGueterverkehrsanteilProzent;

    private boolean showPkwEinheiten;

    // Die nachfolgenden Getter werden aus den Mustache Templates heraus aufgerufen

    public boolean getShowTabsFahrzeugtypen() {
        return (getActiveTabsFahrzeugtypen() > 0);
    }

    public boolean getShowTabsFahrzeugklassen() {
        return (getActiveTabsFahrzeugklassen() > 0);
    }

    public boolean getShowTabsAnteile() {
        return (getActiveTabsAnteile() > 0);
    }

    /**
     * Gibt die Spaltenbreite für Fahrzeugtypen zurück 42 steht dabei für 42% der Gesamtbreite
     * (Fahrzeugtypen sollen in der Tabelle optimalerweise 42%
     * einnehmen)
     *
     * @return Prozentsatz als int
     */
    public int getColWidthFahrzeugtypen() {
        return calcWidth(42, getActiveTabsFahrzeugtypen());
    }

    /**
     * Gibt die Spaltenbreite für Fahrzeugklassen zurück 21 steht dabei für 21% der Gesamtbreite
     * (Fahrzeugklassen sollen in der Tabelle optimalerweise 21%
     * einnehmen)
     *
     * @return Prozentsatz als int
     */
    public int getColWidthFahrzeugklassen() {
        return calcWidth(21, getActiveTabsFahrzeugklassen());
    }

    /**
     * Gibt die Spaltenbreite für Anteile zurück 8 steht dabei für 8% der Gesamtbreite (Anteile sollen
     * in der Tabelle optimalerweise 8% einnehmen)
     *
     * @return Prozentsatz als int
     */
    public int getColWidthAnteile() {
        return calcWidth(8, getActiveTabsAnteile());
    }

    /**
     * Hilfsfunktion zum Berechnen der Spaltenbreite
     *
     * @param maxWidth Maximale Breite
     * @param activeCategories Kategorien bzw. Spalten, die sich diese Breite teilen
     * @return
     */
    private int calcWidth(int maxWidth, int activeCategories) {
        if (activeCategories == 0) {
            return 0;
        } else {
            return (maxWidth / activeCategories);
        }
    }

    /**
     * Hier wird berechnet, welche die "rechteste" ausgewählte Spalte bei den Fahrzeugtypen ist. Diese
     * wird benötigt um zu wissen, wo in der Tabelle eine Border
     * gezeichnet werden soll.
     *
     * @return String, der im Mustache Template als CSS-Klasse hinterlegt ist
     */
    public String getRightBorderFahrzeugtypen() {
        if (isShowFussverkehr()) {
            return "fussverkehr";
        } else if (isShowRadverkehr()) {
            return "radverkehr";
        } else if (isShowKraftraeder()) {
            return "kraftraeder";
        } else if (isShowBusse()) {
            return "busse";
        } else if (isShowLieferwagen()) {
            return "lieferwagen";
        } else if (isShowLastzuege()) {
            return "lastzuege";
        } else if (isShowLastkraftwagen()) {
            return "lastkraftwagen";
        } else {
            return "personenkraftwagen";
        }
    }

    /**
     * Hier wird berechnet, welche die "rechteste" ausgewählte Spalte bei den Fahrzeugklassen ist. Diese
     * wird benötigt um zu wissen, wo in der Tabelle eine
     * Border gezeichnet werden soll.
     *
     * @return String, der im Mustache Template als CSS-Klasse hinterlegt ist
     */
    public String getRightBorderFahrzeugklassen() {
        if (isShowGueterverkehr()) {
            return "gueterverkehr";
        } else if (isShowSchwerverkehr()) {
            return "schwerverkehr";
        } else {
            return "kraftfahrzeugverkehr";
        }
    }

    /**
     * Hier wird berechnet, welche die "rechteste" ausgewählte Spalte bei den Anteilen ist. Diese wird
     * benötigt um zu wissen, wo in der Tabelle eine Border
     * gezeichnet werden soll.
     *
     * @return String, der im Mustache Template als CSS-Klasse hinterlegt ist
     */
    public String getRightBorderAnteile() {
        if (isShowGueterverkehrsanteilProzent()) {
            return "gueterverkehrsanteil-prozent";
        } else {
            return "schwerverkehrsanteil-prozent";
        }
    }

}
