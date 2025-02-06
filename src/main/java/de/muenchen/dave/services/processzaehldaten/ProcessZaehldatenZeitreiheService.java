package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenZeitreiheDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.ZeitauswahlDTO;
import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.ZeitauswahlService;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.services.pdfgenerator.FillPdfBeanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ProcessZaehldatenZeitreiheService {

    private static final String FAHRBEZIEHUNG_NICHT_VORHANDEN = "\n(Fahrbez. nicht vorh.)";
    private final ZeitintervallRepository zeitintervallRepository;
    private final ZaehlstelleIndexService indexService;
    private final ZeitauswahlService zeitauswahlService;

    public ProcessZaehldatenZeitreiheService(final ZaehlstelleIndexService indexService,
            final ZeitintervallRepository zeitintervallRepository,
            final ZeitauswahlService zeitauswahlService) {
        this.zeitauswahlService = zeitauswahlService;
        this.indexService = indexService;
        this.zeitintervallRepository = zeitintervallRepository;
    }

    /**
     * Hier wird überprüft, ob die mitgegebene Zählung die in den mitgegebenen Optionen ausgewählte
     * Fahrbeziehung besitzt
     *
     * @param zaehlung Zählung die überprüft werden soll
     * @param options
     * @return
     */
    private static boolean checkFahrbeziehungen(Zaehlung zaehlung, OptionsDTO options) {
        List<Fahrbeziehung> fahrbeziehungList;
        if (zaehlung.getKreisverkehr()) {
            // Bei Kreisverkehr: Prüfe auf Knotenarm
            fahrbeziehungList = zaehlung.getFahrbeziehungen()
                    .stream()
                    .filter(fahrbeziehung -> fahrbeziehung.getKnotenarm() == options.getVonKnotenarm() || options.getVonKnotenarm() == null)
                    .collect(Collectors.toList());
        } else {
            // Bei Kreuzung: Prüfe auf Von und Nach
            fahrbeziehungList = zaehlung.getFahrbeziehungen()
                    .stream()
                    .filter(fahrbeziehung -> fahrbeziehung.getVon() == options.getVonKnotenarm() || options.getVonKnotenarm() == null)
                    .filter(fahrbeziehung -> fahrbeziehung.getNach() == options.getNachKnotenarm() || options.getNachKnotenarm() == null)
                    .collect(Collectors.toList());
        }
        return fahrbeziehungList.size() > 0;
    }

    /**
     * Befüllt das übergebene ladeZaehldatenZeitreiheDTO Objekt mit den Zeitreihe-Daten
     *
     * @param options Optionen aus dem Frontend
     * @param ladeZaehldatenZeitreiheDTO Objekt, das befüllt werden soll
     * @param ladeZaehldatumDTO Objekt mit den Werten
     */
    static void fillLadeZaehldatenZeitreiheDTO(final OptionsDTO options, final LadeZaehldatenZeitreiheDTO ladeZaehldatenZeitreiheDTO,
            final LadeZaehldatumDTO ladeZaehldatumDTO) {
        if (options.getKraftfahrzeugverkehr()) {
            ladeZaehldatenZeitreiheDTO.getKfz().add(ladeZaehldatumDTO.getKfz());
        }
        if (options.getSchwerverkehr()) {
            ladeZaehldatenZeitreiheDTO.getSv().add(ladeZaehldatumDTO.getSchwerverkehr());
        }
        if (options.getGueterverkehr()) {
            ladeZaehldatenZeitreiheDTO.getGv().add(ladeZaehldatumDTO.getGueterverkehr());
        }
        if (options.getFussverkehr()) {
            ladeZaehldatenZeitreiheDTO.getFuss().add(ladeZaehldatumDTO.getFussgaenger());
        }
        if (options.getRadverkehr()) {
            ladeZaehldatenZeitreiheDTO.getRad().add(ladeZaehldatumDTO.getFahrradfahrer());
        }
        if (options.getSchwerverkehrsanteilProzent()) {
            ladeZaehldatenZeitreiheDTO.getSvAnteilInProzent().add(ladeZaehldatumDTO.getAnteilSchwerverkehrAnKfzProzent());
        }
        if (options.getGueterverkehrsanteilProzent()) {
            ladeZaehldatenZeitreiheDTO.getGvAnteilInProzent().add(ladeZaehldatumDTO.getAnteilGueterverkehrAnKfzProzent());
        }
        if (options.getZeitreiheGesamt()) {
            ladeZaehldatenZeitreiheDTO.getGesamt()
                    .add(calculateGesamt(ladeZaehldatumDTO.getKfz(), ladeZaehldatumDTO.getFussgaenger(), ladeZaehldatumDTO.getFahrradfahrer()));
        }
    }

    /**
     * Ermittelt einen Gesamtwert auf KFZ, Fussgänger und Fahrradfahrer und gibt diesen zurück
     *
     * @param kfz Wert für Kraftfahrzeuge
     * @param fussgaenger Wert für Fussgänger
     * @param fahrradfahrer Wert für Fahrradfahrer
     * @return Summe aus KFZ, Fussgänger und Fahrradfahrer als BigDecimal
     */
    static BigDecimal calculateGesamt(final BigDecimal kfz, final Integer fussgaenger, final Integer fahrradfahrer) {
        BigDecimal gesamt = new BigDecimal(0);
        gesamt = gesamt.add(kfz);
        if (fussgaenger != null) {
            gesamt = gesamt.add(BigDecimal.valueOf(fussgaenger));
        }
        if (fahrradfahrer != null) {
            gesamt = gesamt.add(BigDecimal.valueOf(fahrradfahrer));
        }
        return gesamt;
    }

    /**
     * Berechnet das "älteste Datum" nach dem gesucht werden soll. Wenn in den Optionen keine
     * Vergleichszählung gewählt wurde, wird das Datum der drittletzten
     * Zählung (ab der aktuellen) zurückgegeben, sofern vorhanden. Ansonsten das Datum der zweitletzten
     * Zählung bzw. wenn auch diese nicht vorhanden, dann der
     * aktuellen Zählung.
     *
     * @param zaehlstelle Im Frontend gewählte Zählstelle
     * @param currentDate Datum der aktuell im Frontend gewählten Zählung
     * @param options Optionen aus dem Frontend
     * @return
     */
    static LocalDate calculateOldestDate(final Zaehlstelle zaehlstelle, final LocalDate currentDate, final OptionsDTO options) {
        LocalDate oldestDate;

        if (options.getIdVergleichszaehlungZeitreihe() != null) {
            final Zaehlung oldestZaehlung = zaehlstelle.getZaehlungen().stream()
                    .filter(zaehlung -> options.getIdVergleichszaehlungZeitreihe().equals(zaehlung.getId()))
                    .findFirst()
                    .get();
            oldestDate = oldestZaehlung.getDatum();
        } else {
            // Es wurde kein Datum ausgewählt, wir versuchen die letzten drei Zählungen zu laden

            // Liste an Zählungen sortieren (alt nach neu)
            List<Zaehlung> zaehlungen = zaehlstelle.getZaehlungen();
            zaehlungen.sort(Comparator.comparing(Zaehlung::getDatum));

            // Aktuell ausgewählte Zählung laden, index innerhalb der sortierten Liste bestimmen und mit 2 subtrahieren
            // um den Index der drittletzten Zählung zu erhalten
            final Zaehlung currentZaehlung = zaehlstelle.getZaehlungen().stream()
                    .filter(zaehlung -> currentDate.equals(zaehlung.getDatum()))
                    .findFirst()
                    .get();
            final List<Zaehlung> filteredZaehlungen = zaehlungen.stream().filter(zaehlung -> zaehlung.getZaehlart().equals(currentZaehlung.getZaehlart()))
                    .toList();
            final int minIndex = filteredZaehlungen.indexOf(currentZaehlung) - 2;

            if (minIndex < 0) {
                // Wenn minIndex < 0 dann soll das Datum des ersten Elements genommen werden
                oldestDate = filteredZaehlungen.get(0).getDatum();
            } else {
                // Ansonsten nimm das Datum des Elements mit minIndex
                oldestDate = filteredZaehlungen.get(minIndex).getDatum();
            }
        }

        return oldestDate;
    }

    /**
     * Lädt die Daten für eine Zeitreihe und gibt diese zurück
     *
     * @param zaehlstelleId Die ID der im Frontend ausgewählten Zählstelle
     * @param currentZaehlungId Die ID der im Frontend ausgewählten Zählung
     * @param options Optionen aus dem Frontend
     * @return Zeitreihendaten als LadeZaehldatenZeitreiheDTO
     * @throws DataNotFoundException wenn keine Zaehlstelle/Zaehlung geladen werden konnte
     */
    @Cacheable(value = CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, key = "{#p0, #p1, #p2}")
    public LadeZaehldatenZeitreiheDTO getZeitreiheDTO(final String zaehlstelleId, final String currentZaehlungId, final OptionsDTO options)
            throws DataNotFoundException {
        log.debug(String.format("Zugriff auf #getZeitreiheDTO mit %s, %s und %s", zaehlstelleId, currentZaehlungId, options.toString()));
        final Zaehlstelle zaehlstelle = indexService.getZaehlstelle(zaehlstelleId);
        final Zaehlung currentZaehlung = indexService.getZaehlung(currentZaehlungId);

        final LadeZaehldatenZeitreiheDTO ladeZaehldatenZeitreiheDTO = new LadeZaehldatenZeitreiheDTO();

        final ZeitauswahlDTO zeitauswahlDTO = zeitauswahlService.determinePossibleZeitauswahl(currentZaehlung.getZaehldauer(), currentZaehlung.getId());

        getFilteredAndSortedZaehlungenForZeitreihe(zaehlstelle, currentZaehlung, options, zeitauswahlDTO)
                .forEach(zaehlung -> {
                    if (checkFahrbeziehungen(zaehlung, options)) {
                        // Setzen der Zähldauer anhand der aktuellen Zählung nötig, da es ansonsten zu einem Fehler kommt wenn die Basiszählung,
                        // auf der die Optionen basieren, eine 24-Std.-Zählung ist, diese allerdings mit 2x4-Std.-Zählungen verglichen wird
                        options.setZaehldauer(Zaehldauer.valueOf(zaehlung.getZaehldauer()));

                        final Zeitintervall zeitintervall = zeitintervallRepository
                                .findByZaehlungIdAndTypeAndFahrbeziehungVonAndFahrbeziehungNachAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndFahrbeziehungFahrbewegungKreisverkehrIsNull(
                                        UUID.fromString(zaehlung.getId()),
                                        options.getZeitblock().getTypeZeitintervall(),
                                        options.getVonKnotenarm(),
                                        options.getNachKnotenarm(),
                                        options.getZeitblock().getStart(),
                                        options.getZeitblock().getEnd());

                        final LadeZaehldatumDTO ladeZaehldatumDTO = LadeZaehldatenService.mapToZaehldatum(zeitintervall, zaehlung.getPkwEinheit(), options);
                        ladeZaehldatenZeitreiheDTO.getDatum().add(zaehlung.getDatum().format(FillPdfBeanService.DDMMYYYY));

                        fillLadeZaehldatenZeitreiheDTO(options, ladeZaehldatenZeitreiheDTO, ladeZaehldatumDTO);
                    } else {
                        final LadeZaehldatumDTO ladeZaehldatumDTO = new LadeZaehldatumDTO();
                        ladeZaehldatumDTO.setPkw(0);
                        ladeZaehldatumDTO.setLkw(0);
                        ladeZaehldatumDTO.setLastzuege(0);
                        ladeZaehldatumDTO.setBusse(0);
                        ladeZaehldatumDTO.setKraftraeder(0);
                        ladeZaehldatumDTO.setFahrradfahrer(0);
                        ladeZaehldatumDTO.setFussgaenger(0);
                        ladeZaehldatumDTO.setPkwEinheiten(0);

                        ladeZaehldatenZeitreiheDTO.getDatum().add(zaehlung.getDatum().format(FillPdfBeanService.DDMMYYYY) + FAHRBEZIEHUNG_NICHT_VORHANDEN);
                        fillLadeZaehldatenZeitreiheDTO(options, ladeZaehldatenZeitreiheDTO, ladeZaehldatumDTO);
                    }
                });
        return ladeZaehldatenZeitreiheDTO;
    }

    /**
     * Filtert die Zaehlungen aus der Zaehlstelle heraus, die in der Zeitreihe angezeigt werden sollen
     * und gibt diese als Stream zurück. Die Zaehlungen werden
     * anhand der aktuellen Zählung und der in den Optionen gewählten Vergleichszählung gefiltert.
     * Gewählte Zählungen müssen alle der folgenden Kriterien
     * erfüllen:
     * - Zähldatum muss zwischen dem Datum der Basiszählung und der Vergleichszählung liegen (inkl.)
     * - Zählart muss identisch sein
     * - Gewählter Zeitblock muss in beiden Zählungen vorhanden sein
     * - Wenn Basiszählung eine Sonderzählung müssen andere Zählungen ebenfalls Sonderzählungen sein
     *
     * @param zaehlstelle Zählstelle mit allen Zählungen
     * @param currentZaehlung Im Frontend ausgewählten Zählung
     * @param options Optionen aus dem Frontend
     * @param zeitauswahlDTO ZeitauswahlDTO um für Vergleich von Zeitblock in Zählung
     * @return Stream der gefilterten Zählungen
     */
    public Stream<Zaehlung> getFilteredAndSortedZaehlungenForZeitreihe(final Zaehlstelle zaehlstelle,
            final Zaehlung currentZaehlung,
            final OptionsDTO options,
            final ZeitauswahlDTO zeitauswahlDTO) {
        final LocalDate currentDate = currentZaehlung.getDatum();
        final LocalDate oldestDateToSearchFor = calculateOldestDate(zaehlstelle, currentDate, options);

        // Zählungen sortieren und nach Datum filtern (Zählung muss nach oder am OldestDate und vor oder am CurrentDate stattgefunden haben)
        return zaehlstelle.getZaehlungen()
                .stream()
                .sorted(Comparator.comparing(Zaehlung::getDatum))
                .filter(zaehlung -> zaehlung.getStatus().equalsIgnoreCase(Status.ACTIVE.name()))
                .filter(zaehlung -> zaehlung.getDatum().isAfter(oldestDateToSearchFor) || zaehlung.getDatum().isEqual(oldestDateToSearchFor))
                .filter(zaehlung -> zaehlung.getDatum().isBefore(currentDate) || zaehlung.getDatum().isEqual(currentDate))
                .filter(zaehlung -> zaehlung.getZaehlart().equals(currentZaehlung.getZaehlart()))
                .filter(zaehlung -> zaehlung.getSonderzaehlung().equals(currentZaehlung.getSonderzaehlung()))
                .filter(zaehlung -> this.zeitauswahlService.determinePossibleZeitauswahl(
                        zaehlung.getZaehldauer(),
                        zaehlung.getId()).getBlocks().contains(options.getZeitblock()) ||
                        this.zeitauswahlService.determinePossibleZeitauswahl(
                                zaehlung.getZaehldauer(),
                                zaehlung.getId()).getHours().contains(options.getZeitblock())
                        ||
                        options.getZeitblock().equals(Zeitblock.ZB_00_24));
    }
}
