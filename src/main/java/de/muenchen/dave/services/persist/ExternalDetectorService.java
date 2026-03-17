package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Hochrechnungsfaktor;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.external.DetectionDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.mapper.DetectorMapper;
import de.muenchen.dave.domain.mapper.HochrechnungsfaktorMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.HochrechnungsfaktorRepository;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalDetectorService {

    private final DetectorMapper detectorMapper;

    private final HochrechnungsfaktorMapper hochrechnungsfaktorMapper;

    private final HochrechnungsfaktorRepository hochrechnungsfaktorRepository;

    private final ZeitintervallRepository zeitintervallRepository;

    private final ZaehlstelleIndexService zaehlstelleIndexService;

    public ExternalDetectorService(final DetectorMapper detectorMapper,
            final HochrechnungsfaktorMapper hochrechnungsfaktorMapper,
            final HochrechnungsfaktorRepository hochrechnungsfaktorRepository,
            final ZeitintervallRepository zeitintervallRepository,
            final ZaehlstelleIndexService zaehlstelleIndexService) {

        this.detectorMapper = detectorMapper;
        this.hochrechnungsfaktorMapper = hochrechnungsfaktorMapper;
        this.hochrechnungsfaktorRepository = hochrechnungsfaktorRepository;
        this.zeitintervallRepository = zeitintervallRepository;
        this.zaehlstelleIndexService = zaehlstelleIndexService;
    }

    @Transactional
    public BackendIdDTO saveDetection(DetectionDTO detection) throws BrokenInfrastructureException, DataNotFoundException {
        log.debug("saveDetection");
        Zeitintervall zi = detectorMapper.detectionDTO2Bean(detection);

        Hochrechnungsfaktor hochrechnungsfaktor = hochrechnungsfaktorRepository.findByDefaultFaktorTrue();
        if (hochrechnungsfaktor == null) {
            log.error("Kein Standard-Hochrechnungsfaktor in der Datenbank gefunden.");
            throw new BrokenInfrastructureException();
        }
        zi.setHochrechnung(createHochrechnung(hochrechnungsfaktor, zi));
        log.debug("Converted Zeitintervall: {}", zi);

        zeitintervallRepository.save(zi);
        log.debug("Der Messpunkt wurde erfolgreich in der Datenbank gespeichert.");

        final BackendIdDTO backendIdDto = new BackendIdDTO();
        backendIdDto.setId(detection.getZaehlungId().toString());
        return backendIdDto;
    }

    private Hochrechnung createHochrechnung(Hochrechnungsfaktor hochrechnungsfaktor, Zeitintervall zi) {
        HochrechnungsfaktorDTO hochrechnungsfaktorDto = hochrechnungsfaktorMapper.bean2Dto(hochrechnungsfaktor);

        final LadeZaehldatumDTO ladeZaehldatumDTO = new LadeZaehldatumDTO();
        ladeZaehldatumDTO.setPkw(zi.getPkw());
        ladeZaehldatumDTO.setLkw(zi.getLkw());
        ladeZaehldatumDTO.setLastzuege(zi.getLastzuege());
        ladeZaehldatumDTO.setBusse(zi.getBusse());
        ladeZaehldatumDTO.setKraftraeder(zi.getKraftraeder());
        ladeZaehldatumDTO.setFahrradfahrer(zi.getFahrradfahrer());
        ladeZaehldatumDTO.setFussgaenger(zi.getFussgaenger());

        final Hochrechnung hochrechnung = new Hochrechnung();
        hochrechnung.setFaktorKfz(BigDecimal.valueOf(hochrechnungsfaktorDto.getKfz()));
        hochrechnung.setFaktorSv(BigDecimal.valueOf(hochrechnungsfaktorDto.getSv()));
        hochrechnung.setFaktorGv(BigDecimal.valueOf(hochrechnungsfaktorDto.getGv()));
        hochrechnung.setHochrechnungKfz(ladeZaehldatumDTO.getKfz().multiply(hochrechnung.getFaktorKfz()));
        hochrechnung.setHochrechnungGv(ladeZaehldatumDTO.getGueterverkehr().multiply(hochrechnung.getFaktorGv()));
        hochrechnung.setHochrechnungSv(ladeZaehldatumDTO.getSchwerverkehr().multiply(hochrechnung.getFaktorSv()));
        return hochrechnung;
    }

    private List<DetectionDTO> addMissingKnotenarme(List<DetectionDTO> detections) throws DataNotFoundException {
        if (detections.isEmpty()) {
            return null;
        }
        DetectionDTO detection = detections.get(0);
        UUID zaehlungId = detection.getZaehlungId();
        LocalDateTime start = detection.getStartUhrzeit();
        LocalDateTime ende = detection.getEndeUhrzeit();

        List<DetectionDTO> zeroDetectionsToAdd = new ArrayList<>();

        Zaehlung zaehlung = zaehlstelleIndexService.getZaehlung(detection.getZaehlungId().toString());
        zaehlung.getFahrbeziehungen().forEach(f -> {
            int von = f.getVon();
            int nach = f.getNach();
            if (!detectionHasDataForFahrbeziehung(detections, von, nach)) {
                log.debug("Detection enthält keine Daten für die Fahrbeziehung von {} nach {}, füge diese mit Zählwert 0 hinzu", von, nach);
                DetectionDTO d = new DetectionDTO();
                d.setStartUhrzeit(start);
                d.setEndeUhrzeit(ende);
                d.setZaehlungId(zaehlungId);
                d.setVon(von);
                d.setNach(nach);
                d.setBusse(0);
                d.setFussgaenger(0);
                d.setFahrradfahrer(0);
                d.setKraftraeder(0);
                d.setLastzuege(0);
                d.setLkw(0);
                d.setPkw(0);
                zeroDetectionsToAdd.add(d);
            }
        });
        detections.addAll(zeroDetectionsToAdd);
        return detections;
    }

    private boolean detectionHasDataForFahrbeziehung(List<DetectionDTO> detections, Integer von, Integer nach) {
        return detections.stream()
                .anyMatch(d -> d.getVon() == von && d.getNach() == nach);
    }

    public BackendIdDTO saveLatestDetections(List<DetectionDTO> detections) throws BrokenInfrastructureException, DataNotFoundException {
        log.debug("saveLatestDetections");
        //get all Knotenarme for Zaehlung and set counts to zero for all arms that are not included in the detection
        detections = addMissingKnotenarme(detections);

        BackendIdDTO backendIdDto = new BackendIdDTO();
        for (DetectionDTO detection : detections) {
            backendIdDto = saveDetection(detection);
        }
        return backendIdDto;
    }

}
