package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.CsvDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteListenausgabeDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.exceptions.DataNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenerateCsvMstService {

    public static final DateTimeFormatter DDMMYYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String SEMIKOLON = ";";
    private static final String UHRZEIT_23_59 = "23:59";
    private static final String UHRZEIT_24_00 = "24:00";

    private final MesswerteService messwerteService;
    private final MessstelleService messstelleService;

    /**
     * Erzeugt eine csv-Datei die der Tabelle aus der Oberflaeche entspricht.
     *
     * @param messstelleId Id der aktuellen Messstelle
     * @param options aktuell gesetzen Einstellungen
     * @return CSV als String
     * @throws DataNotFoundException Wenn keine Daten gelesen werden konnten
     */
    public CsvDTO generateDatentabelleCsv(final String messstelleId, MessstelleOptionsDTO options) throws DataNotFoundException {
        final LadeMesswerteListenausgabeDTO zaehldatenTable = messwerteService.ladeMesswerte(messstelleId, options).getZaehldatenTable();
        final List<LadeMesswerteDTO> data = zaehldatenTable.getZaehldaten();
        final ReadMessstelleInfoDTO messstelle = messstelleService.readMessstelleInfo(messstelleId);
        final FahrzeugOptionsDTO fahrzeugOptions = options.getFahrzeuge();

        final StringBuilder csvBuilder = new StringBuilder();
        final String header = getHeader(fahrzeugOptions);

        final String metaHeader = getMetaHeader(header);
        final String metaData = getMetaData(messstelle, header, options);
        csvBuilder.append(metaHeader);
        csvBuilder.append("\n");
        csvBuilder.append(metaData);
        csvBuilder.append("\n");
        csvBuilder.append(header);
        csvBuilder.append("\n");
        data.forEach(dat -> {
            csvBuilder.append(getData(fahrzeugOptions, dat));
            csvBuilder.append("\n");
        });
        final CsvDTO csvAsString = new CsvDTO();
        csvAsString.setCsvAsString(new String(csvBuilder));
        return csvAsString;
    }

    /**
     * Erzeugt die Metaheader für die Tabelle
     *
     * @param header Zur Berechnung der Anzahl der Semikolons
     * @return Csv-Zeile
     */
    public String getMetaHeader(final String header) {
        final int neededSemikolons = header.split(SEMIKOLON).length - 1;

        final StringBuilder metaHeader = new StringBuilder();
        metaHeader.append("ID Messstelle");
        metaHeader.append(SEMIKOLON);
        metaHeader.append("KFZ oder RAD");
        metaHeader.append(SEMIKOLON);
        metaHeader.append("auswählter Messzeitraum / Einzeltag");
        metaHeader.append(SEMIKOLON);
        metaHeader.append("ausgewählter Wochentag");
        metaHeader.append(SEMIKOLON);
        metaHeader.append("ausgewählter MQ (Merkmale \"MQ-ID - Richtung - Standort MQ\") bzw. \"Alle Messquerschnitte\"");

        metaHeader.append(SEMIKOLON.repeat(Math.max(0, neededSemikolons - 3)));
        return new String(metaHeader);
    }

    /**
     * Erzeugt die Metadaten für die Tabelle
     *
     * @param messstelle die Messstelle
     * @param header Zur Berechnung der Anzahl der Semikolons
     * @param options Zur Anzeige der Fahrbeziehung
     * @return Csv-Zeile
     */
    public String getMetaData(final ReadMessstelleInfoDTO messstelle, final String header, final MessstelleOptionsDTO options) {
        final int neededSemikolons = header.split(SEMIKOLON).length - 1;
        final StringBuilder metaData = new StringBuilder();
        metaData.append(messstelle.getMstId());
        metaData.append(SEMIKOLON);
        metaData.append(messstelle.getDetektierteVerkehrsarten());
        metaData.append(SEMIKOLON);
        if (CollectionUtils.isNotEmpty(options.getZeitraum())) {
            metaData.append(options.getZeitraum().get(0).format(DDMMYYYY));
            if (options.getZeitraum().size() == 2) {
                metaData.append(" bis ");
                metaData.append(options.getZeitraum().get(1).format(DDMMYYYY));
            }
        }
        metaData.append(SEMIKOLON);
        if (StringUtils.isNotEmpty(options.getTagesTyp())) {
            metaData.append(options.getTagesTyp());
        }
        metaData.append(SEMIKOLON);
        // ausgewählte mq's
        if (CollectionUtils.isNotEmpty(options.getMessquerschnittIds())) {
            if (options.getMessquerschnittIds().size() == messstelle.getMessquerschnitte().size()) {
                metaData.append("Alle Messquerschnitte");
            } else {
                final List<String> mqData = new ArrayList<>();
                messstelle.getMessquerschnitte().forEach(mq -> {
                    final Set<String> messquerschnittIdsSorted = options.getMessquerschnittIds().stream().sorted().collect(
                            Collectors.toCollection(LinkedHashSet::new));
                    if (messquerschnittIdsSorted.contains(mq.getMqId())) {
                        mqData.add(String.format("%s - %s - %s", mq.getMqId(), mq.getFahrtrichtung(), mq.getStandort()));
                    }
                });
                metaData.append(StringUtils.join(mqData, ", "));
            }
        }
        metaData.append(SEMIKOLON.repeat(Math.max(0, neededSemikolons - 3)));
        return new String(metaData);
    }

    /**
     * Erzeugt für jede Zeile aus der Tabelle eine Zeile fuer das CSV-File.
     *
     * @param options aktuelle Einstellungen
     * @param dataCsv eine Zeile in der Tabelle
     * @return CSV-Zeile
     */
    public String getData(final FahrzeugOptionsDTO options, final LadeMesswerteDTO dataCsv) {
        final StringBuilder data = new StringBuilder();
        // Zeit
        if (ObjectUtils.isNotEmpty(dataCsv.getStartUhrzeit())) {
            data.append(dataCsv.getStartUhrzeit());
        }
        data.append(SEMIKOLON);
        if (ObjectUtils.isNotEmpty(dataCsv.getEndeUhrzeit())) {
            if (StringUtils.equals(dataCsv.getEndeUhrzeit().toString(), UHRZEIT_23_59)) {
                data.append(UHRZEIT_24_00);
            }
            data.append(dataCsv.getEndeUhrzeit());
        }
        data.append(SEMIKOLON);
        if (StringUtils.isNotEmpty(dataCsv.getType())) {
            data.append(dataCsv.getType());
        }
        data.append(SEMIKOLON);
        // Fahrzeugtypen
        if (options.isPersonenkraftwagen()) {
            if (dataCsv.getPkw() != null) {
                data.append(dataCsv.getPkw());
            }
            data.append(SEMIKOLON);
        }
        if (options.isLastkraftwagen()) {
            if (dataCsv.getLkw() != null) {
                data.append(dataCsv.getLkw());
            }
            data.append(SEMIKOLON);
        }
        if (options.isLastzuege()) {
            if (dataCsv.getLastzuege() != null) {
                data.append(dataCsv.getLastzuege());
            }
            data.append(SEMIKOLON);
        }
        if (options.isLieferwagen()) {
            if (dataCsv.getLfw() != null) {
                data.append(dataCsv.getLfw());
            }
            data.append(SEMIKOLON);
        }
        if (options.isBusse()) {
            if (dataCsv.getBusse() != null) {
                data.append(dataCsv.getBusse());
            }
            data.append(SEMIKOLON);
        }
        if (options.isKraftraeder()) {
            if (dataCsv.getKraftraeder() != null) {
                data.append(dataCsv.getKraftraeder());
            }
            data.append(SEMIKOLON);
        }
        if (options.isRadverkehr()) {
            if (dataCsv.getFahrradfahrer() != null) {
                data.append(dataCsv.getFahrradfahrer());
            }
            data.append(SEMIKOLON);
        }
        if (options.isFussverkehr()) {
            if (dataCsv.getFussgaenger() != null) {
                data.append(dataCsv.getFussgaenger());
            }
            data.append(SEMIKOLON);
        }
        // Fahrzeugklassen
        if (options.isKraftfahrzeugverkehr()) {
            if (dataCsv.getKfz() != null) {
                data.append(dataCsv.getKfz());
            }
            data.append(SEMIKOLON);
        }
        if (options.isSchwerverkehr()) {
            if (dataCsv.getSchwerverkehr() != null) {
                data.append(dataCsv.getSchwerverkehr());
            }
            data.append(SEMIKOLON);
        }
        if (options.isGueterverkehr()) {
            if (dataCsv.getGueterverkehr() != null) {
                data.append(dataCsv.getGueterverkehr());
            }
            data.append(SEMIKOLON);
        }
        // Anteil
        if (options.isSchwerverkehrsanteilProzent()) {
            if (dataCsv.getAnteilSchwerverkehrAnKfzProzent() != null) {
                data.append(dataCsv.getAnteilSchwerverkehrAnKfzProzent());
                data.append("%");
            }
            data.append(SEMIKOLON);
        }
        if (options.isGueterverkehrsanteilProzent()) {
            if (dataCsv.getAnteilGueterverkehrAnKfzProzent() != null) {
                data.append(dataCsv.getAnteilGueterverkehrAnKfzProzent());
                data.append("%");
            }
            data.append(SEMIKOLON);
        }
        return new String(data);
    }

    /**
     * Erzeugt anhand der Einstellungen die Headerzeile der CSV-Datei
     *
     * @param options aktuelle Einstellungen
     * @return Header als CSV-Zeile
     */
    public String getHeader(final FahrzeugOptionsDTO options) {
        final StringBuilder header = new StringBuilder();
        // Zeit
        header.append("von");
        header.append(SEMIKOLON);
        header.append("bis");
        header.append(SEMIKOLON);
        // Kein Headername, aber leer lassen
        header.append(SEMIKOLON);
        // Fahrzeugtypen
        if (options.isPersonenkraftwagen()) {
            header.append("Pkw");
            header.append(SEMIKOLON);
        }
        if (options.isLastkraftwagen()) {
            header.append("Lkw");
            header.append(SEMIKOLON);
        }
        if (options.isLastzuege()) {
            header.append("Lz");
            header.append(SEMIKOLON);
        }
        if (options.isLieferwagen()) {
            header.append("Lfw");
            header.append(SEMIKOLON);
        }
        if (options.isBusse()) {
            header.append("Bus");
            header.append(SEMIKOLON);
        }
        if (options.isKraftraeder()) {
            header.append("Krad");
            header.append(SEMIKOLON);
        }
        if (options.isRadverkehr()) {
            header.append("Rad");
            header.append(SEMIKOLON);
        }
        if (options.isFussverkehr()) {
            header.append("Fuß");
            header.append(SEMIKOLON);
        }
        // Fahrzeugklassen
        if (options.isKraftfahrzeugverkehr()) {
            header.append("KFZ");
            header.append(SEMIKOLON);
        }
        if (options.isSchwerverkehr()) {
            header.append("SV");
            header.append(SEMIKOLON);
        }
        if (options.isGueterverkehr()) {
            header.append("GV");
            header.append(SEMIKOLON);
        }
        // Anteil
        if (options.isSchwerverkehrsanteilProzent()) {
            header.append("SV%");
            header.append(SEMIKOLON);
        }
        if (options.isGueterverkehrsanteilProzent()) {
            header.append("GV%");
            header.append(SEMIKOLON);
        }
        return new String(header);
    }
}
