package de.muenchen.dave.spring.services.csvgenerator;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.csv.CsvMetaObject;
import de.muenchen.dave.domain.dtos.CsvDTO;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Quelle;
import de.muenchen.dave.domain.enums.Wetter;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.GenerateCsvService;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * In dieser Testfile werden einige Objekte mithilfe eines JSON-Strings erstellt, welcher in der
 * Produktion im Debug Mode generiert wurde.
 * Um ein aktuelles JSON zu bekommen, im zu testenden Code an einer entsprechenden Stelle an der das
 * gewünschte Objekt existiert
 * einen Breakpoint setzen, den Test hier debuggen und dort im Evaluator folgenden Code ausführen:
 * <p>
 * Gson gson = new Gson();
 * gson.toJson(variablenNameGewuenschtesObjekt);
 * <p>
 * Resultierenden String dann hier an die entsprechende Stelle (gson.fromJson) kopieren.
 */
@SpringBootTest(
        classes = { DaveBackendApplication.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE"}
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
public class GenerateCsvServiceSpringTest {

    public static final String MOCKABLE_ZAEHLUNG_ID = "6837e615-ea6e-4e42-9c6f-f9aadde6599f";

    @Autowired
    private GenerateCsvService generateCsvService;

    @MockBean
    private LadeZaehldatenService ladeZaehldatenService;

    @MockBean
    private ZaehlstelleIndexService indexService;

    private static Zaehlung getZaehlung() {
        Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(MOCKABLE_ZAEHLUNG_ID);
        zaehlung.setDatum(LocalDate.of(2020, 11, 4));
        zaehlung.setJahr("2020");
        zaehlung.setMonat("November");
        zaehlung.setJahreszeit("Herbst");
        zaehlung.setZaehlart("K");
        zaehlung.setPunkt(null);
        zaehlung.setTagesTyp(null);
        zaehlung.setProjektNummer("M3213");
        zaehlung.setProjektName("VZ Testinger");
        zaehlung.setSonderzaehlung(false);
        zaehlung.setKategorien(null);
        zaehlung.setKreisverkehr(false);
        zaehlung.setStatus("ACTIVE");
        zaehlung.setZaehlsituation("Situation normal");
        zaehlung.setZaehlsituationErweitert("Alles in bester Ordnung");
        zaehlung.setZaehlIntervall(15);
        zaehlung.setWetter(Wetter.CONTINUOUS_RAINY.toString());
        zaehlung.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN.toString());
        zaehlung.setQuelle(Quelle.MANUALLY.toString());
        zaehlung.setSchulZeiten(null);
        zaehlung.setSuchwoerter(null);
        zaehlung.setFahrbeziehungen(Lists.newArrayList());
        zaehlung.setKreuzungsname("Leopoldstr. - Feilitzschstr. - Herzogstr.");

        Knotenarm knotenarm1 = new Knotenarm();
        knotenarm1.setNummer(1);
        knotenarm1.setStrassenname("Cosimastr.");
        Knotenarm knotenarm2 = new Knotenarm();
        knotenarm2.setNummer(3);
        knotenarm2.setStrassenname("Cosimastr");
        Knotenarm knotenarm3 = new Knotenarm();
        knotenarm3.setNummer(4);
        knotenarm3.setStrassenname("Wahnfriedallee");
        List<Knotenarm> knotenarmList = new ArrayList<>();
        knotenarmList.add(knotenarm1);
        knotenarmList.add(knotenarm2);
        knotenarmList.add(knotenarm3);

        zaehlung.setKnotenarme(knotenarmList);

        zaehlung.setPkwEinheit(null);

        return zaehlung;
    }

    private static Zaehlstelle getZaehlstelle(Zaehlung zaehlung) {
        Zaehlstelle zaehlstelle = new Zaehlstelle();
        zaehlstelle.setId("abcd-ef12-3456");
        zaehlstelle.setNummer("133301");
        zaehlstelle.setStadtbezirk("Bogenhausen");
        zaehlstelle.setStadtbezirkNummer(13);
        zaehlstelle.setLetzteZaehlungMonatNummer(11);
        zaehlstelle.setLetzteZaehlungMonat("November");
        zaehlstelle.setLetzteZaehlungJahr(2020);
        zaehlstelle.setGrundLetzteZaehlung("Musste mal wieder gemacht werden");

        List<Zaehlung> zaehlungen = new ArrayList<>();
        zaehlungen.add(zaehlung);
        zaehlstelle.setZaehlungen(zaehlungen);

        return zaehlstelle;
    }

    private static OptionsDTO getOptionsDTO() {
        OptionsDTO optionsDTO = new OptionsDTO();
        optionsDTO.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        optionsDTO.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        optionsDTO.setZeitblock(Zeitblock.ZB_00_06);
        optionsDTO.setZeitauswahl(Zeitauswahl.TAGESWERT.getCapitalizedName());
        optionsDTO.setKraftfahrzeugverkehr(true);
        optionsDTO.setSchwerverkehr(true);
        optionsDTO.setGueterverkehr(true);
        optionsDTO.setRadverkehr(true);
        optionsDTO.setFussverkehr(false);
        optionsDTO.setSchwerverkehrsanteilProzent(true);
        optionsDTO.setGueterverkehrsanteilProzent(true);
        optionsDTO.setPkwEinheiten(true);
        optionsDTO.setPersonenkraftwagen(true);
        optionsDTO.setLastkraftwagen(true);
        optionsDTO.setLastzuege(true);
        optionsDTO.setBusse(true);
        optionsDTO.setKraftraeder(true);
        optionsDTO.setStundensumme(true);
        optionsDTO.setBlocksumme(true);
        optionsDTO.setTagessumme(true);
        optionsDTO.setSpitzenstunde(true);
        optionsDTO.setMittelwert(false);
        optionsDTO.setFahrzeugklassenStapeln(false);
        optionsDTO.setBeschriftung(false);
        optionsDTO.setDatentabelle(false);
        optionsDTO.setWerteHundertRunden(true);
        optionsDTO.setDifferenzdatenDarstellen(false);
        optionsDTO.setVergleichszaehlungsId(null);
        optionsDTO.setVonKnotenarm(null);
        optionsDTO.setNachKnotenarm(null);

        return optionsDTO;
    }

    private static LadeZaehldatenTableDTO getLadeZaehldatenTableDTO() {

        /*
         * Nachfolgende JSON wurde aus der Produktion im Debug Mode generiert. Beschreibung siehe JavaDoc
         * von FillBeanServiceSpringTest.
         */
        Gson gson = new Gson();
        LadeZaehldatenTableDTO ladeZaehldatenTableDTO = gson.fromJson(
                "{\"zaehldaten\":[{\"startUhrzeit\":{\"hour\":6,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":6,\"minute\":15,\"second\":0,\"nano\":0},\"pkw\":49,\"lkw\":4,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":3,\"fahrradfahrer\":17,\"pkwEinheiten\":85},{\"startUhrzeit\":{\"hour\":6,\"minute\":15,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":6,\"minute\":30,\"second\":0,\"nano\":0},\"pkw\":83,\"lkw\":8,\"lastzuege\":1,\"busse\":2,\"kraftraeder\":4,\"fahrradfahrer\":22,\"pkwEinheiten\":119},{\"startUhrzeit\":{\"hour\":6,\"minute\":30,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":6,\"minute\":45,\"second\":0,\"nano\":0},\"pkw\":122,\"lkw\":4,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":9,\"fahrradfahrer\":31,\"pkwEinheiten\":165},{\"startUhrzeit\":{\"hour\":6,\"minute\":45,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":7,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":144,\"lkw\":8,\"lastzuege\":1,\"busse\":6,\"kraftraeder\":6,\"fahrradfahrer\":29,\"pkwEinheiten\":197},{\"type\":\"Stunde\",\"startUhrzeit\":{\"hour\":6,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":7,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":398,\"lkw\":24,\"lastzuege\":2,\"busse\":20,\"kraftraeder\":22,\"fahrradfahrer\":99,\"pkwEinheiten\":565},{\"type\":\"Block\",\"startUhrzeit\":{\"hour\":6,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":10,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":3242,\"lkw\":130,\"lastzuege\":8,\"busse\":89,\"kraftraeder\":181,\"fahrradfahrer\":954,\"pkwEinheiten\":4222},{\"type\":\"SpStd\",\"startUhrzeit\":{\"hour\":17,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":18,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":1318,\"lkw\":7,\"lastzuege\":0,\"busse\":27,\"kraftraeder\":99,\"fahrradfahrer\":277,\"fussgaenger\":0,\"pkwEinheiten\":1559},{\"kfz\":16586,\"schwerverkehr\":762,\"gueterverkehr\":403,\"type\":\"Tageswert\",\"startUhrzeit\":{\"hour\":0,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":23,\"minute\":59,\"second\":59,\"nano\":999000000}}]}",
                LadeZaehldatenTableDTO.class);
        return ladeZaehldatenTableDTO;
    }

    private void init() throws DataNotFoundException {
        final Zaehlung zaehlung = getZaehlung();
        final Zaehlstelle zaehlstelle = getZaehlstelle(zaehlung);
        final LadeZaehldatenTableDTO ladeZaehldatenTableDTO = getLadeZaehldatenTableDTO();
        final OptionsDTO options = getOptionsDTO();

        when(indexService.getZaehlstelleByZaehlungId(MOCKABLE_ZAEHLUNG_ID)).thenReturn(zaehlstelle);
        when(indexService.getZaehlung(MOCKABLE_ZAEHLUNG_ID)).thenReturn(zaehlung);
        when(ladeZaehldatenService.ladeZaehldaten(UUID.fromString(MOCKABLE_ZAEHLUNG_ID), options)).thenReturn(ladeZaehldatenTableDTO);
        final CsvMetaObject metaObject = new CsvMetaObject();
        metaObject.setZaehlung(zaehlung);
        metaObject.setZaehlstelle(zaehlstelle);
    }

    @Test
    public void fillDatentabelleCSV() throws DataNotFoundException {
        init();
        final OptionsDTO options = getOptionsDTO();

        final CsvDTO csvDTO = generateCsvService.generateDatentabelleCsv(MOCKABLE_ZAEHLUNG_ID, options);

        final StringBuilder expected = new StringBuilder();
        expected.append("Zählstellennummer;Zählart;Zähldatum;Fahrbeziehung;;;;;;;;;;;;");
        expected.append("\n");
        expected.append("133301;K;04.11.2020;Von: Alle - Nach: Alle;;;;;;;;;;;;");
        expected.append("\n");
        expected.append("von;bis;;Pkw;Lkw;Lz;Bus;Krad;Rad;KFZ;SV;GV;SV%;GV%;PKW-Einheiten;");
        expected.append("\n");
        expected.append("06:00;06:15;;49;4;0;6;3;17;62;10;4;16.1%;6.5%;85;");
        expected.append("\n");
        expected.append("06:15;06:30;;83;8;1;2;4;22;98;11;9;11.2%;9.2%;119;");
        expected.append("\n");
        expected.append("06:30;06:45;;122;4;0;6;9;31;141;10;4;7.1%;2.8%;165;");
        expected.append("\n");
        expected.append("06:45;07:00;;144;8;1;6;6;29;165;15;9;9.1%;5.5%;197;");
        expected.append("\n");
        expected.append("06:00;07:00;Stunde;398;24;2;20;22;99;466;46;26;9.9%;5.6%;565;");
        expected.append("\n");
        expected.append("06:00;10:00;Block;3242;130;8;89;181;954;3650;227;138;6.2%;3.8%;4222;");
        expected.append("\n");
        expected.append("17:00;18:00;SpStd;1318;7;0;27;99;277;1451;34;7;2.3%;0.5%;1559;");
        expected.append("\n");
        expected.append(";;Tageswert;;;;;;;0;0;0;0%;0%;;");
        expected.append("\n");

        assertThat(csvDTO.getCsvAsString(), is(new String(expected)));

    }
}
