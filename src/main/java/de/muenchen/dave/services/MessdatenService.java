package de.muenchen.dave.services;

import de.muenchen.dave.geodateneai.gen.api.MessdatenApi;
import de.muenchen.dave.geodateneai.gen.model.GetMessdatenRequest;
import de.muenchen.dave.geodateneai.gen.model.MessdatenDto;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessdatenService {

    private final MessdatenApi messdatenApi;

    /**
     * Diese Methode ruft die angefragten Messdaten aus der Geodaten-Eai ab.
     *
     * @param messstelleId Id der angefragten Messstelle
     * @param von required Zeitpunkt der Daten.
     * @param bis optional Ende eines Zeitraums.
     * @param tagtyp Typ des Tages
     * @return Liste der gefundenen Messdaten pro Tag
     */
    public List<MessdatenDto> ladeMessdaten(final long messstelleId, final String von, final String bis, final String tagtyp) {
        final GetMessdatenRequest getMessdatenRequest = new GetMessdatenRequest();
        getMessdatenRequest.setMessstelleId(messstelleId);
        getMessdatenRequest.setTagTyp(tagtyp);
        getMessdatenRequest.setZeitpunktStart(LocalDate.parse(von));
        getMessdatenRequest.setZeitpunktEnde(LocalDate.parse(bis));
        Mono<ResponseEntity<List<MessdatenDto>>> messdatenWithHttpInfo = messdatenApi.getMessdatenWithHttpInfo(getMessdatenRequest);
        final List<MessdatenDto> body = messdatenWithHttpInfo.block().getBody();
        // Mappen auf Struktur für Charts

        // Charts laden
        return body;

    }
}
