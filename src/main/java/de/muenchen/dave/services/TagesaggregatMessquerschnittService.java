package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.domain.mapper.TagesaggregatMessquerschnittMapper;
import de.muenchen.dave.geodateneai.gen.api.TagesaggregatMessquerschnittApi;
import de.muenchen.dave.geodateneai.gen.model.NichtPlausibleTageDto;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TagesaggregatMessquerschnittService {
    private final TagesaggregatMessquerschnittMapper tagesaggregatMessquerschnittMapper;
    private final TagesaggregatMessquerschnittApi tagesaggregatMessquerschnittApi;
    @Value("${geodaten.eai.url:}")
    public String geodatenEaiUrl;

    public NichtPlausibleTageResponseDTO getNichtPlausibleDatenFromEai(String messquerschnittId) {
        NichtPlausibleTageDto eaiRequestResult = Objects
                .requireNonNull(tagesaggregatMessquerschnittApi.getNichtPlausibleTageWithHttpInfo(messquerschnittId).block()).getBody();
        return tagesaggregatMessquerschnittMapper.requestToResponse(eaiRequestResult);
    }
}
