package de.muenchen.dave.domain.enums;

/**
 * Versionierte Eingabeformate fuer ONNX-Modelle.
 *
 * <p>
 * Ein neues Tensorformat erfordert einen neuen Enum-Wert und einen passenden
 * {@code ModelInputEncoder}; bestehende Formate bleiben damit unveraendert reproduzierbar.
 * </p>
 */
public enum ModelInputSchema {

    /** Zehn Kontextmerkmale je Viertelstundenintervall, einschliesslich des Radwerts. */
    KONTEXT_RAD_V1,
    /** Ausschliesslich ein Radwert je Viertelstundenintervall. */
    REINE_RADWERTE_V1

}
