package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenZeitreiheDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ProcessZaehldatenZeitreiheTest {

    private static final LocalDate DATE1 = LocalDate.of(2011, 11, 4);
    private static final LocalDate DATE2 = LocalDate.of(2016, 8, 8);
    private static final LocalDate DATE3 = LocalDate.of(2020, 4, 3);
    private static final String ID1 = "1234";
    private static final String ID2 = "5678";
    private static final String ID3 = "90ab";

    private static Zaehlstelle getZaehlstelleWithZaehlungen() {

        Zaehlung zaehlung1 = new Zaehlung();
        zaehlung1.setId(ID1);
        zaehlung1.setDatum(DATE1);

        Zaehlung zaehlung2 = new Zaehlung();
        zaehlung2.setId(ID2);
        zaehlung2.setDatum(DATE2);

        Zaehlung zaehlung3 = new Zaehlung();
        zaehlung3.setId(ID3);
        zaehlung3.setDatum(DATE3);

        List<Zaehlung> zaehlungList = new ArrayList<>();
        zaehlungList.add(zaehlung1);
        zaehlungList.add(zaehlung2);
        zaehlungList.add(zaehlung3);

        Zaehlstelle zaehlstelle = new Zaehlstelle();
        zaehlstelle.setZaehlungen(zaehlungList);

        return zaehlstelle;
    }

    @Test
    public void calculateOldestDate() {
        Zaehlstelle zaehlstelle = getZaehlstelleWithZaehlungen();
        OptionsDTO options = new OptionsDTO();

        LocalDate currentDate = DATE3;
        assertThat(ProcessZaehldatenZeitreiheService.calculateOldestDate(zaehlstelle, currentDate, options),
                is(DATE1));

        options.setIdVergleichszaehlungZeitreihe(ID2);
        currentDate = DATE3;
        assertThat(ProcessZaehldatenZeitreiheService.calculateOldestDate(zaehlstelle, currentDate, options),
                is(DATE2));

        options.setIdVergleichszaehlungZeitreihe(null);
        currentDate = DATE2;
        assertThat(ProcessZaehldatenZeitreiheService.calculateOldestDate(zaehlstelle, currentDate, options),
                is(DATE1));
    }

    @Test
    public void fillLadeZaehldatenZeitreiheDTO() {
        final OptionsDTO options = new OptionsDTO();
        options.setKraftfahrzeugverkehr(true);
        options.setSchwerverkehr(true);
        options.setGueterverkehr(true);
        options.setFussverkehr(true);
        options.setRadverkehr(true);
        options.setSchwerverkehrsanteilProzent(true);
        options.setGueterverkehrsanteilProzent(true);
        options.setZeitreiheGesamt(true);

        final LadeZaehldatumDTO ladeZaehldatumDTO = new LadeZaehldatumDTO();
        ladeZaehldatumDTO.setPkw(100);
        ladeZaehldatumDTO.setLkw(50);
        ladeZaehldatumDTO.setLastzuege(20);
        ladeZaehldatumDTO.setBusse(5);
        ladeZaehldatumDTO.setKraftraeder(25);
        ladeZaehldatumDTO.setFahrradfahrer(40);
        ladeZaehldatumDTO.setFussgaenger(45);
        ladeZaehldatumDTO.setPkwEinheiten(100);

        final LadeZaehldatenZeitreiheDTO ladeZaehldatenZeitreiheDTO1 = new LadeZaehldatenZeitreiheDTO();
        ProcessZaehldatenZeitreiheService.fillLadeZaehldatenZeitreiheDTO(options, ladeZaehldatenZeitreiheDTO1,
                ladeZaehldatumDTO);
        assertThat(ladeZaehldatenZeitreiheDTO1.getKfz().get(0), is(new BigDecimal(200)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getSv().get(0), is(new BigDecimal(75)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getGv().get(0), is(new BigDecimal(70)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getFuss().get(0), is(45));
        assertThat(ladeZaehldatenZeitreiheDTO1.getRad().get(0), is(40));
        assertThat(ladeZaehldatenZeitreiheDTO1.getGesamt().get(0), is(new BigDecimal(285)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getSvAnteilInProzent().get(0), is(BigDecimal.valueOf(37.5)));
        assertThat(ladeZaehldatenZeitreiheDTO1.getGvAnteilInProzent().get(0), is(BigDecimal.valueOf(35.0)));

        options.setKraftfahrzeugverkehr(false);
        options.setSchwerverkehr(false);
        options.setGueterverkehr(false);
        options.setFussverkehr(false);
        options.setRadverkehr(false);
        options.setSchwerverkehrsanteilProzent(false);

        final LadeZaehldatenZeitreiheDTO ladeZaehldatenZeitreiheDTO2 = new LadeZaehldatenZeitreiheDTO();
        ProcessZaehldatenZeitreiheService.fillLadeZaehldatenZeitreiheDTO(options, ladeZaehldatenZeitreiheDTO2,
                ladeZaehldatumDTO);
        assertThat(ladeZaehldatenZeitreiheDTO2.getKfz().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getSv().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getGv().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getFuss().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getRad().size(), is(0));
        assertThat(ladeZaehldatenZeitreiheDTO2.getGesamt().get(0), is(new BigDecimal(285)));
        assertThat(ladeZaehldatenZeitreiheDTO2.getSvAnteilInProzent().size(), is(0));
    }

    @Test
    public void calculateGesamt() {
        BigDecimal kfz = new BigDecimal(1000);
        Integer fussgaenger = 100;
        Integer fahrradfahrer = 300;

        assertThat(ProcessZaehldatenZeitreiheService.calculateGesamt(kfz, fussgaenger, fahrradfahrer), is(new BigDecimal(1400)));

        fussgaenger = null;
        fahrradfahrer = null;
        assertThat(ProcessZaehldatenZeitreiheService.calculateGesamt(kfz, fussgaenger, fahrradfahrer), is(new BigDecimal(1000)));
    }

}
