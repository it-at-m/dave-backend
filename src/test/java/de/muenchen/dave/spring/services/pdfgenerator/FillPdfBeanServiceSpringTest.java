package de.muenchen.dave.spring.services.pdfgenerator;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Quelle;
import de.muenchen.dave.domain.enums.Wetter;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.pdf.components.ZaehlstelleninformationenPdfComponent;
import de.muenchen.dave.domain.pdf.components.ZusatzinformationenPdfComponent;
import de.muenchen.dave.domain.pdf.templates.DatentabellePdf;
import de.muenchen.dave.domain.pdf.templates.DiagrammPdf;
import de.muenchen.dave.domain.pdf.templates.GangliniePdf;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.services.pdfgenerator.FillPdfBeanService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
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
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
public class FillPdfBeanServiceSpringTest {

    public static final String MOCKABLE_ZAEHLUNG_ID = "6837e615-ea6e-4e42-9c6f-f9aadde6599f";
    public static final String DEPARTMENT = "TestOU";

    @MockBean
    private MessstelleIndex messstelleIndex;

    @MockBean
    private CustomSuggestIndex customSuggestIndex;

    @MockBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @Autowired
    private FillPdfBeanService fillPdfBeanService;

    @MockBean
    private LadeZaehldatenService ladeZaehldatenService;

    @MockBean
    private ZaehlstelleIndexService indexService;

    public static Zaehlung getZaehlung() {
        final Zaehlung zaehlung = new Zaehlung();
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

        final Knotenarm knotenarm1 = new Knotenarm();
        knotenarm1.setNummer(1);
        knotenarm1.setStrassenname("Cosimastr.");
        final Knotenarm knotenarm2 = new Knotenarm();
        knotenarm2.setNummer(3);
        knotenarm2.setStrassenname("Cosimastr");
        final Knotenarm knotenarm3 = new Knotenarm();
        knotenarm3.setNummer(4);
        knotenarm3.setStrassenname("Wahnfriedallee");
        final List<Knotenarm> knotenarmList = new ArrayList<>();
        knotenarmList.add(knotenarm1);
        knotenarmList.add(knotenarm2);
        knotenarmList.add(knotenarm3);

        zaehlung.setKnotenarme(knotenarmList);

        zaehlung.setPkwEinheit(null);

        return zaehlung;
    }

    public static Zaehlstelle getZaehlstelle(final Zaehlung zaehlung) {
        final Zaehlstelle zaehlstelle = new Zaehlstelle();
        zaehlstelle.setId("abcd-ef12-3456");
        zaehlstelle.setNummer("133301");
        zaehlstelle.setStadtbezirk("Bogenhausen");
        zaehlstelle.setStadtbezirkNummer(13);
        zaehlstelle.setLetzteZaehlungMonatNummer(11);
        zaehlstelle.setLetzteZaehlungMonat("November");
        zaehlstelle.setLetzteZaehlungJahr(2020);
        zaehlstelle.setGrundLetzteZaehlung("Musste mal wieder gemacht werden");

        final List<Zaehlung> zaehlungen = new ArrayList<>();
        zaehlungen.add(zaehlung);
        zaehlstelle.setZaehlungen(zaehlungen);

        return zaehlstelle;
    }

    public static OptionsDTO getChosenOptionsDTO() {
        final OptionsDTO optionsDTO = new OptionsDTO();
        optionsDTO.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        optionsDTO.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        optionsDTO.setZeitblock(Zeitblock.ZB_00_06);
        optionsDTO.setZeitauswahl(Zeitauswahl.TAGESWERT.getCapitalizedName());
        optionsDTO.setKraftfahrzeugverkehr(true);
        optionsDTO.setSchwerverkehr(true);
        optionsDTO.setGueterverkehr(true);
        optionsDTO.setRadverkehr(true);
        optionsDTO.setFussverkehr(true);
        optionsDTO.setSchwerverkehrsanteilProzent(true);
        optionsDTO.setGueterverkehrsanteilProzent(true);
        optionsDTO.setPkwEinheiten(true);
        optionsDTO.setPersonenkraftwagen(true);
        optionsDTO.setLastkraftwagen(true);
        optionsDTO.setLastzuege(true);
        optionsDTO.setBusse(true);
        optionsDTO.setKraftraeder(false);
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

    public static LadeZaehldatenTableDTO getLadeZaehldatenTableDTO() {

        /*
         * Nachfolgende JSON wurde aus der Produktion im Debug Mode generiert. Beschreibung siehe JavaDoc
         * von FillBeanServiceSpringTest.
         */
        final Gson gson = new Gson();
        final LadeZaehldatenTableDTO ladeZaehldatenTableDTO = gson.fromJson(
                "{\"zaehldaten\":[{\"startUhrzeit\":{\"hour\":6,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":6,\"minute\":15,\"second\":0,\"nano\":0},\"pkw\":49,\"lkw\":4,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":3,\"fahrradfahrer\":17,\"pkwEinheiten\":85},{\"startUhrzeit\":{\"hour\":6,\"minute\":15,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":6,\"minute\":30,\"second\":0,\"nano\":0},\"pkw\":83,\"lkw\":8,\"lastzuege\":1,\"busse\":2,\"kraftraeder\":4,\"fahrradfahrer\":22,\"pkwEinheiten\":119},{\"startUhrzeit\":{\"hour\":6,\"minute\":30,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":6,\"minute\":45,\"second\":0,\"nano\":0},\"pkw\":122,\"lkw\":4,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":9,\"fahrradfahrer\":31,\"pkwEinheiten\":165},{\"startUhrzeit\":{\"hour\":6,\"minute\":45,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":7,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":144,\"lkw\":8,\"lastzuege\":1,\"busse\":6,\"kraftraeder\":6,\"fahrradfahrer\":29,\"pkwEinheiten\":197},{\"type\":\"Stunde\",\"startUhrzeit\":{\"hour\":6,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":7,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":398,\"lkw\":24,\"lastzuege\":2,\"busse\":20,\"kraftraeder\":22,\"fahrradfahrer\":99,\"pkwEinheiten\":565},{\"startUhrzeit\":{\"hour\":7,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":7,\"minute\":15,\"second\":0,\"nano\":0},\"pkw\":191,\"lkw\":7,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":14,\"fahrradfahrer\":55,\"pkwEinheiten\":250},{\"startUhrzeit\":{\"hour\":7,\"minute\":15,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":7,\"minute\":30,\"second\":0,\"nano\":0},\"pkw\":216,\"lkw\":5,\"lastzuege\":0,\"busse\":3,\"kraftraeder\":14,\"fahrradfahrer\":68,\"pkwEinheiten\":264},{\"startUhrzeit\":{\"hour\":7,\"minute\":30,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":7,\"minute\":45,\"second\":0,\"nano\":0},\"pkw\":234,\"lkw\":8,\"lastzuege\":1,\"busse\":9,\"kraftraeder\":10,\"fahrradfahrer\":102,\"pkwEinheiten\":321},{\"startUhrzeit\":{\"hour\":7,\"minute\":45,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":8,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":242,\"lkw\":4,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":13,\"fahrradfahrer\":125,\"pkwEinheiten\":308},{\"type\":\"Stunde\",\"startUhrzeit\":{\"hour\":7,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":8,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":883,\"lkw\":24,\"lastzuege\":1,\"busse\":22,\"kraftraeder\":51,\"fahrradfahrer\":350,\"pkwEinheiten\":1143},{\"startUhrzeit\":{\"hour\":8,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":8,\"minute\":15,\"second\":0,\"nano\":0},\"pkw\":298,\"lkw\":15,\"lastzuege\":0,\"busse\":7,\"kraftraeder\":14,\"fahrradfahrer\":103,\"pkwEinheiten\":390},{\"startUhrzeit\":{\"hour\":8,\"minute\":15,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":8,\"minute\":30,\"second\":0,\"nano\":0},\"pkw\":260,\"lkw\":14,\"lastzuege\":3,\"busse\":4,\"kraftraeder\":14,\"fahrradfahrer\":91,\"pkwEinheiten\":348},{\"startUhrzeit\":{\"hour\":8,\"minute\":30,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":8,\"minute\":45,\"second\":0,\"nano\":0},\"pkw\":247,\"lkw\":8,\"lastzuege\":0,\"busse\":7,\"kraftraeder\":21,\"fahrradfahrer\":80,\"pkwEinheiten\":322},{\"startUhrzeit\":{\"hour\":8,\"minute\":45,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":9,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":263,\"lkw\":11,\"lastzuege\":2,\"busse\":5,\"kraftraeder\":15,\"fahrradfahrer\":81,\"pkwEinheiten\":342},{\"type\":\"Stunde\",\"startUhrzeit\":{\"hour\":8,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":9,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":1068,\"lkw\":48,\"lastzuege\":5,\"busse\":23,\"kraftraeder\":64,\"fahrradfahrer\":355,\"pkwEinheiten\":1403},{\"startUhrzeit\":{\"hour\":9,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":9,\"minute\":15,\"second\":0,\"nano\":0},\"pkw\":204,\"lkw\":10,\"lastzuege\":0,\"busse\":8,\"kraftraeder\":11,\"fahrradfahrer\":46,\"pkwEinheiten\":271},{\"startUhrzeit\":{\"hour\":9,\"minute\":15,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":9,\"minute\":30,\"second\":0,\"nano\":0},\"pkw\":213,\"lkw\":9,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":13,\"fahrradfahrer\":42,\"pkwEinheiten\":264},{\"startUhrzeit\":{\"hour\":9,\"minute\":30,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":9,\"minute\":45,\"second\":0,\"nano\":0},\"pkw\":230,\"lkw\":7,\"lastzuege\":0,\"busse\":7,\"kraftraeder\":14,\"fahrradfahrer\":32,\"pkwEinheiten\":285},{\"startUhrzeit\":{\"hour\":9,\"minute\":45,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":10,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":246,\"lkw\":8,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":6,\"fahrradfahrer\":30,\"pkwEinheiten\":292},{\"type\":\"Stunde\",\"startUhrzeit\":{\"hour\":9,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":10,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":893,\"lkw\":34,\"lastzuege\":0,\"busse\":24,\"kraftraeder\":44,\"fahrradfahrer\":150,\"pkwEinheiten\":1112},{\"type\":\"SpStd\",\"startUhrzeit\":{\"hour\":8,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":9,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":1068,\"lkw\":48,\"lastzuege\":5,\"busse\":23,\"kraftraeder\":64,\"fahrradfahrer\":355,\"fussgaenger\":0,\"pkwEinheiten\":1403},{\"type\":\"Block\",\"startUhrzeit\":{\"hour\":6,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":10,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":3242,\"lkw\":130,\"lastzuege\":8,\"busse\":89,\"kraftraeder\":181,\"fahrradfahrer\":954,\"pkwEinheiten\":4222},{\"startUhrzeit\":{\"hour\":15,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":15,\"minute\":15,\"second\":0,\"nano\":0},\"pkw\":251,\"lkw\":3,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":15,\"fahrradfahrer\":44,\"pkwEinheiten\":295},{\"startUhrzeit\":{\"hour\":15,\"minute\":15,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":15,\"minute\":30,\"second\":0,\"nano\":0},\"pkw\":282,\"lkw\":5,\"lastzuege\":0,\"busse\":2,\"kraftraeder\":20,\"fahrradfahrer\":50,\"pkwEinheiten\":324},{\"startUhrzeit\":{\"hour\":15,\"minute\":30,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":15,\"minute\":45,\"second\":0,\"nano\":0},\"pkw\":261,\"lkw\":4,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":17,\"fahrradfahrer\":61,\"pkwEinheiten\":313},{\"startUhrzeit\":{\"hour\":15,\"minute\":45,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":16,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":289,\"lkw\":5,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":20,\"fahrradfahrer\":61,\"pkwEinheiten\":341},{\"type\":\"Stunde\",\"startUhrzeit\":{\"hour\":15,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":16,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":1083,\"lkw\":17,\"lastzuege\":0,\"busse\":16,\"kraftraeder\":72,\"fahrradfahrer\":216,\"pkwEinheiten\":1274},{\"startUhrzeit\":{\"hour\":16,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":16,\"minute\":15,\"second\":0,\"nano\":0},\"pkw\":314,\"lkw\":2,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":17,\"fahrradfahrer\":41,\"pkwEinheiten\":356},{\"startUhrzeit\":{\"hour\":16,\"minute\":15,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":16,\"minute\":30,\"second\":0,\"nano\":0},\"pkw\":297,\"lkw\":5,\"lastzuege\":1,\"busse\":4,\"kraftraeder\":16,\"fahrradfahrer\":33,\"pkwEinheiten\":343},{\"startUhrzeit\":{\"hour\":16,\"minute\":30,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":16,\"minute\":45,\"second\":0,\"nano\":0},\"pkw\":311,\"lkw\":4,\"lastzuege\":0,\"busse\":3,\"kraftraeder\":22,\"fahrradfahrer\":45,\"pkwEinheiten\":354},{\"startUhrzeit\":{\"hour\":16,\"minute\":45,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":17,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":309,\"lkw\":2,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":26,\"fahrradfahrer\":62,\"pkwEinheiten\":359},{\"type\":\"Stunde\",\"startUhrzeit\":{\"hour\":16,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":17,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":1231,\"lkw\":13,\"lastzuege\":1,\"busse\":16,\"kraftraeder\":81,\"fahrradfahrer\":181,\"pkwEinheiten\":1412},{\"startUhrzeit\":{\"hour\":17,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":17,\"minute\":15,\"second\":0,\"nano\":0},\"pkw\":317,\"lkw\":3,\"lastzuege\":0,\"busse\":10,\"kraftraeder\":18,\"fahrradfahrer\":52,\"pkwEinheiten\":383},{\"startUhrzeit\":{\"hour\":17,\"minute\":15,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":17,\"minute\":30,\"second\":0,\"nano\":0},\"pkw\":323,\"lkw\":3,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":30,\"fahrradfahrer\":73,\"pkwEinheiten\":380},{\"startUhrzeit\":{\"hour\":17,\"minute\":30,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":17,\"minute\":45,\"second\":0,\"nano\":0},\"pkw\":340,\"lkw\":1,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":33,\"fahrradfahrer\":75,\"pkwEinheiten\":395},{\"startUhrzeit\":{\"hour\":17,\"minute\":45,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":18,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":338,\"lkw\":0,\"lastzuege\":0,\"busse\":9,\"kraftraeder\":18,\"fahrradfahrer\":77,\"pkwEinheiten\":402},{\"type\":\"Stunde\",\"startUhrzeit\":{\"hour\":17,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":18,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":1318,\"lkw\":7,\"lastzuege\":0,\"busse\":27,\"kraftraeder\":99,\"fahrradfahrer\":277,\"pkwEinheiten\":1559},{\"startUhrzeit\":{\"hour\":18,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":18,\"minute\":15,\"second\":0,\"nano\":0},\"pkw\":304,\"lkw\":4,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":16,\"fahrradfahrer\":73,\"pkwEinheiten\":359},{\"startUhrzeit\":{\"hour\":18,\"minute\":15,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":18,\"minute\":30,\"second\":0,\"nano\":0},\"pkw\":309,\"lkw\":5,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":16,\"fahrradfahrer\":62,\"pkwEinheiten\":367},{\"startUhrzeit\":{\"hour\":18,\"minute\":30,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":18,\"minute\":45,\"second\":0,\"nano\":0},\"pkw\":332,\"lkw\":5,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":23,\"fahrradfahrer\":61,\"pkwEinheiten\":393},{\"startUhrzeit\":{\"hour\":18,\"minute\":45,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":19,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":333,\"lkw\":1,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":13,\"fahrradfahrer\":53,\"pkwEinheiten\":375},{\"type\":\"Stunde\",\"startUhrzeit\":{\"hour\":18,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":19,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":1278,\"lkw\":15,\"lastzuege\":0,\"busse\":22,\"kraftraeder\":68,\"fahrradfahrer\":249,\"pkwEinheiten\":1494},{\"type\":\"SpStd\",\"startUhrzeit\":{\"hour\":17,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":18,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":1318,\"lkw\":7,\"lastzuege\":0,\"busse\":27,\"kraftraeder\":99,\"fahrradfahrer\":277,\"fussgaenger\":0,\"pkwEinheiten\":1559},{\"type\":\"Block\",\"startUhrzeit\":{\"hour\":15,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":19,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":4910,\"lkw\":52,\"lastzuege\":1,\"busse\":81,\"kraftraeder\":320,\"fahrradfahrer\":923,\"pkwEinheiten\":5738},{\"type\":\"SpStd\",\"startUhrzeit\":{\"hour\":17,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":18,\"minute\":0,\"second\":0,\"nano\":0},\"pkw\":1318,\"lkw\":7,\"lastzuege\":0,\"busse\":27,\"kraftraeder\":99,\"fahrradfahrer\":277,\"fussgaenger\":0,\"pkwEinheiten\":1559},{\"kfz\":16586,\"schwerverkehr\":762,\"gueterverkehr\":403,\"type\":\"Tageswert\",\"startUhrzeit\":{\"hour\":0,\"minute\":0,\"second\":0,\"nano\":0},\"endeUhrzeit\":{\"hour\":23,\"minute\":59,\"second\":59,\"nano\":999000000}}]}",
                LadeZaehldatenTableDTO.class);

        return ladeZaehldatenTableDTO;
    }

    public static LadeZaehldatumDTO getLadeZaehldatumDTO(final String type, final LocalTime start, final LocalTime end,
            final Integer pkw, final Integer lkw, final Integer lz, final Integer busse,
            final Integer krad, final Integer rad, final Integer fuss, final Integer pkwEinheiten) {
        final LadeZaehldatumDTO lzd = new LadeZaehldatumDTO();
        lzd.setType(type);
        lzd.setStartUhrzeit(start);
        lzd.setEndeUhrzeit(end);
        lzd.setPkw(pkw);
        lzd.setLkw(lkw);
        lzd.setLastzuege(lz);
        lzd.setBusse(busse);
        lzd.setKraftraeder(krad);
        lzd.setFahrradfahrer(rad);
        lzd.setFussgaenger(fuss);
        lzd.setPkwEinheiten(pkwEinheiten);
        return lzd;
    }

    private void init() throws DataNotFoundException {
        final Zaehlung zaehlung = getZaehlung();
        final Zaehlstelle zaehlstelle = getZaehlstelle(zaehlung);
        final LadeZaehldatenTableDTO ladeZaehldatenTableDTO = getLadeZaehldatenTableDTO();
        final OptionsDTO options = getChosenOptionsDTO();

        when(this.indexService.getZaehlstelleByZaehlungId(MOCKABLE_ZAEHLUNG_ID)).thenReturn(zaehlstelle);
        when(this.indexService.getZaehlung(MOCKABLE_ZAEHLUNG_ID)).thenReturn(zaehlung);
        when(this.ladeZaehldatenService.ladeZaehldaten(UUID.fromString(MOCKABLE_ZAEHLUNG_ID), options)).thenReturn(ladeZaehldatenTableDTO);
    }

    @Test
    public void fillBelastungsplanPdf() throws DataNotFoundException {
        this.init();

        final OptionsDTO options = getChosenOptionsDTO();

        final String chartAsBase64Png = "Hier könnte ihre Reklame stehen!";
        DiagrammPdf diagrammPdf = new DiagrammPdf();
        diagrammPdf = this.fillPdfBeanService.fillBelastungsplanPdf(diagrammPdf, MOCKABLE_ZAEHLUNG_ID, options, chartAsBase64Png, DEPARTMENT);

        assertThat(diagrammPdf.getChart(), is("Hier könnte ihre Reklame stehen!"));
        assertThat(diagrammPdf.getChartTitle(), is("Tageswert"));
        assertThat(diagrammPdf.getDocumentTitle(), is("Belastungsplan - Zählstelle 133301K"));
        assertThat(diagrammPdf.getFooterDate(), is(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        assertThat(diagrammPdf.getFooterOrganisationseinheit(), is(DEPARTMENT));

        final ZaehlstelleninformationenPdfComponent zaehlstelleninformationenPdfComponent = diagrammPdf.getZaehlstelleninformationen();
        assertThat(zaehlstelleninformationenPdfComponent.getKreuzungsname(), is("Leopoldstr. - Feilitzschstr. - Herzogstr."));
        assertThat(zaehlstelleninformationenPdfComponent.getWetter(), is("Regnerisch (dauerhaft)"));
        assertThat(zaehlstelleninformationenPdfComponent.getZaehlsituation(), is("Situation normal"));
        assertThat(zaehlstelleninformationenPdfComponent.getZaehlsituationErweitert(), is("Alles in bester Ordnung"));
        assertThat(zaehlstelleninformationenPdfComponent.getProjektname(), is("VZ Testinger"));
        assertThat(zaehlstelleninformationenPdfComponent.getZaehldatum(), is("04.11.2020"));
        assertThat(zaehlstelleninformationenPdfComponent.getZaehldauer(), is("Kurzzeiterhebung (6 bis 10 Uhr; 15 bis 19 Uhr)"));

        final ZusatzinformationenPdfComponent zusatzinformationenPdfComponent = diagrammPdf.getZusatzinformationen();
        assertThat(zusatzinformationenPdfComponent.getKommentarZaehlstelle(), is(""));
        assertThat(zusatzinformationenPdfComponent.getKommentarZaehlung(), is(""));
    }

    @Test
    public void fillGangliniePdf() throws DataNotFoundException {
        this.init();

        final OptionsDTO options = getChosenOptionsDTO();
        final String chartAsBase64Png = "Hier könnte ihre Reklame stehen!";
        final String schematischeUebersicht = "Übersicht";
        GangliniePdf gangliniePdfActual = new GangliniePdf();
        gangliniePdfActual = this.fillPdfBeanService.fillGangliniePdf(gangliniePdfActual, MOCKABLE_ZAEHLUNG_ID, options, chartAsBase64Png,
                schematischeUebersicht, DEPARTMENT);

        /*
         * Nachfolgende JSON wurde aus der Produktion im Debug Mode generiert. Beschreibung siehe JavaDoc
         * von FillBeanServiceSpringTest.
         */
        final Gson gson = new Gson();
        final GangliniePdf ganglinieExpected = gson.fromJson(
                "{\"tableCellWidth\":\"13mm\",\"ganglinieTables\":[{\"ganglinieTableColumns\":[{\"uhrzeit\":\"6 - 7\",\"kfz\":\"466\",\"sv\":\"46\",\"svAnteil\":\"9.9\",\"gv\":\"26\",\"gvAnteil\":\"5.6\",\"pkw\":\"398\",\"lkw\":\"24\",\"lastzuege\":\"2\",\"busse\":\"20\",\"kraftraeder\":\"22\",\"fahrradfahrer\":\"99\",\"fussgaenger\":\"\",\"pkwEinheiten\":\"565\"},{\"uhrzeit\":\"7 - 8\",\"kfz\":\"981\",\"sv\":\"47\",\"svAnteil\":\"4.8\",\"gv\":\"25\",\"gvAnteil\":\"2.6\",\"pkw\":\"883\",\"lkw\":\"24\",\"lastzuege\":\"1\",\"busse\":\"22\",\"kraftraeder\":\"51\",\"fahrradfahrer\":\"350\",\"fussgaenger\":\"\",\"pkwEinheiten\":\"1143\"},{\"uhrzeit\":\"8 - 9\",\"kfz\":\"1208\",\"sv\":\"76\",\"svAnteil\":\"6.3\",\"gv\":\"53\",\"gvAnteil\":\"4.4\",\"pkw\":\"1068\",\"lkw\":\"48\",\"lastzuege\":\"5\",\"busse\":\"23\",\"kraftraeder\":\"64\",\"fahrradfahrer\":\"355\",\"fussgaenger\":\"\",\"pkwEinheiten\":\"1403\"},{\"uhrzeit\":\"9 - 10\",\"kfz\":\"995\",\"sv\":\"58\",\"svAnteil\":\"5.8\",\"gv\":\"34\",\"gvAnteil\":\"3.4\",\"pkw\":\"893\",\"lkw\":\"34\",\"lastzuege\":\"0\",\"busse\":\"24\",\"kraftraeder\":\"44\",\"fahrradfahrer\":\"150\",\"fussgaenger\":\"\",\"pkwEinheiten\":\"1112\"},{\"uhrzeit\":\"Block\",\"kfz\":\"3650\",\"sv\":\"227\",\"svAnteil\":\"6.2\",\"gv\":\"138\",\"gvAnteil\":\"3.8\",\"pkw\":\"3242\",\"lkw\":\"130\",\"lastzuege\":\"8\",\"busse\":\"89\",\"kraftraeder\":\"181\",\"fahrradfahrer\":\"954\",\"fussgaenger\":\"\",\"pkwEinheiten\":\"4222\"},{\"uhrzeit\":\"15 - 16\",\"kfz\":\"1188\",\"sv\":\"33\",\"svAnteil\":\"2.8\",\"gv\":\"17\",\"gvAnteil\":\"1.4\",\"pkw\":\"1083\",\"lkw\":\"17\",\"lastzuege\":\"0\",\"busse\":\"16\",\"kraftraeder\":\"72\",\"fahrradfahrer\":\"216\",\"fussgaenger\":\"\",\"pkwEinheiten\":\"1274\"},{\"uhrzeit\":\"16 - 17\",\"kfz\":\"1342\",\"sv\":\"30\",\"svAnteil\":\"2.2\",\"gv\":\"14\",\"gvAnteil\":\"1.0\",\"pkw\":\"1231\",\"lkw\":\"13\",\"lastzuege\":\"1\",\"busse\":\"16\",\"kraftraeder\":\"81\",\"fahrradfahrer\":\"181\",\"fussgaenger\":\"\",\"pkwEinheiten\":\"1412\"},{\"uhrzeit\":\"17 - 18\",\"kfz\":\"1451\",\"sv\":\"34\",\"svAnteil\":\"2.3\",\"gv\":\"7\",\"gvAnteil\":\"0.5\",\"pkw\":\"1318\",\"lkw\":\"7\",\"lastzuege\":\"0\",\"busse\":\"27\",\"kraftraeder\":\"99\",\"fahrradfahrer\":\"277\",\"fussgaenger\":\"\",\"pkwEinheiten\":\"1559\"},{\"uhrzeit\":\"18 - 19\",\"kfz\":\"1383\",\"sv\":\"37\",\"svAnteil\":\"2.7\",\"gv\":\"15\",\"gvAnteil\":\"1.1\",\"pkw\":\"1278\",\"lkw\":\"15\",\"lastzuege\":\"0\",\"busse\":\"22\",\"kraftraeder\":\"68\",\"fahrradfahrer\":\"249\",\"fussgaenger\":\"\",\"pkwEinheiten\":\"1494\"},{\"uhrzeit\":\"Block\",\"kfz\":\"5364\",\"sv\":\"134\",\"svAnteil\":\"2.5\",\"gv\":\"53\",\"gvAnteil\":\"1.0\",\"pkw\":\"4910\",\"lkw\":\"52\",\"lastzuege\":\"1\",\"busse\":\"81\",\"kraftraeder\":\"320\",\"fahrradfahrer\":\"923\",\"fussgaenger\":\"\",\"pkwEinheiten\":\"5738\"},{\"uhrzeit\":\"Tageswert\",\"kfz\":\"0\",\"sv\":\"0\",\"svAnteil\":\"0\",\"gv\":\"0\",\"gvAnteil\":\"0\",\"pkw\":\"\",\"lkw\":\"\",\"lastzuege\":\"\",\"busse\":\"\",\"kraftraeder\":\"\",\"fahrradfahrer\":\"\",\"fussgaenger\":\"\",\"pkwEinheiten\":\"\"}]}],\"schematischeUebersichtAsBase64Png\":\"Übersicht\",\"schematischeUebersichtNeeded\":false,\"kraftfahrzeugverkehr\":true,\"schwerverkehr\":true,\"gueterverkehr\":true,\"radverkehr\":true,\"fussverkehr\":true,\"schwerverkehrsanteilProzent\":true,\"gueterverkehrsanteilProzent\":true,\"pkwEinheiten\":true,\"personenkraftwagen\":true,\"lastkraftwagen\":true,\"lastzuege\":true,\"busse\":true,\"kraftraeder\":false,\"chart\":\"Hier könnte ihre Reklame stehen!\",\"chartTitle\":\"Gesamte Zählstelle\",\"zaehlstelleninformationen\":{\"wetter\":\"Regnerisch (dauerhaft)\",\"zaehlsituation\":\"Situation normal\",\"projektname\":\"VZ Testinger\",\"zaehldatum\":\"04.11.2020\",\"zaehldauer\":\"Kurzzeiterhebung (6 bis 10 Uhr; 15 bis 19 Uhr)\",\"kreuzungsname\":\"Leopoldstr. - Feilitzschstr. - Herzogstr.\"},\"zusatzinformationen\":{\"istKommentarVorhanden\":false,\"istKommentarVorhandenZaehlstelle\":false,\"istKommentarVorhandenZaehlung\":false,\"kommentarZaehlstelle\":\"\",\"kommentarZaehlung\":\"\"},\"documentTitle\":\"Ganglinie - Zählstelle 133301K\",\"footerDate\":\"18.06.2021\",\"footerOrganisationseinheit\":\"TestOU\"}",
                GangliniePdf.class);

        assertThat(gangliniePdfActual, is(ganglinieExpected));
    }

    @Test
    public void fillDatentabellePdf() throws DataNotFoundException {
        this.init();
        final OptionsDTO options = getChosenOptionsDTO();
        final String schematischeUebersicht = "Base64 String";

        DatentabellePdf datentabellePdfActual = new DatentabellePdf();
        datentabellePdfActual = this.fillPdfBeanService.fillDatentabellePdf(datentabellePdfActual, MOCKABLE_ZAEHLUNG_ID, options, schematischeUebersicht,
                DEPARTMENT);

        /*
         * Nachfolgende JSON wurde aus der Produktion im Debug Mode generiert. Beschreibung siehe JavaDoc
         * von FillPdfBeanServiceSpringTest.
         */
        final Gson gson = new Gson();
        final DatentabellePdf datentabellePdfExpected = gson.fromJson(
                "{\"datentabelleZaehldaten\":{\"zaehldatenList\":[{\"startUhrzeit\":\"06:00\",\"endeUhrzeit\":\"06:15\",\"pkw\":49,\"lkw\":4,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":3,\"fahrradfahrer\":17,\"pkwEinheiten\":85,\"gesamt\":79,\"kfz\":62,\"schwerverkehr\":10,\"gueterverkehr\":4,\"anteilSchwerverkehrAnKfzProzent\":16.1,\"anteilGueterverkehrAnKfzProzent\":6.5},{\"startUhrzeit\":\"06:15\",\"endeUhrzeit\":\"06:30\",\"pkw\":83,\"lkw\":8,\"lastzuege\":1,\"busse\":2,\"kraftraeder\":4,\"fahrradfahrer\":22,\"pkwEinheiten\":119,\"gesamt\":120,\"kfz\":98,\"schwerverkehr\":11,\"gueterverkehr\":9,\"anteilSchwerverkehrAnKfzProzent\":11.2,\"anteilGueterverkehrAnKfzProzent\":9.2},{\"startUhrzeit\":\"06:30\",\"endeUhrzeit\":\"06:45\",\"pkw\":122,\"lkw\":4,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":9,\"fahrradfahrer\":31,\"pkwEinheiten\":165,\"gesamt\":172,\"kfz\":141,\"schwerverkehr\":10,\"gueterverkehr\":4,\"anteilSchwerverkehrAnKfzProzent\":7.1,\"anteilGueterverkehrAnKfzProzent\":2.8},{\"startUhrzeit\":\"06:45\",\"endeUhrzeit\":\"07:00\",\"pkw\":144,\"lkw\":8,\"lastzuege\":1,\"busse\":6,\"kraftraeder\":6,\"fahrradfahrer\":29,\"pkwEinheiten\":197,\"gesamt\":194,\"kfz\":165,\"schwerverkehr\":15,\"gueterverkehr\":9,\"anteilSchwerverkehrAnKfzProzent\":9.1,\"anteilGueterverkehrAnKfzProzent\":5.5},{\"type\":\"Stunde\",\"startUhrzeit\":\"06:00\",\"endeUhrzeit\":\"07:00\",\"pkw\":398,\"lkw\":24,\"lastzuege\":2,\"busse\":20,\"kraftraeder\":22,\"fahrradfahrer\":99,\"pkwEinheiten\":565,\"gesamt\":565,\"kfz\":466,\"schwerverkehr\":46,\"gueterverkehr\":26,\"anteilSchwerverkehrAnKfzProzent\":9.9,\"anteilGueterverkehrAnKfzProzent\":5.6},{\"startUhrzeit\":\"07:00\",\"endeUhrzeit\":\"07:15\",\"pkw\":191,\"lkw\":7,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":14,\"fahrradfahrer\":55,\"pkwEinheiten\":250,\"gesamt\":273,\"kfz\":218,\"schwerverkehr\":13,\"gueterverkehr\":7,\"anteilSchwerverkehrAnKfzProzent\":6.0,\"anteilGueterverkehrAnKfzProzent\":3.2},{\"startUhrzeit\":\"07:15\",\"endeUhrzeit\":\"07:30\",\"pkw\":216,\"lkw\":5,\"lastzuege\":0,\"busse\":3,\"kraftraeder\":14,\"fahrradfahrer\":68,\"pkwEinheiten\":264,\"gesamt\":306,\"kfz\":238,\"schwerverkehr\":8,\"gueterverkehr\":5,\"anteilSchwerverkehrAnKfzProzent\":3.4,\"anteilGueterverkehrAnKfzProzent\":2.1},{\"startUhrzeit\":\"07:30\",\"endeUhrzeit\":\"07:45\",\"pkw\":234,\"lkw\":8,\"lastzuege\":1,\"busse\":9,\"kraftraeder\":10,\"fahrradfahrer\":102,\"pkwEinheiten\":321,\"gesamt\":364,\"kfz\":262,\"schwerverkehr\":18,\"gueterverkehr\":9,\"anteilSchwerverkehrAnKfzProzent\":6.9,\"anteilGueterverkehrAnKfzProzent\":3.4},{\"startUhrzeit\":\"07:45\",\"endeUhrzeit\":\"08:00\",\"pkw\":242,\"lkw\":4,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":13,\"fahrradfahrer\":125,\"pkwEinheiten\":308,\"gesamt\":388,\"kfz\":263,\"schwerverkehr\":8,\"gueterverkehr\":4,\"anteilSchwerverkehrAnKfzProzent\":3.0,\"anteilGueterverkehrAnKfzProzent\":1.5},{\"type\":\"Stunde\",\"startUhrzeit\":\"07:00\",\"endeUhrzeit\":\"08:00\",\"pkw\":883,\"lkw\":24,\"lastzuege\":1,\"busse\":22,\"kraftraeder\":51,\"fahrradfahrer\":350,\"pkwEinheiten\":1143,\"gesamt\":1331,\"kfz\":981,\"schwerverkehr\":47,\"gueterverkehr\":25,\"anteilSchwerverkehrAnKfzProzent\":4.8,\"anteilGueterverkehrAnKfzProzent\":2.6},{\"startUhrzeit\":\"08:00\",\"endeUhrzeit\":\"08:15\",\"pkw\":298,\"lkw\":15,\"lastzuege\":0,\"busse\":7,\"kraftraeder\":14,\"fahrradfahrer\":103,\"pkwEinheiten\":390,\"gesamt\":437,\"kfz\":334,\"schwerverkehr\":22,\"gueterverkehr\":15,\"anteilSchwerverkehrAnKfzProzent\":6.6,\"anteilGueterverkehrAnKfzProzent\":4.5},{\"startUhrzeit\":\"08:15\",\"endeUhrzeit\":\"08:30\",\"pkw\":260,\"lkw\":14,\"lastzuege\":3,\"busse\":4,\"kraftraeder\":14,\"fahrradfahrer\":91,\"pkwEinheiten\":348,\"gesamt\":386,\"kfz\":295,\"schwerverkehr\":21,\"gueterverkehr\":17,\"anteilSchwerverkehrAnKfzProzent\":7.1,\"anteilGueterverkehrAnKfzProzent\":5.8},{\"startUhrzeit\":\"08:30\",\"endeUhrzeit\":\"08:45\",\"pkw\":247,\"lkw\":8,\"lastzuege\":0,\"busse\":7,\"kraftraeder\":21,\"fahrradfahrer\":80,\"pkwEinheiten\":322,\"gesamt\":363,\"kfz\":283,\"schwerverkehr\":15,\"gueterverkehr\":8,\"anteilSchwerverkehrAnKfzProzent\":5.3,\"anteilGueterverkehrAnKfzProzent\":2.8},{\"startUhrzeit\":\"08:45\",\"endeUhrzeit\":\"09:00\",\"pkw\":263,\"lkw\":11,\"lastzuege\":2,\"busse\":5,\"kraftraeder\":15,\"fahrradfahrer\":81,\"pkwEinheiten\":342,\"gesamt\":377,\"kfz\":296,\"schwerverkehr\":18,\"gueterverkehr\":13,\"anteilSchwerverkehrAnKfzProzent\":6.1,\"anteilGueterverkehrAnKfzProzent\":4.4},{\"type\":\"Stunde\",\"startUhrzeit\":\"08:00\",\"endeUhrzeit\":\"09:00\",\"pkw\":1068,\"lkw\":48,\"lastzuege\":5,\"busse\":23,\"kraftraeder\":64,\"fahrradfahrer\":355,\"pkwEinheiten\":1403,\"gesamt\":1563,\"kfz\":1208,\"schwerverkehr\":76,\"gueterverkehr\":53,\"anteilSchwerverkehrAnKfzProzent\":6.3,\"anteilGueterverkehrAnKfzProzent\":4.4},{\"startUhrzeit\":\"09:00\",\"endeUhrzeit\":\"09:15\",\"pkw\":204,\"lkw\":10,\"lastzuege\":0,\"busse\":8,\"kraftraeder\":11,\"fahrradfahrer\":46,\"pkwEinheiten\":271,\"gesamt\":279,\"kfz\":233,\"schwerverkehr\":18,\"gueterverkehr\":10,\"anteilSchwerverkehrAnKfzProzent\":7.7,\"anteilGueterverkehrAnKfzProzent\":4.3},{\"startUhrzeit\":\"09:15\",\"endeUhrzeit\":\"09:30\",\"pkw\":213,\"lkw\":9,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":13,\"fahrradfahrer\":42,\"pkwEinheiten\":264,\"gesamt\":281,\"kfz\":239,\"schwerverkehr\":13,\"gueterverkehr\":9,\"anteilSchwerverkehrAnKfzProzent\":5.4,\"anteilGueterverkehrAnKfzProzent\":3.8},{\"startUhrzeit\":\"09:30\",\"endeUhrzeit\":\"09:45\",\"pkw\":230,\"lkw\":7,\"lastzuege\":0,\"busse\":7,\"kraftraeder\":14,\"fahrradfahrer\":32,\"pkwEinheiten\":285,\"gesamt\":290,\"kfz\":258,\"schwerverkehr\":14,\"gueterverkehr\":7,\"anteilSchwerverkehrAnKfzProzent\":5.4,\"anteilGueterverkehrAnKfzProzent\":2.7},{\"startUhrzeit\":\"09:45\",\"endeUhrzeit\":\"10:00\",\"pkw\":246,\"lkw\":8,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":6,\"fahrradfahrer\":30,\"pkwEinheiten\":292,\"gesamt\":295,\"kfz\":265,\"schwerverkehr\":13,\"gueterverkehr\":8,\"anteilSchwerverkehrAnKfzProzent\":4.9,\"anteilGueterverkehrAnKfzProzent\":3.0},{\"type\":\"Stunde\",\"startUhrzeit\":\"09:00\",\"endeUhrzeit\":\"10:00\",\"pkw\":893,\"lkw\":34,\"lastzuege\":0,\"busse\":24,\"kraftraeder\":44,\"fahrradfahrer\":150,\"pkwEinheiten\":1112,\"gesamt\":1145,\"kfz\":995,\"schwerverkehr\":58,\"gueterverkehr\":34,\"anteilSchwerverkehrAnKfzProzent\":5.8,\"anteilGueterverkehrAnKfzProzent\":3.4},{\"type\":\"SpStd\",\"startUhrzeit\":\"08:00\",\"endeUhrzeit\":\"09:00\",\"pkw\":1068,\"lkw\":48,\"lastzuege\":5,\"busse\":23,\"kraftraeder\":64,\"fahrradfahrer\":355,\"fussgaenger\":0,\"pkwEinheiten\":1403,\"gesamt\":1563,\"kfz\":1208,\"schwerverkehr\":76,\"gueterverkehr\":53,\"anteilSchwerverkehrAnKfzProzent\":6.3,\"anteilGueterverkehrAnKfzProzent\":4.4},{\"type\":\"Block\",\"startUhrzeit\":\"06:00\",\"endeUhrzeit\":\"10:00\",\"pkw\":3242,\"lkw\":130,\"lastzuege\":8,\"busse\":89,\"kraftraeder\":181,\"fahrradfahrer\":954,\"pkwEinheiten\":4222,\"gesamt\":4604,\"kfz\":3650,\"schwerverkehr\":227,\"gueterverkehr\":138,\"anteilSchwerverkehrAnKfzProzent\":6.2,\"anteilGueterverkehrAnKfzProzent\":3.8},{\"startUhrzeit\":\"15:00\",\"endeUhrzeit\":\"15:15\",\"pkw\":251,\"lkw\":3,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":15,\"fahrradfahrer\":44,\"pkwEinheiten\":295,\"gesamt\":318,\"kfz\":274,\"schwerverkehr\":8,\"gueterverkehr\":3,\"anteilSchwerverkehrAnKfzProzent\":2.9,\"anteilGueterverkehrAnKfzProzent\":1.1},{\"startUhrzeit\":\"15:15\",\"endeUhrzeit\":\"15:30\",\"pkw\":282,\"lkw\":5,\"lastzuege\":0,\"busse\":2,\"kraftraeder\":20,\"fahrradfahrer\":50,\"pkwEinheiten\":324,\"gesamt\":359,\"kfz\":309,\"schwerverkehr\":7,\"gueterverkehr\":5,\"anteilSchwerverkehrAnKfzProzent\":2.3,\"anteilGueterverkehrAnKfzProzent\":1.6},{\"startUhrzeit\":\"15:30\",\"endeUhrzeit\":\"15:45\",\"pkw\":261,\"lkw\":4,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":17,\"fahrradfahrer\":61,\"pkwEinheiten\":313,\"gesamt\":348,\"kfz\":287,\"schwerverkehr\":9,\"gueterverkehr\":4,\"anteilSchwerverkehrAnKfzProzent\":3.1,\"anteilGueterverkehrAnKfzProzent\":1.4},{\"startUhrzeit\":\"15:45\",\"endeUhrzeit\":\"16:00\",\"pkw\":289,\"lkw\":5,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":20,\"fahrradfahrer\":61,\"pkwEinheiten\":341,\"gesamt\":379,\"kfz\":318,\"schwerverkehr\":9,\"gueterverkehr\":5,\"anteilSchwerverkehrAnKfzProzent\":2.8,\"anteilGueterverkehrAnKfzProzent\":1.6},{\"type\":\"Stunde\",\"startUhrzeit\":\"15:00\",\"endeUhrzeit\":\"16:00\",\"pkw\":1083,\"lkw\":17,\"lastzuege\":0,\"busse\":16,\"kraftraeder\":72,\"fahrradfahrer\":216,\"pkwEinheiten\":1274,\"gesamt\":1404,\"kfz\":1188,\"schwerverkehr\":33,\"gueterverkehr\":17,\"anteilSchwerverkehrAnKfzProzent\":2.8,\"anteilGueterverkehrAnKfzProzent\":1.4},{\"startUhrzeit\":\"16:00\",\"endeUhrzeit\":\"16:15\",\"pkw\":314,\"lkw\":2,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":17,\"fahrradfahrer\":41,\"pkwEinheiten\":356,\"gesamt\":379,\"kfz\":338,\"schwerverkehr\":7,\"gueterverkehr\":2,\"anteilSchwerverkehrAnKfzProzent\":2.1,\"anteilGueterverkehrAnKfzProzent\":0.6},{\"startUhrzeit\":\"16:15\",\"endeUhrzeit\":\"16:30\",\"pkw\":297,\"lkw\":5,\"lastzuege\":1,\"busse\":4,\"kraftraeder\":16,\"fahrradfahrer\":33,\"pkwEinheiten\":343,\"gesamt\":356,\"kfz\":323,\"schwerverkehr\":10,\"gueterverkehr\":6,\"anteilSchwerverkehrAnKfzProzent\":3.1,\"anteilGueterverkehrAnKfzProzent\":1.9},{\"startUhrzeit\":\"16:30\",\"endeUhrzeit\":\"16:45\",\"pkw\":311,\"lkw\":4,\"lastzuege\":0,\"busse\":3,\"kraftraeder\":22,\"fahrradfahrer\":45,\"pkwEinheiten\":354,\"gesamt\":385,\"kfz\":340,\"schwerverkehr\":7,\"gueterverkehr\":4,\"anteilSchwerverkehrAnKfzProzent\":2.1,\"anteilGueterverkehrAnKfzProzent\":1.2},{\"startUhrzeit\":\"16:45\",\"endeUhrzeit\":\"17:00\",\"pkw\":309,\"lkw\":2,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":26,\"fahrradfahrer\":62,\"pkwEinheiten\":359,\"gesamt\":403,\"kfz\":341,\"schwerverkehr\":6,\"gueterverkehr\":2,\"anteilSchwerverkehrAnKfzProzent\":1.8,\"anteilGueterverkehrAnKfzProzent\":0.6},{\"type\":\"Stunde\",\"startUhrzeit\":\"16:00\",\"endeUhrzeit\":\"17:00\",\"pkw\":1231,\"lkw\":13,\"lastzuege\":1,\"busse\":16,\"kraftraeder\":81,\"fahrradfahrer\":181,\"pkwEinheiten\":1412,\"gesamt\":1523,\"kfz\":1342,\"schwerverkehr\":30,\"gueterverkehr\":14,\"anteilSchwerverkehrAnKfzProzent\":2.2,\"anteilGueterverkehrAnKfzProzent\":1.0},{\"startUhrzeit\":\"17:00\",\"endeUhrzeit\":\"17:15\",\"pkw\":317,\"lkw\":3,\"lastzuege\":0,\"busse\":10,\"kraftraeder\":18,\"fahrradfahrer\":52,\"pkwEinheiten\":383,\"gesamt\":400,\"kfz\":348,\"schwerverkehr\":13,\"gueterverkehr\":3,\"anteilSchwerverkehrAnKfzProzent\":3.7,\"anteilGueterverkehrAnKfzProzent\":0.9},{\"startUhrzeit\":\"17:15\",\"endeUhrzeit\":\"17:30\",\"pkw\":323,\"lkw\":3,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":30,\"fahrradfahrer\":73,\"pkwEinheiten\":380,\"gesamt\":433,\"kfz\":360,\"schwerverkehr\":7,\"gueterverkehr\":3,\"anteilSchwerverkehrAnKfzProzent\":1.9,\"anteilGueterverkehrAnKfzProzent\":0.8},{\"startUhrzeit\":\"17:30\",\"endeUhrzeit\":\"17:45\",\"pkw\":340,\"lkw\":1,\"lastzuege\":0,\"busse\":4,\"kraftraeder\":33,\"fahrradfahrer\":75,\"pkwEinheiten\":395,\"gesamt\":453,\"kfz\":378,\"schwerverkehr\":5,\"gueterverkehr\":1,\"anteilSchwerverkehrAnKfzProzent\":1.3,\"anteilGueterverkehrAnKfzProzent\":0.3},{\"startUhrzeit\":\"17:45\",\"endeUhrzeit\":\"18:00\",\"pkw\":338,\"lkw\":0,\"lastzuege\":0,\"busse\":9,\"kraftraeder\":18,\"fahrradfahrer\":77,\"pkwEinheiten\":402,\"gesamt\":442,\"kfz\":365,\"schwerverkehr\":9,\"gueterverkehr\":0,\"anteilSchwerverkehrAnKfzProzent\":2.5,\"anteilGueterverkehrAnKfzProzent\":0.0},{\"type\":\"Stunde\",\"startUhrzeit\":\"17:00\",\"endeUhrzeit\":\"18:00\",\"pkw\":1318,\"lkw\":7,\"lastzuege\":0,\"busse\":27,\"kraftraeder\":99,\"fahrradfahrer\":277,\"pkwEinheiten\":1559,\"gesamt\":1728,\"kfz\":1451,\"schwerverkehr\":34,\"gueterverkehr\":7,\"anteilSchwerverkehrAnKfzProzent\":2.3,\"anteilGueterverkehrAnKfzProzent\":0.5},{\"startUhrzeit\":\"18:00\",\"endeUhrzeit\":\"18:15\",\"pkw\":304,\"lkw\":4,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":16,\"fahrradfahrer\":73,\"pkwEinheiten\":359,\"gesamt\":402,\"kfz\":329,\"schwerverkehr\":9,\"gueterverkehr\":4,\"anteilSchwerverkehrAnKfzProzent\":2.7,\"anteilGueterverkehrAnKfzProzent\":1.2},{\"startUhrzeit\":\"18:15\",\"endeUhrzeit\":\"18:30\",\"pkw\":309,\"lkw\":5,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":16,\"fahrradfahrer\":62,\"pkwEinheiten\":367,\"gesamt\":398,\"kfz\":336,\"schwerverkehr\":11,\"gueterverkehr\":5,\"anteilSchwerverkehrAnKfzProzent\":3.3,\"anteilGueterverkehrAnKfzProzent\":1.5},{\"startUhrzeit\":\"18:30\",\"endeUhrzeit\":\"18:45\",\"pkw\":332,\"lkw\":5,\"lastzuege\":0,\"busse\":6,\"kraftraeder\":23,\"fahrradfahrer\":61,\"pkwEinheiten\":393,\"gesamt\":427,\"kfz\":366,\"schwerverkehr\":11,\"gueterverkehr\":5,\"anteilSchwerverkehrAnKfzProzent\":3.0,\"anteilGueterverkehrAnKfzProzent\":1.4},{\"startUhrzeit\":\"18:45\",\"endeUhrzeit\":\"19:00\",\"pkw\":333,\"lkw\":1,\"lastzuege\":0,\"busse\":5,\"kraftraeder\":13,\"fahrradfahrer\":53,\"pkwEinheiten\":375,\"gesamt\":405,\"kfz\":352,\"schwerverkehr\":6,\"gueterverkehr\":1,\"anteilSchwerverkehrAnKfzProzent\":1.7,\"anteilGueterverkehrAnKfzProzent\":0.3},{\"type\":\"Stunde\",\"startUhrzeit\":\"18:00\",\"endeUhrzeit\":\"19:00\",\"pkw\":1278,\"lkw\":15,\"lastzuege\":0,\"busse\":22,\"kraftraeder\":68,\"fahrradfahrer\":249,\"pkwEinheiten\":1494,\"gesamt\":1632,\"kfz\":1383,\"schwerverkehr\":37,\"gueterverkehr\":15,\"anteilSchwerverkehrAnKfzProzent\":2.7,\"anteilGueterverkehrAnKfzProzent\":1.1},{\"type\":\"SpStd\",\"startUhrzeit\":\"17:00\",\"endeUhrzeit\":\"18:00\",\"pkw\":1318,\"lkw\":7,\"lastzuege\":0,\"busse\":27,\"kraftraeder\":99,\"fahrradfahrer\":277,\"fussgaenger\":0,\"pkwEinheiten\":1559,\"gesamt\":1728,\"kfz\":1451,\"schwerverkehr\":34,\"gueterverkehr\":7,\"anteilSchwerverkehrAnKfzProzent\":2.3,\"anteilGueterverkehrAnKfzProzent\":0.5},{\"type\":\"Block\",\"startUhrzeit\":\"15:00\",\"endeUhrzeit\":\"19:00\",\"pkw\":4910,\"lkw\":52,\"lastzuege\":1,\"busse\":81,\"kraftraeder\":320,\"fahrradfahrer\":923,\"pkwEinheiten\":5738,\"gesamt\":6287,\"kfz\":5364,\"schwerverkehr\":134,\"gueterverkehr\":53,\"anteilSchwerverkehrAnKfzProzent\":2.5,\"anteilGueterverkehrAnKfzProzent\":1.0},{\"type\":\"SpStd\",\"startUhrzeit\":\"17:00\",\"endeUhrzeit\":\"18:00\",\"pkw\":1318,\"lkw\":7,\"lastzuege\":0,\"busse\":27,\"kraftraeder\":99,\"fahrradfahrer\":277,\"fussgaenger\":0,\"pkwEinheiten\":1559,\"gesamt\":1728,\"kfz\":1451,\"schwerverkehr\":34,\"gueterverkehr\":7,\"anteilSchwerverkehrAnKfzProzent\":2.3,\"anteilGueterverkehrAnKfzProzent\":0.5},{\"type\":\"Tageswert\",\"gesamt\":0,\"kfz\":0,\"schwerverkehr\":0,\"gueterverkehr\":0,\"anteilSchwerverkehrAnKfzProzent\":0,\"anteilGueterverkehrAnKfzProzent\":0}],\"activeTabsFahrzeugtypen\":6,\"activeTabsFahrzeugklassen\":3,\"activeTabsAnteile\":2,\"showPersonenkraftwagen\":true,\"showLastkraftwagen\":true,\"showLastzuege\":true,\"showBusse\":true,\"showKraftraeder\":false,\"showRadverkehr\":true,\"showFussverkehr\":true,\"showKraftfahrzeugverkehr\":true,\"showSchwerverkehr\":true,\"showGueterverkehr\":true,\"showSchwerverkehrsanteilProzent\":true,\"showGueterverkehrsanteilProzent\":true,\"showPkwEinheiten\":true},\"schematischeUebersichtAsBase64Png\":\"Base64 String\",\"schematischeUebersichtNeeded\":false,\"tableTitle\":\"Gesamte Zählstelle (Zulauf)\",\"zaehlstelleninformationen\":{\"wetter\":\"Regnerisch (dauerhaft)\",\"zaehlsituation\":\"Situation normal\",\"projektname\":\"VZ Testinger\",\"zaehldatum\":\"04.11.2020\",\"zaehldauer\":\"Kurzzeiterhebung (6 bis 10 Uhr; 15 bis 19 Uhr)\",\"kreuzungsname\":\"Leopoldstr. - Feilitzschstr. - Herzogstr.\"},\"zusatzinformationen\":{\"istKommentarVorhanden\":false,\"istKommentarVorhandenZaehlstelle\":false,\"istKommentarVorhandenZaehlung\":false,\"kommentarZaehlstelle\":\"\",\"kommentarZaehlung\":\"\"},\"documentTitle\":\"Listenausgabe - Zählstelle 133301K\",\"footerDate\":\"18.06.2021\",\"footerOrganisationseinheit\":\"TestOU\"}",
                DatentabellePdf.class);

        assertThat(datentabellePdfActual, is(datentabellePdfExpected));

    }

    @Test
    public void createChartTitleBelastungsplan() throws DataNotFoundException {
        this.init();

        final OptionsDTO options = getChosenOptionsDTO();

        String actualChartTitle = this.fillPdfBeanService.createChartTitleZeitauswahl(MOCKABLE_ZAEHLUNG_ID, options);
        assertThat(actualChartTitle, is("Tageswert"));

        options.setZeitauswahl(Zeitauswahl.BLOCK.getCapitalizedName());
        options.setZeitblock(Zeitblock.ZB_00_06);
        actualChartTitle = this.fillPdfBeanService.createChartTitleZeitauswahl(MOCKABLE_ZAEHLUNG_ID, options);
        assertThat(actualChartTitle, is("Block 0 - 6 Uhr"));

        options.setZeitauswahl(Zeitauswahl.STUNDE.getCapitalizedName());
        options.setZeitblock(Zeitblock.ZB_04_05);
        actualChartTitle = this.fillPdfBeanService.createChartTitleZeitauswahl(MOCKABLE_ZAEHLUNG_ID, options);
        assertThat(actualChartTitle, is("Stunde 4 - 5 Uhr"));

        options.setZeitauswahl(Zeitauswahl.SPITZENSTUNDE_KFZ.getCapitalizedName());
        options.setZeitblock(Zeitblock.ZB_06_10);
        when(this.ladeZaehldatenService.ladeZaehldaten(UUID.fromString(MOCKABLE_ZAEHLUNG_ID), options)).thenReturn(getLadeZaehldatenTableDTO());

        options.setZeitauswahl(Zeitauswahl.SPITZENSTUNDE_KFZ.getCapitalizedName());
        options.setZeitblock(Zeitblock.ZB_06_10);
        actualChartTitle = this.fillPdfBeanService.createChartTitleZeitauswahl(MOCKABLE_ZAEHLUNG_ID, options);
        assertThat(actualChartTitle, is("Spitzenstunde KFZ (Block 6 - 10 Uhr)"));
    }

    @Test
    void getCorrectZaehlartString() {
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.N.toString());
        String result = this.fillPdfBeanService.getCorrectZaehlartString(zaehlung);
        assertThat(result, is(StringUtils.EMPTY));

        zaehlung.setZaehlart(Zaehlart.Q.toString());
        result = this.fillPdfBeanService.getCorrectZaehlartString(zaehlung);
        assertThat(result, is("Q"));

        zaehlung.setZaehlart(Zaehlart.Q_.toString());
        result = this.fillPdfBeanService.getCorrectZaehlartString(zaehlung);
        assertThat(result, is("Q_"));
    }

    @Test
    public void createChartTitleBelastungsplan_Messstelle() throws DataNotFoundException {
        this.init();

        final MessstelleOptionsDTO options = new MessstelleOptionsDTO();

        options.setZeitauswahl(Zeitauswahl.TAGESWERT.getCapitalizedName());
        options.setZeitraum(List.of(LocalDate.now()));
        String actualChartTitle = this.fillPdfBeanService.createChartTitleZeitauswahl(options, null);
        assertThat(actualChartTitle, is("Tageswert"));

        options.setZeitraum(List.of(LocalDate.now(), LocalDate.now()));
        actualChartTitle = this.fillPdfBeanService.createChartTitleZeitauswahl(options, null);
        assertThat(actualChartTitle, is("Durchschnittlicher Tageswert"));

        options.setZeitauswahl(Zeitauswahl.BLOCK.getCapitalizedName());
        options.setZeitblock(Zeitblock.ZB_00_06);
        actualChartTitle = this.fillPdfBeanService.createChartTitleZeitauswahl(options, null);
        assertThat(actualChartTitle, is("Block 0 - 6 Uhr"));

        options.setZeitauswahl(Zeitauswahl.STUNDE.getCapitalizedName());
        options.setZeitblock(Zeitblock.ZB_04_05);
        actualChartTitle = this.fillPdfBeanService.createChartTitleZeitauswahl(options, null);
        assertThat(actualChartTitle, is("Stunde 4 - 5 Uhr"));

        options.setZeitauswahl(Zeitauswahl.SPITZENSTUNDE_KFZ.getCapitalizedName());
        options.setZeitblock(Zeitblock.ZB_06_10);
        actualChartTitle = this.fillPdfBeanService.createChartTitleZeitauswahl(options, Collections.emptyList());
        assertThat(actualChartTitle, is("Spitzenstunde KFZ (Block 6 - 10 Uhr)"));
    }

}
