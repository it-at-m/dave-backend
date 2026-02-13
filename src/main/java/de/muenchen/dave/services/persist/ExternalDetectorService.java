package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Hochrechnungsfaktor;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.external.DetectionDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.mapper.DetectorMapper;
import de.muenchen.dave.domain.mapper.HochrechnungsfaktorMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.HochrechnungsfaktorRepository;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalDetectorService {

    private final DetectorMapper detectorMapper;

    private final HochrechnungsfaktorMapper hochrechnungsfaktorMapper;

    private final HochrechnungsfaktorRepository hochrechnungsfaktorRepository;

    private final ZeitintervallRepository zeitintervallRepository;

    public ExternalDetectorService(final DetectorMapper detectorMapper,
            final HochrechnungsfaktorMapper hochrechnungsfaktorMapper,
            final HochrechnungsfaktorRepository hochrechnungsfaktorRepository,
            final ZeitintervallRepository zeitintervallRepository) {

        this.detectorMapper = detectorMapper;
        this.hochrechnungsfaktorMapper = hochrechnungsfaktorMapper;
        this.hochrechnungsfaktorRepository = hochrechnungsfaktorRepository;
        this.zeitintervallRepository = zeitintervallRepository;
    }

    @Transactional
    public BackendIdDTO saveDetection(DetectionDTO detection) throws BrokenInfrastructureException, DataNotFoundException {
        log.debug("saveDetection");
        Zeitintervall zi = detectorMapper.detectionDTO2Bean(detection);

        Hochrechnungsfaktor hochrechnungsfaktor = hochrechnungsfaktorRepository.findByDefaultFaktorTrue();
        if (hochrechnungsfaktor == null) {
            final String errorMessage = String.format(
                    "Kein Standard-Hochrechnungsfaktor in der Datenbank gefunden. " +
                    "Aufruf: hochrechnungsfaktorRepository.findByDefaultFaktorTrue(), ZaehlungId: %s",
                    detection != null ? detection.getZaehlungId() : "null");
            log.error(errorMessage);
            throw new BrokenInfrastructureException(errorMessage);
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

    public BackendIdDTO saveLatestDetections(List<DetectionDTO> detections) throws BrokenInfrastructureException, DataNotFoundException {
        log.debug("saveLatestDetections");
        BackendIdDTO backendIdDto = new BackendIdDTO();
        for (DetectionDTO detection : detections) {
            backendIdDto = saveDetection(detection);
        }
        return backendIdDto;
    }

}
