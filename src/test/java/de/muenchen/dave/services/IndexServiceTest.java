package de.muenchen.dave.services;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Status;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class IndexServiceTest {

    private IndexService service = new IndexService(
            null,
            null,
            null,
            null,
            null,
            null
    );

    @Test
    public void testUpdateZaehlstelleWithZaehlung() {

        Zaehlstelle zs = new Zaehlstelle();
        zs.setZaehlungen(new ArrayList<>());

        Zaehlung z1 = new Zaehlung();
        z1.setDatum(LocalDate.of(2010, 1, 1));
        z1.setJahr("2010");
        z1.setStatus(Status.ACTIVE.name());

        Zaehlung z2 = new Zaehlung();
        z2.setDatum(LocalDate.of(2015, 5, 1));
        z2.setJahr("2015");
        z2.setStatus(Status.ACTIVE.name());

        Zaehlung z3 = new Zaehlung();
        z3.setDatum(LocalDate.of(2020, 7, 1));
        z3.setJahr("2020");
        z3.setStatus(Status.ACTIVE.name());

        // letzte hinzugefügte Zählung ist älter als die vorhande
        this.service.updateZaehlstelleWithZaehlung(zs, z2);
        this.service.updateZaehlstelleWithZaehlung(zs, z1);
        assertThat(zs.getLetzteZaehlungJahr(), is(equalTo(2015)));
        assertThat(zs.getLetzteZaehlungMonatNummer(), is(equalTo(5)));

        // letztes hinzugefügtes Zählung ist die Jüngste
        this.service.updateZaehlstelleWithZaehlung(zs, z3);
        assertThat(zs.getLetzteZaehlungJahr(), is(equalTo(2020)));
        assertThat(zs.getLetzteZaehlungMonatNummer(), is(equalTo(7)));

    }

}
