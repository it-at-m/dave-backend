package de.muenchen.dave.services;

import de.muenchen.dave.geodateneai.gen.api.MesswerteMessquerschnittApi;
import de.muenchen.dave.geodateneai.gen.model.GetMesswerteMessquerschnittRequest;
import de.muenchen.dave.geodateneai.gen.model.MesswerteIntervallMessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MesswerteTagesaggregatMessquerschnittDto;
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
public class MesswerteMessquerschnittService {

    private final MesswerteMessquerschnittApi messwerteMessquerschnittApi;

    /**
     * Diese Methode ruft die angefragten MesswerteIntervalle eines Messquerschnitts aus der
     * Geodaten-Eai ab.
     *
     * @param messstelleId Id der angefragten Messstelle
     * @param von required Zeitpunkt der Daten.
     * @param bis optional Ende eines Zeitraums.
     * @param tagesTyp Typ des Tages
     * @return Liste der gefundenen MesswerteIntervalle pro Tag
     */
    public List<MesswerteIntervallMessquerschnittDto> ladeMesswerteIntervall(final long messstelleId, final String von, final String bis,
            final String tagesTyp) {
        final GetMesswerteMessquerschnittRequest request = new GetMesswerteMessquerschnittRequest();
        request.setMessstelleId(messstelleId);
        request.setTagesTyp(GetMesswerteMessquerschnittRequest.TagesTypEnum.fromValue(tagesTyp));
        request.setZeitpunktStart(LocalDate.parse(von));
        request.setZeitpunktEnde(LocalDate.parse(bis));
        Mono<ResponseEntity<List<MesswerteIntervallMessquerschnittDto>>> messwerteIntervallWithHttpInfo = messwerteMessquerschnittApi
                .getMesswerteIntervallWithHttpInfo(request);
        final List<MesswerteIntervallMessquerschnittDto> body = messwerteIntervallWithHttpInfo.block().getBody();
        // Mappen auf Struktur für Charts

        // Charts laden
        return body;

    }

    /**
     * Diese Methode ruft die angefragten MesswerteTagesaggregat eines Messquerschnitts aus der
     * Geodaten-Eai ab.
     *
     * @param messstelleId Id der angefragten Messstelle
     * @param von required Zeitpunkt der Daten.
     * @param bis optional Ende eines Zeitraums.
     * @param tagesTyp Typ des Tages
     * @return Liste der gefundenen MesswerteTagesaggregat pro Tag
     */
    public List<MesswerteTagesaggregatMessquerschnittDto> ladeMesswerteTagesaggregat(final long messstelleId, final String von, final String bis,
            final String tagesTyp) {
        final GetMesswerteMessquerschnittRequest request = new GetMesswerteMessquerschnittRequest();
        request.setMessstelleId(messstelleId);
        request.setTagesTyp(GetMesswerteMessquerschnittRequest.TagesTypEnum.fromValue(tagesTyp));
        request.setZeitpunktStart(LocalDate.parse(von));
        request.setZeitpunktEnde(LocalDate.parse(bis));
        Mono<ResponseEntity<List<MesswerteTagesaggregatMessquerschnittDto>>> messwerteTagesaggregatWithHttpInfo = messwerteMessquerschnittApi
                .getMesswerteTagesaggregatWithHttpInfo(request);
        final List<MesswerteTagesaggregatMessquerschnittDto> body = messwerteTagesaggregatWithHttpInfo.block().getBody();
        // Mappen auf Struktur für Charts

        // Charts laden
        return body;

    }
}
