package de.muenchen.dave.domain.dtos;

import de.muenchen.dave.domain.dtos.external.ExternalLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class OptionsDTO implements Serializable {

    @NotNull
    private Zaehldauer zaehldauer;

    @NotNull
    private ZaehldatenIntervall intervall;

    @NotNull
    private Zeitblock zeitblock;

    @NotNull
    private String zeitauswahl;

    @NotNull
    private Boolean kraftfahrzeugverkehr;

    @NotNull
    private Boolean schwerverkehr;

    @NotNull
    private Boolean gueterverkehr;

    @NotNull
    private Boolean radverkehr;

    @NotNull
    private Boolean fussverkehr;

    @NotNull
    private Boolean schwerverkehrsanteilProzent;

    @NotNull
    private Boolean gueterverkehrsanteilProzent;

    @NotNull
    private Boolean pkwEinheiten;

    @NotNull
    private Boolean personenkraftwagen;

    @NotNull
    private Boolean lastkraftwagen;

    @NotNull
    private Boolean lastzuege;

    @NotNull
    private Boolean busse;

    @NotNull
    private Boolean kraftraeder;

    @NotNull
    private Boolean stundensumme;

    @NotNull
    private Boolean blocksumme;

    @NotNull
    private Boolean tagessumme;

    @NotNull
    private Boolean spitzenstunde;

    @NotNull
    private Boolean spitzenstundeKfz;

    @NotNull
    private Boolean spitzenstundeRad;

    @NotNull
    private Boolean spitzenstundeFuss;

    @NotNull
    private Boolean mittelwert;

    @NotNull
    private Boolean fahrzeugklassenStapeln;

    @NotNull
    private Boolean beschriftung;

    @NotNull
    private Boolean datentabelle;

    @NotNull
    private Boolean werteHundertRunden;

    @NotNull
    private Boolean differenzdatenDarstellen;

    @NotNull
    private Boolean zeitreiheGesamt;

    private String vergleichszaehlungsId;

    private Boolean beideRichtungen;

    // Für alle Zaehlarten die nicht QjS, FjS und Qu sind
    private Integer vonKnotenarm;

    private Integer nachKnotenarm;

    // Für Zaehlart QjS (Querschnitt je Straßenseite für Fuss- und Radverkehr)
    private List<ExternalVerkehrsbeziehungDTO> chosenVerkehrsbeziehungen;

    // Für Zaehlart FjS
    private List<ExternalLaengsverkehrDTO> chosenLangsverkehre;

    // Für Zaehlart Qu
    private List<ExternalQuerungsverkehrDTO> chosenQuerungsverkehre;

    private String idVergleichszaehlungZeitreihe;
}
