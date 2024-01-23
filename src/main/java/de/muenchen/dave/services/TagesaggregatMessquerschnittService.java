package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.NichtPlausibleTageEaiRequestDTO;
import de.muenchen.dave.domain.dtos.NichtPlausibleTageResponseDTO;
import de.muenchen.dave.domain.mapper.TagesaggregatMessquerschnittMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class TagesaggregatMessquerschnittService {
    private final TagesaggregatMessquerschnittMapper tagesaggregatMessquerschnittMapper;
    @Value("${geodaten.eai.url:}")
    public String geodatenEaiUrl;

    public NichtPlausibleTageResponseDTO getNichtPlausibleDatenFromEai() {
        String requestUrl = geodatenEaiUrl + "/tagesaggregatMessquerschnitt/nichtPlausibleDaten?messquerschnittId=test";
        RestTemplate restTemplate = new RestTemplate();
        NichtPlausibleTageEaiRequestDTO eaiRequestResult = restTemplate.getForObject(requestUrl, NichtPlausibleTageEaiRequestDTO.class);
        return tagesaggregatMessquerschnittMapper.requestToResponse(eaiRequestResult);
    }
}
