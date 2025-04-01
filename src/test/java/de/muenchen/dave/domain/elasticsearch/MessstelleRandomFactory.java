package de.muenchen.dave.domain.elasticsearch;

import com.github.javafaker.Faker;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessstelleRandomFactory {
    private static final Faker fakerInstance = Faker.instance();

    public static Messstelle getMessstelle() {
        Messstelle messstelle = new Messstelle();
        messstelle.setId(UUID.randomUUID().toString());
        messstelle.setMstId(fakerInstance.number().digits(10));
        messstelle.setName(fakerInstance.pokemon().name());
        messstelle.setStatus(MessstelleStatus.IN_BESTAND);
        messstelle.setFahrzeugKlassen(fakerInstance.pokemon().name());
        messstelle.setDetektierteVerkehrsarten("KFZ");
        messstelle.setHersteller(fakerInstance.pokemon().name());
        messstelle.setRealisierungsdatum(
                LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                        .numberBetween(1, 28)));
        messstelle.setAbbaudatum(
                LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                        .numberBetween(1, 28)));
        messstelle.setStadtbezirkNummer(fakerInstance.number().numberBetween(1, 26));
        messstelle.setBemerkung(fakerInstance.pokemon().name());
        messstelle.setDatumLetztePlausibleMessung(
                LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                        .numberBetween(1, 28)));
        messstelle.setPunkt(new GeoPoint(fakerInstance.number().randomDouble(5, 0, 5), fakerInstance.number().randomDouble(5, 0, 5)));
        messstelle.setSichtbarDatenportal(fakerInstance.bool().bool());
        messstelle.setGeprueft(fakerInstance.bool().bool());
        messstelle.setKommentar(fakerInstance.pokemon().name());
        messstelle.setStandort(fakerInstance.pokemon().location());
        final List<String> suchwoerter = new ArrayList<>();
        suchwoerter.add(fakerInstance.company().buzzword());
        suchwoerter.add(fakerInstance.company().buzzword());
        suchwoerter.add(fakerInstance.company().buzzword());
        messstelle.setSuchwoerter(suchwoerter);
        final List<String> customSuchwoerter = new ArrayList<>();
        customSuchwoerter.add(fakerInstance.company().buzzword());
        customSuchwoerter.add(fakerInstance.company().buzzword());
        customSuchwoerter.add(fakerInstance.company().buzzword());
        messstelle.setCustomSuchwoerter(customSuchwoerter);
        messstelle.setMessquerschnitte(MessquerschnittRandomFactory.getSomeMessquerschnitte());
        messstelle.setMessfaehigkeiten(MessfaehigkeitRandomFactory.getSomeMessfaehigkeiten());
        messstelle.setLageplanVorhanden(fakerInstance.bool().bool());
        return messstelle;
    }

    public static MessstelleDto getMessstelleDto() {
        final MessstelleDto dto = new MessstelleDto();
        dto.setMstId(UUID.randomUUID().toString());
        dto.setName(fakerInstance.pokemon().name());
        dto.setStatus(MessstelleDto.StatusEnum.IN_BESTAND);
        dto.setFahrzeugKlassen(fakerInstance.pokemon().name());
        dto.setDetektierteVerkehrsarten("KFZ");
        dto.setHersteller(fakerInstance.pokemon().name());
        dto.setRealisierungsdatum(
                LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                        .numberBetween(1, 28)));
        dto.setAbbaudatum(LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                .numberBetween(1, 28)));
        dto.setStadtbezirkNummer(fakerInstance.number().numberBetween(1, 26));
        dto.setBemerkung(fakerInstance.pokemon().name());
        dto.setDatumLetztePlausibleMessung(
                LocalDate.of(fakerInstance.number().numberBetween(2000, 2020), fakerInstance.number().numberBetween(1, 12), fakerInstance.number()
                        .numberBetween(1, 28)));
        dto.setMessquerschnitte(MessquerschnittRandomFactory.getSomeMessquerschnittDtos());
        dto.setLatitude(fakerInstance.number().randomDouble(5, 0, 5));
        dto.setLongitude(fakerInstance.number().randomDouble(5, 0, 5));
        dto.setMessfaehigkeiten(MessfaehigkeitRandomFactory.getSomeMessfaehigkeitenDtos());
        return dto;
    }

}
