package de.muenchen.dave.services;

import de.muenchen.dave.domain.csv.CsvMetaObject;
import de.muenchen.dave.domain.csv.DatentabelleCsvZaehldatum;
import de.muenchen.dave.domain.dtos.CsvDTO;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.mapper.DatentabelleCsvZaehldatumMapper;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class GenerateCsvService {

    private static final String SEMIKOLON = ";";

    public static final DateTimeFormatter DDMMYYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final LadeZaehldatenService ladeZaehldatenService;

    private final DatentabelleCsvZaehldatumMapper datentabelleCsvZaehldatumMapper;

    private final IndexService indexService;

    public GenerateCsvService(final LadeZaehldatenService ladeZaehldatenService,
                              final DatentabelleCsvZaehldatumMapper datentabelleCsvZaehldatumMapper,
                              final IndexService indexService) {
        this.ladeZaehldatenService = ladeZaehldatenService;
        this.datentabelleCsvZaehldatumMapper = datentabelleCsvZaehldatumMapper;
        this.indexService = indexService;
    }

    /**
     * Erzeugt eine csv-Datei die der Tabelle aus der Oberflaeche entspricht.
     *
     * @param zaehlungId Id der aktuellen Zaehlung
     * @param options    aktuell gesetzen Einstellungen
     * @return CSV als String
     * @throws DataNotFoundException Wenn keine Daten gelesen werden konnten
     */
    public CsvDTO generateDatentabelleCsv(String zaehlungId, OptionsDTO options) throws DataNotFoundException {
        final List<LadeZaehldatumDTO> ladeZaehldatumDTOS = ladeZaehldatenService.ladeZaehldaten(UUID.fromString(zaehlungId), options).getZaehldaten();

        // Bei Tageswert soll keine Uhrzeit angezeigt werden
        ladeZaehldatumDTOS.stream()
                .filter(ladeZaehldatumDTO -> StringUtils.equalsIgnoreCase(ladeZaehldatumDTO.getType(), LadeZaehldatenService.TAGESWERT))
                .forEach(ladeZaehldatumDTO ->
                {
                    ladeZaehldatumDTO.setEndeUhrzeit(null);
                    ladeZaehldatumDTO.setStartUhrzeit(null);
                });

        final List<DatentabelleCsvZaehldatum> data = datentabelleCsvZaehldatumMapper.ladeZaehldatumDTOList2beanList(ladeZaehldatumDTOS);

        final StringBuilder csvBuilder = new StringBuilder();
        final String header = getHeader(options);

        final String metaHeader = getMetaHeader(header);
        final String metaData = getMetaData(getCsvMetaObject(zaehlungId), header, options);
        csvBuilder.append(metaHeader);
        csvBuilder.append("\n");
        csvBuilder.append(metaData);
        csvBuilder.append("\n");
        csvBuilder.append(header);
        csvBuilder.append("\n");
        data.forEach(dat -> {
            csvBuilder.append(getData(options, dat));
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
        metaHeader.append("Zählstellennummer");
        metaHeader.append(SEMIKOLON);
        metaHeader.append("Zählart");
        metaHeader.append(SEMIKOLON);
        metaHeader.append("Zähldatum");
        metaHeader.append(SEMIKOLON);
        metaHeader.append("Fahrbeziehung");

        for (int i = 2; i < neededSemikolons; i++) {
            metaHeader.append(SEMIKOLON);
        }
        return new String(metaHeader);
    }

    /**
     * Erzeugt die Metadaten für die Tabelle
     *
     * @param metaObject enthält die Zählstelle und Zählung
     * @param header     Zur Berechnung der Anzahl der Semikolons
     * @param options    Zur Anzeige der Fahrbeziehung
     * @return Csv-Zeile
     */
    public String getMetaData(final CsvMetaObject metaObject, final String header, final OptionsDTO options) {
        final int neededSemikolons = header.split(SEMIKOLON).length - 1;
        final StringBuilder metaData = new StringBuilder();
        metaData.append(metaObject.getZaehlstelle().getNummer());
        metaData.append(SEMIKOLON);
        metaData.append(metaObject.getZaehlung().getZaehlart());
        metaData.append(SEMIKOLON);
        if (metaObject.getZaehlung().getDatum() != null) {
            metaData.append(metaObject.getZaehlung().getDatum().format(DDMMYYYY));
        } else {
            metaData.append(metaObject.getZaehlung().getDatum());
        }
        metaData.append(SEMIKOLON);
        final StringBuilder fahrbeziehung = new StringBuilder("Von: ");
        if (options.getVonKnotenarm() != null) {
            fahrbeziehung.append(options.getVonKnotenarm());
        } else {
            fahrbeziehung.append("Alle");
        }
        fahrbeziehung.append(" - Nach: ");
        if (options.getNachKnotenarm() != null) {
            fahrbeziehung.append(options.getNachKnotenarm());
        } else {
            fahrbeziehung.append("Alle");
        }

        metaData.append(fahrbeziehung);

        for (int i = 2; i < neededSemikolons; i++) {
            metaData.append(SEMIKOLON);
        }
        return new String(metaData);
    }


    /**
     * Erzeugt für jede Zeile aus der Tabelle eine Zeile fuer das CSV-File.
     *
     * @param options aktuelle Einstellungen
     * @param dataCsv eine Zeile in der Tabelle
     * @return CSV-Zeile
     */
    public String getData(final OptionsDTO options, final DatentabelleCsvZaehldatum dataCsv) {
        final StringBuilder data = new StringBuilder();
        // Zeit
        if (StringUtils.isNotEmpty(dataCsv.getStartUhrzeit())) {
            data.append(dataCsv.getStartUhrzeit());
        }
        data.append(SEMIKOLON);
        if (StringUtils.isNotEmpty(dataCsv.getEndeUhrzeit())) {
            data.append(dataCsv.getEndeUhrzeit());
        }
        data.append(SEMIKOLON);
        if (StringUtils.isNotEmpty(dataCsv.getType())) {
            data.append(dataCsv.getType());
        }
        data.append(SEMIKOLON);
        // Fahrzeugtypen
        if (options.getPersonenkraftwagen()) {
            if (dataCsv.getPkw() != null) {
                data.append(dataCsv.getPkw());
            }
            data.append(SEMIKOLON);
        }
        if (options.getLastkraftwagen()) {
            if (dataCsv.getLkw() != null) {
                data.append(dataCsv.getLkw());
            }
            data.append(SEMIKOLON);
        }
        if (options.getLastzuege()) {
            if (dataCsv.getLastzuege() != null) {
                data.append(dataCsv.getLastzuege());
            }
            data.append(SEMIKOLON);
        }
        if (options.getBusse()) {
            if (dataCsv.getBusse() != null) {
                data.append(dataCsv.getBusse());
            }
            data.append(SEMIKOLON);
        }
        if (options.getKraftraeder()) {
            if (dataCsv.getKraftraeder() != null) {
                data.append(dataCsv.getKraftraeder());
            }
            data.append(SEMIKOLON);
        }
        if (options.getRadverkehr()) {
            if (dataCsv.getFahrradfahrer() != null) {
                data.append(dataCsv.getFahrradfahrer());
            }
            data.append(SEMIKOLON);
        }
        if (options.getFussverkehr()) {
            if (dataCsv.getFussgaenger() != null) {
                data.append(dataCsv.getFussgaenger());
            }
            data.append(SEMIKOLON);
        }
        // Fahrzeugklassen
        if (options.getKraftfahrzeugverkehr()) {
            if (dataCsv.getKfz() != null) {
                data.append(dataCsv.getKfz());
            }
            data.append(SEMIKOLON);
        }
        if (options.getSchwerverkehr()) {
            if (dataCsv.getSchwerverkehr() != null) {
                data.append(dataCsv.getSchwerverkehr());
            }
            data.append(SEMIKOLON);
        }
        if (options.getGueterverkehr()) {
            if (dataCsv.getGueterverkehr() != null) {
                data.append(dataCsv.getGueterverkehr());
            }
            data.append(SEMIKOLON);
        }
        // Anteil
        if (options.getSchwerverkehrsanteilProzent()) {
            if (dataCsv.getAnteilSchwerverkehrAnKfzProzent() != null) {
                data.append(dataCsv.getAnteilSchwerverkehrAnKfzProzent());
                data.append("%");
            }
            data.append(SEMIKOLON);
        }
        if (options.getGueterverkehrsanteilProzent()) {
            if (dataCsv.getAnteilGueterverkehrAnKfzProzent() != null) {
                data.append(dataCsv.getAnteilGueterverkehrAnKfzProzent());
                data.append("%");
            }
            data.append(SEMIKOLON);
        }
        // PKW-Einheiten
        if (options.getPkwEinheiten()) {
            if (dataCsv.getPkwEinheiten() != null) {
                data.append(dataCsv.getPkwEinheiten());
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
    public String getHeader(final OptionsDTO options) {
        final StringBuilder header = new StringBuilder();
        // Zeit
        header.append("von");
        header.append(SEMIKOLON);
        header.append("bis");
        header.append(SEMIKOLON);
        // Kein Headername, aber leer lassen
        header.append(SEMIKOLON);
        // Fahrzeugtypen
        if (options.getPersonenkraftwagen()) {
            header.append("Pkw");
            header.append(SEMIKOLON);
        }
        if (options.getLastkraftwagen()) {
            header.append("Lkw");
            header.append(SEMIKOLON);
        }
        if (options.getLastzuege()) {
            header.append("Lz");
            header.append(SEMIKOLON);
        }
        if (options.getBusse()) {
            header.append("Bus");
            header.append(SEMIKOLON);
        }
        if (options.getKraftraeder()) {
            header.append("Krad");
            header.append(SEMIKOLON);
        }
        if (options.getRadverkehr()) {
            header.append("Rad");
            header.append(SEMIKOLON);
        }
        if (options.getFussverkehr()) {
            header.append("Fuß");
            header.append(SEMIKOLON);
        }
        // Fahrzeugklassen
        if (options.getKraftfahrzeugverkehr()) {
            header.append("KFZ");
            header.append(SEMIKOLON);
        }
        if (options.getSchwerverkehr()) {
            header.append("SV");
            header.append(SEMIKOLON);
        }
        if (options.getGueterverkehr()) {
            header.append("GV");
            header.append(SEMIKOLON);
        }
        // Anteil
        if (options.getSchwerverkehrsanteilProzent()) {
            header.append("SV%");
            header.append(SEMIKOLON);
        }
        if (options.getGueterverkehrsanteilProzent()) {
            header.append("GV%");
            header.append(SEMIKOLON);
        }
        // PKW-Einheiten
        if (options.getPkwEinheiten()) {
            header.append("PKW-Einheiten");
            header.append(SEMIKOLON);
        }
        return new String(header);
    }

    /**
     * Liefert die Zählstelle und die Zählung passend zur Id zurück
     *
     * @param zaehlungId gesucht
     * @return CsvMetaObject mit der Zählstelle und der Zählung
     * @throws DataNotFoundException Wenn keine Daten gelesen werden konnten
     */
    public CsvMetaObject getCsvMetaObject(final String zaehlungId) throws DataNotFoundException {
        final Zaehlstelle zaehlstelle = indexService.getZaehlstelleByZaehlungId(zaehlungId);
        final Zaehlung zaehlung = indexService.getZaehlung(zaehlungId);
        final CsvMetaObject metaObject = new CsvMetaObject();
        metaObject.setZaehlstelle(zaehlstelle);
        metaObject.setZaehlung(zaehlung);
        return metaObject;
    }

}
