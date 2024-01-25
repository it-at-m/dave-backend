//package de.muenchen.dave.services;
//
//import de.muenchen.dave.geodateneai.gen.api.MesswerteMessquerschnittApi;
//import de.muenchen.dave.geodateneai.gen.model.GetMesswerteIntervallMessquerschnittResponse;
//import de.muenchen.dave.geodateneai.gen.model.GetMesswerteOfMessquerschnittRequest;
//import de.muenchen.dave.geodateneai.gen.model.GetMesswerteTagesaggregatMessquerschnittResponse;
//import java.time.LocalDate;
//import java.util.Objects;
//import java.util.Set;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class MesswerteMessquerschnittService {
//
//    private final MesswerteMessquerschnittApi messwerteMessquerschnittApi;
//
//    /**
//     * Diese Methode ruft die angefragten MesswerteIntervalle eines Messquerschnitts aus der
//     * Geodaten-Eai ab.
//     *
//     * @param messstelleId Id der angefragten Messstelle
//     * @param von required Zeitpunkt der Daten.
//     * @param bis optional Ende eines Zeitraums.
//     * @param tagesTyp Typ des Tages
//     * @return Liste der gefundenen MesswerteIntervalle pro Tag
//     */
//    public GetMesswerteIntervallMessquerschnittResponse ladeMesswerteIntervall(final String messstelleId, final String von, final String bis,
//            final String tagesTyp) {
//        final GetMesswerteOfMessquerschnittRequest request = new GetMesswerteOfMessquerschnittRequest();
//        // Anhand der MesstellenId die entsprechenden MessquerschnittIds ermitteln
//        request.setMessquerschnittIds(Set.of(messstelleId, 123L));
//        request.setTagesTyp(GetMesswerteOfMessquerschnittRequest.TagesTypEnum.fromValue(tagesTyp));
//        request.setZeitpunktStart(LocalDate.parse(von));
//        request.setZeitpunktEnde(LocalDate.parse(bis));
//        final Mono<ResponseEntity<GetMesswerteIntervallMessquerschnittResponse>> messwerteIntervallWithHttpInfo = messwerteMessquerschnittApi
//                .getMesswerteIntervallWithHttpInfo(
//                        request);
//        return Objects.requireNonNull(messwerteIntervallWithHttpInfo.block()).getBody();
//
//    }
//
//    /**
//     * Diese Methode ruft die angefragten MesswerteTagesaggregat eines Messquerschnitts aus der
//     * Geodaten-Eai ab.
//     *
//     * @param messstelleId Id der angefragten Messstelle
//     * @param von required Zeitpunkt der Daten.
//     * @param bis optional Ende eines Zeitraums.
//     * @param tagesTyp Typ des Tages
//     * @return Liste der gefundenen MesswerteTagesaggregat pro Tag
//     */
//    public GetMesswerteTagesaggregatMessquerschnittResponse ladeMesswerteTagesaggregat(final long messstelleId, final String von, final String bis,
//            final String tagesTyp) {
//        final GetMesswerteOfMessquerschnittRequest request = new GetMesswerteOfMessquerschnittRequest();
//        // Anhand der MesstellenId die entsprechenden MessquerschnittIds ermitteln
//        request.setMessquerschnittIds(Set.of(messstelleId, 963L));
//        request.setTagesTyp(GetMesswerteOfMessquerschnittRequest.TagesTypEnum.fromValue(tagesTyp));
//        request.setZeitpunktStart(LocalDate.parse(von));
//        request.setZeitpunktEnde(LocalDate.parse(bis));
//        final Mono<ResponseEntity<GetMesswerteTagesaggregatMessquerschnittResponse>> messwerteTagesaggregatWithHttpInfo = messwerteMessquerschnittApi
//                .getMesswerteTagesaggregatWithHttpInfo(request);
//        return Objects.requireNonNull(messwerteTagesaggregatWithHttpInfo.block()).getBody();
//
//    }
//}
