package de.muenchen.dave.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.laden.FahrbeziehungenDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungDTO;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class VerkehrsbeziehungUtilTest {

    @Test
    public void determinePossibleFahrbeziehungenKreuzung() {
        final LadeZaehlungDTO ladeZaehlung = new LadeZaehlungDTO();
        ladeZaehlung.setKreisverkehr(Boolean.FALSE);
        ladeZaehlung.setVerkehrsbeziehungen(new ArrayList<>());

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzung1nach1 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzung1nach1.setIsKreuzung(true);
        fahrbeziehungKreuzung1nach1.setVon(1);
        fahrbeziehungKreuzung1nach1.setNach(1);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzung1nach1);

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzung1nach2 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzung1nach2.setIsKreuzung(true);
        fahrbeziehungKreuzung1nach2.setVon(1);
        fahrbeziehungKreuzung1nach2.setNach(2);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzung1nach2);

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzung1nach3 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzung1nach3.setIsKreuzung(true);
        fahrbeziehungKreuzung1nach3.setVon(1);
        fahrbeziehungKreuzung1nach3.setNach(3);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzung1nach3);

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzung2nach2 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzung2nach2.setIsKreuzung(true);
        fahrbeziehungKreuzung2nach2.setVon(2);
        fahrbeziehungKreuzung2nach2.setNach(2);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzung2nach2);

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzung2nach3 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzung2nach3.setIsKreuzung(true);
        fahrbeziehungKreuzung2nach3.setVon(2);
        fahrbeziehungKreuzung2nach3.setNach(3);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzung2nach3);

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzung3nach1 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzung3nach1.setIsKreuzung(true);
        fahrbeziehungKreuzung3nach1.setVon(3);
        fahrbeziehungKreuzung3nach1.setNach(1);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzung3nach1);

        final FahrbeziehungenDTO optionsFahrbeziehungen = FahrbeziehungUtil.determinePossibleVerkehrsbeziehung(ladeZaehlung);

        assertThat(optionsFahrbeziehungen.getVonKnotenarme().size(), is(3));
        assertThat(optionsFahrbeziehungen.getVonKnotenarme().toArray()[0], is(1));
        assertThat(optionsFahrbeziehungen.getVonKnotenarme().toArray()[1], is(2));
        assertThat(optionsFahrbeziehungen.getVonKnotenarme().toArray()[2], is(3));

        assertThat(optionsFahrbeziehungen.getNachKnotenarme().size(), is(3));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(1).size(), is(3));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(1).toArray()[0], is(1));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(1).toArray()[1], is(2));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(1).toArray()[2], is(3));

        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(2).size(), is(2));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(2).toArray()[0], is(2));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(2).toArray()[1], is(3));

        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(3).size(), is(1));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(3).toArray()[0], is(1));
    }

    @Test
    public void determinePossibleFahrbeziehungenKreisverkehr() {
        final LadeZaehlungDTO ladeZaehlung = new LadeZaehlungDTO();
        ladeZaehlung.setKreisverkehr(Boolean.TRUE);
        ladeZaehlung.setVerkehrsbeziehungen(new ArrayList<>());

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzungHinein1 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzungHinein1.setIsKreuzung(false);
        fahrbeziehungKreuzungHinein1.setKnotenarm(1);
        fahrbeziehungKreuzungHinein1.setHinein(Boolean.TRUE);
        fahrbeziehungKreuzungHinein1.setHeraus(Boolean.FALSE);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzungHinein1);

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzungHeraus1 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzungHeraus1.setIsKreuzung(false);
        fahrbeziehungKreuzungHeraus1.setKnotenarm(1);
        fahrbeziehungKreuzungHeraus1.setHinein(Boolean.FALSE);
        fahrbeziehungKreuzungHeraus1.setHeraus(Boolean.TRUE);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzungHeraus1);

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzungHinein2 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzungHinein2.setIsKreuzung(false);
        fahrbeziehungKreuzungHinein2.setKnotenarm(2);
        fahrbeziehungKreuzungHinein2.setHinein(Boolean.TRUE);
        fahrbeziehungKreuzungHinein2.setHeraus(Boolean.FALSE);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzungHinein2);

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzungHeraus2 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzungHeraus2.setIsKreuzung(false);
        fahrbeziehungKreuzungHeraus2.setKnotenarm(2);
        fahrbeziehungKreuzungHeraus2.setHinein(Boolean.FALSE);
        fahrbeziehungKreuzungHeraus2.setHeraus(Boolean.TRUE);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzungHeraus2);

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzungHinein3 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzungHinein3.setIsKreuzung(false);
        fahrbeziehungKreuzungHinein3.setKnotenarm(3);
        fahrbeziehungKreuzungHinein3.setHinein(Boolean.TRUE);
        fahrbeziehungKreuzungHinein3.setHeraus(Boolean.FALSE);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzungHinein3);

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzungHeraus4 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzungHeraus4.setIsKreuzung(false);
        fahrbeziehungKreuzungHeraus4.setKnotenarm(4);
        fahrbeziehungKreuzungHeraus4.setHinein(Boolean.FALSE);
        fahrbeziehungKreuzungHeraus4.setHeraus(Boolean.TRUE);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzungHeraus4);

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungKreuzungVorbei4 = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungKreuzungVorbei4.setIsKreuzung(false);
        fahrbeziehungKreuzungVorbei4.setKnotenarm(4);
        fahrbeziehungKreuzungVorbei4.setHinein(Boolean.FALSE);
        fahrbeziehungKreuzungVorbei4.setHeraus(Boolean.FALSE);
        fahrbeziehungKreuzungVorbei4.setVorbei(Boolean.TRUE);
        ladeZaehlung.getVerkehrsbeziehungen().add(fahrbeziehungKreuzungVorbei4);

        final FahrbeziehungenDTO optionsFahrbeziehungen = FahrbeziehungUtil.determinePossibleVerkehrsbeziehung(ladeZaehlung);

        assertThat(optionsFahrbeziehungen.getVonKnotenarme().size(), is(3));
        assertThat(optionsFahrbeziehungen.getVonKnotenarme().toArray()[0], is(1));
        assertThat(optionsFahrbeziehungen.getVonKnotenarme().toArray()[1], is(2));
        assertThat(optionsFahrbeziehungen.getVonKnotenarme().toArray()[2], is(3));

        assertThat(optionsFahrbeziehungen.getNachKnotenarme().size(), is(3));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(1).size(), is(3));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(1).toArray()[0], is(1));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(1).toArray()[1], is(2));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(1).toArray()[2], is(4));

        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(2).size(), is(3));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(2).toArray()[0], is(1));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(2).toArray()[1], is(2));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(2).toArray()[2], is(4));

        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(3).size(), is(3));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(3).toArray()[0], is(1));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(3).toArray()[1], is(2));
        assertThat(optionsFahrbeziehungen.getNachKnotenarme().get(3).toArray()[2], is(4));

    }

}
