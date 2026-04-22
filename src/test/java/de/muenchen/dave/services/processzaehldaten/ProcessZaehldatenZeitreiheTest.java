package de.muenchen.dave.services.processzaehldaten;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.OptionsLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.OptionsQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.OptionsVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenZeitreiheDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.*;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.Zaehlart;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ProcessZaehldatenZeitreiheTest {

    private static final LocalDate DATE1 = LocalDate.of(2011, 11, 4);
    private static final LocalDate DATE2 = LocalDate.of(2016, 8, 8);
    private static final LocalDate DATE3 = LocalDate.of(2020, 4, 3);
    private static final LocalDate DATE4 = LocalDate.of(2024, 1, 11);
    private static final String ID1 = "1234";
    private static final String ID2 = "5678";
    private static final String ID3 = "90ab";
    private static final String ID4 = "42cd";

    private static Zaehlstelle getZaehlstelleWithZaehlungen() {

        Zaehlung zaehlung1 = new Zaehlung();
        zaehlung1.setId(ID1);
        zaehlung1.setDatum(DATE1);
        zaehlung1.setZaehlart(Zaehlart.N.name());

        Zaehlung zaehlung2 = new Zaehlung();
        zaehlung2.setId(ID2);
        zaehlung2.setDatum(DATE2);
        zaehlung2.setZaehlart(Zaehlart.N.name());

        Zaehlung zaehlung3 = new Zaehlung();
        zaehlung3.setId(ID3);
        zaehlung3.setDatum(DATE3);
        zaehlung3.setZaehlart(Zaehlart.R.name());

        Zaehlung zaehlung4 = new Zaehlung();
        zaehlung4.setId(ID4);
        zaehlung4.setDatum(DATE4);
        zaehlung4.setZaehlart(Zaehlart.N.name());

        List<Zaehlung> zaehlungList = new ArrayList<>();
        zaehlungList.add(zaehlung1);
        zaehlungList.add(zaehlung2);
        zaehlungList.add(zaehlung3);
        zaehlungList.add(zaehlung4);

        Zaehlstelle zaehlstelle = new Zaehlstelle();
        zaehlstelle.setZaehlungen(zaehlungList);

        return zaehlstelle;
    }

    @Test
    public void calculateOldestDate() {
        Zaehlstelle zaehlstelle = getZaehlstelleWithZaehlungen();
        OptionsDTO options = new OptionsDTO();

        LocalDate currentDate = DATE4;
        assertThat(ProcessZaehldatenZeitreiheService.calculateOldestDate(zaehlstelle, currentDate, options),
                is(DATE1));

        options.setIdVergleichszaehlungZeitreihe(ID2);
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

    @Test
    public void checkVerkehrsbeziehungenQU() {
        // setup
        Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.QU.name());
        Querungsverkehr qv1 = new Querungsverkehr();
        qv1.setKnotenarm(1);
        qv1.setRichtung(Himmelsrichtung.N);
        Querungsverkehr qv2 = new Querungsverkehr();
        qv2.setKnotenarm(1);
        qv2.setRichtung(Himmelsrichtung.S);
        zaehlung.setQuerungsverkehr(List.of(qv1, qv2));

        // positive cases
        OptionsDTO options = new OptionsDTO();
        options.setChosenQuerungsverkehre(List.of(new OptionsQuerungsverkehrDTO() {
            {
                setKnotenarm(1);
                setRichtung(Himmelsrichtung.N);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(true));

        options.setChosenQuerungsverkehre(List.of(new OptionsQuerungsverkehrDTO() {
            {
                setKnotenarm(1);
                setRichtung(Himmelsrichtung.N);
            }
        }, new OptionsQuerungsverkehrDTO() {
            {
                setKnotenarm(1);
                setRichtung(Himmelsrichtung.S);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(true));

        // negative cases
        options.setChosenQuerungsverkehre(List.of(new OptionsQuerungsverkehrDTO() {
            {
                setKnotenarm(2);
                setRichtung(Himmelsrichtung.N);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(false));

        // negative cases
        options.setChosenQuerungsverkehre(List.of(new OptionsQuerungsverkehrDTO() {
            {
                setKnotenarm(1);
                setRichtung(Himmelsrichtung.N);
            }
        }, new OptionsQuerungsverkehrDTO() {
            {
                setKnotenarm(2);
                setRichtung(Himmelsrichtung.S);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(false));

        options.setChosenQuerungsverkehre(List.of(new OptionsQuerungsverkehrDTO() {
            {
                setKnotenarm(1);
                setRichtung(Himmelsrichtung.W);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(false));
    }

    @Test
    public void checkVerkehrsbeziehungenFJS() {
        // setup
        Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.FJS.name());
        Laengsverkehr l1 = new Laengsverkehr();
        l1.setKnotenarm(1);
        l1.setRichtung(Bewegungsrichtung.EIN);
        l1.setStrassenseite(Himmelsrichtung.N);
        Laengsverkehr l2 = new Laengsverkehr();
        l2.setKnotenarm(1);
        l2.setRichtung(Bewegungsrichtung.AUS);
        l2.setStrassenseite(Himmelsrichtung.S);
        zaehlung.setLaengsverkehr(List.of(l1, l2));

        // positive cases
        OptionsDTO options = new OptionsDTO();
        options.setChosenLaengsverkehre(List.of(new OptionsLaengsverkehrDTO() {
            {
                setKnotenarm(1);
                setRichtung(Bewegungsrichtung.EIN);
                setStrassenseite(Himmelsrichtung.N);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(true));

        options.setChosenLaengsverkehre(List.of(new OptionsLaengsverkehrDTO() {
            {
                setKnotenarm(1);
                setRichtung(Bewegungsrichtung.EIN);
                setStrassenseite(Himmelsrichtung.N);
            }
        }, new OptionsLaengsverkehrDTO() {
            {
                setKnotenarm(1);
                setRichtung(Bewegungsrichtung.AUS);
                setStrassenseite(Himmelsrichtung.S);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(true));

        // negative cases
        options.setChosenLaengsverkehre(List.of(new OptionsLaengsverkehrDTO() {
            {
                setKnotenarm(2);
                setRichtung(Bewegungsrichtung.EIN);
                setStrassenseite(Himmelsrichtung.N);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(false));

        options.setChosenLaengsverkehre(List.of(new OptionsLaengsverkehrDTO() {
            {
                setKnotenarm(1);
                setRichtung(Bewegungsrichtung.AUS);
                setStrassenseite(Himmelsrichtung.N);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(false));

        options.setChosenLaengsverkehre(List.of(new OptionsLaengsverkehrDTO() {
            {
                setKnotenarm(1);
                setRichtung(Bewegungsrichtung.EIN);
                setStrassenseite(Himmelsrichtung.S);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(false));

    }

    @Test
    public void checkVerkehrsbeziehungenQJS() {
        // setup
        Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.QJS.name());
        Verkehrsbeziehung vb1 = new Verkehrsbeziehung();
        vb1.setVon(1);
        vb1.setNach(3);
        vb1.setStrassenseite(Himmelsrichtung.N);
        Verkehrsbeziehung vb2 = new Verkehrsbeziehung();
        vb2.setVon(3);
        vb2.setNach(1);
        vb2.setStrassenseite(Himmelsrichtung.N);
        zaehlung.setVerkehrsbeziehungen(List.of(vb1, vb2));

        // positive cases
        OptionsDTO options = new OptionsDTO();
        options.setChosenVerkehrsbeziehungen(List.of(new OptionsVerkehrsbeziehungDTO() {
            {
                setVon(1);
                setNach(3);
                setStrassenseite(Himmelsrichtung.N);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(true));

        options.setChosenVerkehrsbeziehungen(List.of(new OptionsVerkehrsbeziehungDTO() {
            {
                setVon(1);
                setNach(3);
                setStrassenseite(Himmelsrichtung.N);
            }
        }, new OptionsVerkehrsbeziehungDTO() {
            {
                setVon(3);
                setNach(1);
                setStrassenseite(Himmelsrichtung.N);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(true));

        // negative cases
        options.setChosenVerkehrsbeziehungen(List.of(new OptionsVerkehrsbeziehungDTO() {
            {
                setVon(1);
                setNach(3);
                setStrassenseite(Himmelsrichtung.S);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(false));

        options.setChosenVerkehrsbeziehungen(List.of(new OptionsVerkehrsbeziehungDTO() {
            {
                setVon(3);
                setNach(3);
                setStrassenseite(Himmelsrichtung.N);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(false));

        options.setChosenVerkehrsbeziehungen(List.of(new OptionsVerkehrsbeziehungDTO() {
            {
                setVon(1);
                setNach(1);
                setStrassenseite(Himmelsrichtung.N);
            }
        }));
        assertThat(ProcessZaehldatenZeitreiheService.checkVerkehrsbeziehungen(zaehlung, options), is(false));
    }
}
