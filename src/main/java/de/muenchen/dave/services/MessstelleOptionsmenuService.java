package de.muenchen.dave.services;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.dtos.messstelle.AuffaelligeTageDTO;
import de.muenchen.dave.domain.dtos.ChosenTageValidResponseDTO;
import de.muenchen.dave.domain.dtos.ChosenTagesTypValidEaiRequestDTO;
import de.muenchen.dave.domain.mapper.MessstelleOptionsmenuMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleOptionsmenuControllerApi;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidDTO;
import de.muenchen.dave.geodateneai.gen.model.ChosenTagesTypValidRequestDto;
import de.muenchen.dave.services.messstelle.UnauffaelligeTageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessstelleOptionsmenuService {
    private final MessstelleOptionsmenuMapper messstelleOptionsmenuMapper;
    private final MessstelleOptionsmenuControllerApi messstelleOptionsmenuControllerApi;
    private final UnauffaelligeTageService unauffaelligeTageService;
    private final KalendertagService kalendertagService;

    public AuffaelligeTageDTO getAuffaelligeTageForMessstelle(final Integer mstId) {
        final List<UnauffaelligerTag> unauffaelligeTageForMessstelle = unauffaelligeTageService.getUnauffaelligeTageForMessstelle(mstId);
        final List<LocalDate> unauffaelligeTage = unauffaelligeTageForMessstelle
                .stream()
                .map(unauffaelligerTag -> unauffaelligerTag.getKalendertag().getDatum())
                .toList();
        final List<Kalendertag> auffaelligeKalendertage = kalendertagService.getAllKalendertageWhereDatumNotInAndDatumIsBefore(
                unauffaelligeTage,
                LocalDate.now());
        final List<LocalDate> auffaelligeTage = auffaelligeKalendertage.stream().map(Kalendertag::getDatum).toList();
        final AuffaelligeTageDTO auffaelligeTageDTO = new AuffaelligeTageDTO();
        auffaelligeTageDTO.setAuffaelligeTage(auffaelligeTage);
        return auffaelligeTageDTO;
    }

    public ChosenTageValidResponseDTO isTagesTypValid(final ChosenTagesTypValidEaiRequestDTO chosenTagesTypValidEaiRequestDTO) {
        final ChosenTagesTypValidRequestDto chosenTagesTypValidRequestDto = messstelleOptionsmenuMapper
                .backendToEaiRequestChosenTageValid(chosenTagesTypValidEaiRequestDTO);
        final ChosenTagesTypValidDTO chosenTagesTypValidDTO = Objects
                .requireNonNull(messstelleOptionsmenuControllerApi.isTagesTypDataValidWithHttpInfo(chosenTagesTypValidRequestDto).block()).getBody();
        return messstelleOptionsmenuMapper.eaiToBackendResponseChosenTageValid(chosenTagesTypValidDTO);
    }
}
