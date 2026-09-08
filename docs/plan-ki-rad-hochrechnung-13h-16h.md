# Umsetzungsplan: KI-Hochrechnung Radverkehr fuer 13h und 16h

## Ziel und Umfang

Die bestehende Radverkehrshochrechnung fuer 2x4h-Zaehlungen wird so modularisiert, dass fuer jede Zaehlungsdauer ein eigenes ONNX-Modell verwendet werden kann. In Teil 1 werden drei Radmodelle unterstuetzt:

| Zaehlungsdauer | Modell                      | Eingabe pro Bewegungsbeziehung |
| --- |-----------------------------| --- |
| `DAUER_2_X_4_STUNDEN` | `Rad_Modell_DAVE_2x4h.onnx` | Bestehendes Schema mit Radwert und Kalendermerkmalen fuer 32 Intervalle |
| `DAUER_13_STUNDEN` | `Rad_Modell_DAVE_13h.onnx`  | 52 reine Radzaehlwerte in zeitlicher Reihenfolge |
| `DAUER_16_STUNDEN` | `Rad_Modell_DAVE_16h.onnx`  | 64 reine Radzaehlwerte in zeitlicher Reihenfolge |

Teil 1 veraendert weder die vorhandenen Hochrechnungswerte fuer Kfz, SV und GV noch fuegt er neue Fahrzeugtypen als Hochrechnungswerte hinzu. Diese Erweiterung ist Teil 2 und wird erst nach Abschluss von Teil 1 umgesetzt.

## Fachliche Festlegungen

- Die neuen 13h- und 16h-Modelle erhalten ausschliesslich die erfassten Radwerte; Kalendermerkmale werden nicht uebergeben.
- Die Eingabereihenfolge ist zeitlich aufsteigend anhand des bestehenden `sortingIndex`.
- Die ONNX-Modelle verwenden den Eingabetensornamen `int64_input` und den Datentyp `int64`.
- Die Ausgabe bleibt `long[anzahlBewegungsbeziehungen][1]`. Der einzelne Wert je Bewegungsbeziehung ist die vorhergesagte Rad-Tagessumme.
- Die Zeitfenster und die erwartete Zahl der Viertelstundenintervalle werden ausschliesslich aus `Zaehldauer` abgeleitet. Sie werden nicht nochmals pro Modell konfiguriert.
- Fehlende, nicht lesbare oder inkompatible 13h-/16h-Modelle verhindern den Anwendungsstart nicht. Die betroffene Zaehlung wird ohne KI-Radhochrechnung verarbeitet; der Fehler wird mit Modell-ID und Zaehlungs-ID protokolliert.
- Bestehende abgeschlossene 13h- und 16h-Zaehlungen werden nicht automatisch nachberechnet. Die neuen Modelle gelten fuer kuenftige Abschluesse sowie regulare erneute Aufbereitungen.

## Bestehende Grundlage

`ZeitintervallZeitblockSummationUtil` bildet Zaehlungsdauern bereits auf Zeitbloecke ab:

| Zaehlungsdauer | Zeitfenster fuer die Modelleinabe | Bestehende Definition |
| --- | --- | --- |
| 2x4h | 06:00-10:00 und 15:00-19:00 | `ZB_06_10`, `ZB_15_19` |
| 13h | 06:00-19:00 | `ZB_06_19` |
| 16h | 06:00-22:00 | `ZB_06_22` |

Die bestehende Methode `getZeitbloeckeAccordingZaehldauer` ist nicht direkt als Modelleingabe geeignet, weil sie neben den Erhebungsfenstern auch Stunden-, Halbstunden- und Tagesaggregate liefert. Eine neue, gezielt benannte Methode soll nur die urspruenglichen Erhebungsfenster liefern.

Die aktuelle Implementierung in `KIService` ist auf das bisherige 2x4h-Radmodell zugeschnitten: Sie filtert die zwei Spitzenzeitfenster fest, mappt jedes Intervall auf zehn Merkmale und erwartet daher 32 mal zehn Eingabewerte. Diese Kopplung wird aufgeloest.

## Zielarchitektur

```mermaid
classDiagram
    class ZeitintervallPersistierungsService {
        +aufbereitenUndPersistieren(zaehldauer, intervalle, kiAufbereitung)
    }

    class HochrechnungsService {
        +calculateRadhochrechnung(zaehldauer, groupedIntervalle) List~KIPredictionResult~
    }

    class OnnxModelRegistry {
        -Map~ModelSet, Hochrechnungsmodell~ models
        +findModel(zaehldauer, hochrechnungskategorie) Optional~Hochrechnungsmodell~
        +closeModels()
    }

    class Hochrechnungsmodell {
        <<interface>>
        +getDefinition() OnnxModelDefinition
        +calculate(groupedZeitintervalle) List~KIPredictionResult~
    }

    class OnnxHochrechnungsmodell {
        -OrtSession session
        -OnnxModelDefinition definition
        -ModelInputEncoder modelInputEncoder
        +calculate(groupedZeitintervalle) List~KIPredictionResult~
        +closeSession()
    }

    class ModelInputEncoder {
        <<interface>>
        +encode(zaehldauer, groupedIntervalle) long[][]
        +getSchema() ModelInputSchema
    }

    class KontextRadV1Encoder {
        +encode(zaehldauer, groupedIntervalle) long[][]
    }

    class ReineRadwerteV1Encoder {
        +encode(zaehldauer, groupedIntervalle) long[][]
    }

    class OnnxModelDefinition {
        +String id
        +Hochrechnungskategorie hochrechnungskategorie
        +Zaehldauer zaehldauer
        +String resourcePath
        +String inputTensorName
        +ModelInputSchema inputSchema
    }

    class OnnxModelProperties {
        +List~OnnxModelDefinition~ modelle
    }

    class Zaehldauer {
        +int anzahlZeitintervalle
        +List~Zeitblock~ modelInputZeitbloecke
    }

    class KIPredictionResult {
        +int radTagessumme
    }

    class ZeitintervallKIUtil {
        <<utility>>
        +groupZeitintervalleByBewegungsbeziehung(intervalle) List~List~Zeitintervall~~
        +createKIZeitintervalleForTagessummeFromKIPredictionResults(results, intervalle) List~Zeitintervall~
        +mergeKiHochrechnungInGesamt(intervalle, kiIntervalle)
    }

    ZeitintervallPersistierungsService --> HochrechnungsService
    ZeitintervallPersistierungsService ..> ZeitintervallKIUtil
    HochrechnungsService --> OnnxModelRegistry
    OnnxModelRegistry --> Hochrechnungsmodell
    OnnxModelRegistry --> OnnxModelProperties
    Hochrechnungsmodell <|.. OnnxHochrechnungsmodell
    OnnxHochrechnungsmodell --> OnnxModelDefinition
    OnnxHochrechnungsmodell --> ModelInputEncoder
    ModelInputEncoder <|.. KontextRadV1Encoder
    ModelInputEncoder <|.. ReineRadwerteV1Encoder
    OnnxModelProperties --> OnnxModelDefinition
    ModelInputEncoder --> Zaehldauer
    Hochrechnungsmodell --> KIPredictionResult
    ZeitintervallKIUtil --> KIPredictionResult
```

## Modellkonfiguration

Die Konfiguration ersetzt den einzelnen Property-Wert `dave.onnx.model-path` durch eine Liste von Modellen. Die Konfiguration beschreibt nur Modellauswahl und technische ONNX-Informationen. Zeitfenster und Intervallzahl werden aus `Zaehldauer` bestimmt.

```yaml
dave:
  prediction:
    models:
      - id: rad-2x4h-v1
        version: "1"
        ziel: RAD
        zaehldauer: DAUER_2_X_4_STUNDEN
        resource-path: model/Rad_Modell_DAVE_2x4h.onnx
        input-tensor-name: int64_input
        input-schema: RAW_RAD_COUNTS_V1

      - id: rad-13h-v1
        version: "1"
        ziel: RAD
        zaehldauer: DAUER_13_STUNDEN
        resource-path: model/Rad_Modell_DAVE_13h.onnx
        input-tensor-name: int64_input
        input-schema: RAW_RAD_COUNTS_V1

      - id: rad-16h-v1
        version: "1"
        ziel: RAD
        zaehldauer: DAUER_16_STUNDEN
        resource-path: model/Rad_Modell_DAVE_16h.onnx
        input-tensor-name: int64_input
        input-schema: RAW_RAD_COUNTS_V1
```

`input-schema` ist bewusst kein Boolean. Neue Eingabeformate werden durch einen eigenen `ModelInputEncoder` ergaenzt. Damit kann ein kuenftiges Modell beispielsweise andere Fahrzeugtypen oder weitere Merkmale nutzen, ohne die ONNX-Laufzeit oder die Modellselektion anzupassen.

`Hochrechnungsmodell` ist die bewusst kleine Modellabstraktion. Sie ermoeglicht Tests mit einem Fake-Modell ohne ONNX-Artefakt und entkoppelt die fachliche Auswahl und Ergebnisverarbeitung von der ONNX-Laufzeit. Fuer Teil 1 gibt es genau eine Implementierung, `OnnxHochrechnungsmodell`; weitere technische Modelltypen werden erst bei konkretem Bedarf ergaenzt.

Die Konfiguration der beiden neuen Modelle ist bereits vorhanden. Bis die Artefakte unter den konfigurierten Pfaden bereitgestellt werden, markiert die `OnnxModellRegistry` sie als nicht verfuegbar. Zaehlungen dieser Dauer werden dann ohne KI-Radhochrechnung persistiert.

## Umsetzungsschritte: Teil 1

1. Modelle ablegen und konfigurieren.

   Die Artefakte `Rad_Modell_DAVE_13h.onnx` und `Rad_Modell_DAVE_16h.onnx` werden unter `src/main/resources/model/` abgelegt. `application.yml` erhaelt die beschriebene Modellliste. Eine `@ConfigurationProperties`-Klasse validiert eindeutige Modell-IDs sowie die Kombination aus Zielgroesse und Zaehlungsdauer.

2. Zaehlungsdauerbasierte Eingabezeitfenster zentralisieren.

   Eine Methode wie `Zaehldauer#getInputZeitbloeckeForPrediction()` oder ein kleiner, zentraler Selektor liefert genau die Erhebungsfenster. Sie gibt fuer 2x4h `ZB_06_10` und `ZB_15_19`, fuer 13h `ZB_06_19` und fuer 16h `ZB_06_22` zurueck. Die erwartete Intervallzahl wird aus `Zaehldauer#getAnzahlZeitintervalle()` bezogen.

3. Eingabe-Encoder einfuehren.

   `ModelInputEncoder` selektiert die Intervalle mit den zentralen Zeitfenstern, sortiert sie aufsteigend nach `sortingIndex`, prueft die erwartete Anzahl und erstellt den `long[][]`-Tensorinhalt. `KontextRadV1Encoder` kapselt die bestehende Logik mit `KIZeitintervallMapper`. `ReineRadwerteV1Encoder` liest ausschliesslich `Zeitintervall.getFahrradfahrer()` und erzeugt eine Zeile mit 52 beziehungsweise 64 Werten je Bewegungsbeziehung. Die Verarbeitung vermeidet mehrfach verschachtelte Streams.

4. ONNX-Laufzeit modularisieren.

   Die technische Session-Verwaltung aus `KIService` wird nach `OnnxHochrechnungsmodell` ueberfuehrt. Ein Modell besitzt eine eigene ONNX-Session. Die `OnnxModellRegistry` initialisiert alle konfigurierten Modelle und markiert bei einem Initialisierungsfehler nur das betroffene Modell als nicht verfuegbar. Der Start der Anwendung bleibt moeglich.

5. Modellselektion und Orchestrierung einfuehren.

   `HochrechnungsService` ermittelt ueber `OnnxModelRegistry` die aktiven Modelle fuer die Zaehlungsdauer, gruppiert die Zeitintervalle nach Bewegungsbeziehung und fuehrt die Modelle aus. In Teil 1 existiert jeweils hoechstens ein Radmodell pro Zaehlungsdauer. Das Design erlaubt dennoch mehrere, nicht ueberlappende Zielgroessen.

6. Ergebnisverarbeitung aus `ZeitintervallKIUtil` herausloesen.

   Die ONNX-Ausgabe wird zunaechst weiterhin als `KIPredictionResult` verarbeitet. `ZeitintervallKIUtil` erzeugt je Bewegungsbeziehung das bestehende `GESAMT_KI`-Intervall, setzt `fahrradfahrer` sowie `hochrechnung.hochrechnungRad` und uebertraegt den Wert in das regulare `GESAMT`-Intervall. Das heutige fachliche Ergebnis bleibt dadurch kompatibel.

7. Persistierungsservice anbinden.

   `ZeitintervallPersistierungsService` ruft statt `KIService` direkt den `HochrechnungsService` auf. Die bisherige feste Aktivierung fuer 2x4h, 13h und 16h wird durch die Verfuegbarkeit eines passenden Modells in der Registry ersetzt. Bei Inferenzfehlern wird die konventionelle Aufbereitung weiterhin gespeichert.

8. Fehlerbehandlung und Protokollierung umsetzen.

   Fehlermeldungen enthalten mindestens Modell-ID, Zaehlungs-ID, Zaehlungsdauer und Ursache. Ein Fehler bei der 13h-Inferenz darf weder das 2x4h- noch das 16h-Modell noch die restliche Aufbereitung beeinflussen. Nicht verfuegbare Modelle werden beim Start und bei der Verwendung klar geloggt.

9. Alle notwendigen Tests implementieren.

   Alle in diesem Plan beschriebenen Unit- und Integrationstests muessen mit der Umsetzung implementiert werden; sie sind kein optionaler Nachbereitungsschritt. Unit-Tests pruefen die Auswahl der Zeitfenster, Sortierung, Dimensionspruefung, den `ReineRadwerteV1Encoder` fuer 52 und 64 Werte sowie die Auswahl des korrekten Modells. Der `HochrechnungsService` wird zusaetzlich mit einem Fake-`Hochrechnungsmodell` ohne ONNX-Artefakt getestet. Tests fuer den bestehenden 2x4h-Pfad sichern dessen unveraendertes Verhalten. Integrationstests mit kleinen ONNX-Testmodellen pruefen die vollstaendige 13h- und 16h-Inferenz bis zur Persistierung des `GESAMT_KI`- und `GESAMT`-Werts. Zusaetzlich wird getestet, dass ein defektes Modell nur seine eigene Zaehlungsdauer ohne KI-Ergebnis laesst. Neue Testmethoden folgen dem Muster `test_With...`.

   Stand der Umsetzung: Die Encoder-, Modellselektions-, Fehler- und Persistenzpfade fuer 13h und 16h sind getestet. Echte ONNX-Integrationstests fuer diese beiden Dauern werden ergaenzt, sobald die entsprechenden ONNX-Artefakte oder kleine Testartefakte bereitgestellt sind; sie fehlen derzeit im Repository.

10. Rollout und Dokumentation.

    Zuerst wird die Infrastruktur mit dem bisherigen 2x4h-Modell migriert. Danach werden die beiden neuen Artefakte und Konfigurationseintraege aktiviert. Eine automatische Nachberechnung bereits abgeschlossener Zaehlungen wird nicht implementiert.

## Teil 2: Hochrechnung einzelner Fahrzeugtypen

Teil 2 erweitert die Ergebniswerte um Pkw, Lkw, Lastzuege, Busse und Kraftraeder. Dieser Teil ist bewusst nicht Bestandteil der aktuellen Umsetzung.

### Ziel

`Hochrechnung` wird um diese Felder erweitert:

```java
private BigDecimal hochrechnungPkw;
private BigDecimal hochrechnungLkw;
private BigDecimal hochrechnungLastzuege;
private BigDecimal hochrechnungBusse;
private BigDecimal hochrechnungKraftraeder;
```

Kfz, SV und GV sollen danach nicht zusaetzlich und unabhaengig vorhergesagt werden, sondern aus den hochgerechneten Fahrzeugtypen abgeleitet werden:

```text
GV  = Lkw + Lastzuege
SV  = Lkw + Lastzuege + Busse
Kfz = Pkw + Lkw + Lastzuege + Busse + Kraftraeder
```

### Vorbereitete Erweiterungspunkte

- Ein Ergebnisobjekt wie `Hochrechnungswerte` kann um die fuenf Fahrzeugtypen erweitert werden.
- Ein `Kfz`-Modell erhaelt einen eigenen `ModelInputEncoder`, falls sein Eingabeformat vom reinen Radwertschema abweicht.
- Die Ergebnisverarbeitung wird aus `ZeitintervallKIUtil` in einen eigenen Service ueberfuehrt und fasst dann mehrere Modelle je Bewegungsbeziehung zusammen, bevor ein einziges fachliches `GESAMT_KI`-Intervall persistiert wird.
- Eine separate Herkunfts- und Ergebnistabelle sollte Modell-ID, Modellversion, Zielgroesse, Bewegungsbeziehung, Zeitpunkt und Ergebniswert speichern. Damit bleibt nachvollziehbar, welches Modell welchen Teilwert beigesteuert hat.
- Eine Flyway-Migration erweitert die Datenbankspalten fuer die neuen Hochrechnungswerte.
- `LadeZaehldatumTageswertDTO` und `LadeZaehldatenService` muessen die neuen Tageswerte an die API ausliefern. Das Frontend muss Tabellen, Diagramme und Exporte entsprechend erweitern.

Ohne eigene Fahrzeugtypmodelle duerfen die Typwerte nicht aus einem Gesamt-Kfz-Wert mit impliziten Anteilen abgeleitet werden. Das waere eine weitere fachliche Schaetzung und erfordert explizit definierte, belastbare Verteilungsfaktoren.
