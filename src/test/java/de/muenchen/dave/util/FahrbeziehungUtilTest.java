package de.muenchen.dave.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteFahrbeziehungDTO;
import de.muenchen.dave.domain.dtos.laden.FahrbeziehungenDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungDTO;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class FahrbeziehungUtilTest {

    @Test
    public void determinePossibleFahrbeziehungenKreuzung() {
        final LadeZaehlungDTO ladeZaehlung = new LadeZaehlungDTO();
        ladeZaehlung.setKreisverkehr(Boolean.FALSE);
        ladeZaehlung.setFahrbeziehungen(new ArrayList<>());

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzung1nach1 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzung1nach1.setIsKreuzung(true);
        fahrbeziehungKreuzung1nach1.setVon(1);
        fahrbeziehungKreuzung1nach1.setNach(1);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzung1nach1);

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzung1nach2 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzung1nach2.setIsKreuzung(true);
        fahrbeziehungKreuzung1nach2.setVon(1);
        fahrbeziehungKreuzung1nach2.setNach(2);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzung1nach2);

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzung1nach3 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzung1nach3.setIsKreuzung(true);
        fahrbeziehungKreuzung1nach3.setVon(1);
        fahrbeziehungKreuzung1nach3.setNach(3);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzung1nach3);

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzung2nach2 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzung2nach2.setIsKreuzung(true);
        fahrbeziehungKreuzung2nach2.setVon(2);
        fahrbeziehungKreuzung2nach2.setNach(2);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzung2nach2);

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzung2nach3 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzung2nach3.setIsKreuzung(true);
        fahrbeziehungKreuzung2nach3.setVon(2);
        fahrbeziehungKreuzung2nach3.setNach(3);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzung2nach3);

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzung3nach1 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzung3nach1.setIsKreuzung(true);
        fahrbeziehungKreuzung3nach1.setVon(3);
        fahrbeziehungKreuzung3nach1.setNach(1);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzung3nach1);

        final FahrbeziehungenDTO optionsFahrbeziehungen = FahrbeziehungUtil.determinePossibleFahrbeziehungen(ladeZaehlung);

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
        ladeZaehlung.setFahrbeziehungen(new ArrayList<>());

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzungHinein1 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzungHinein1.setIsKreuzung(false);
        fahrbeziehungKreuzungHinein1.setKnotenarm(1);
        fahrbeziehungKreuzungHinein1.setHinein(Boolean.TRUE);
        fahrbeziehungKreuzungHinein1.setHeraus(Boolean.FALSE);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzungHinein1);

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzungHeraus1 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzungHeraus1.setIsKreuzung(false);
        fahrbeziehungKreuzungHeraus1.setKnotenarm(1);
        fahrbeziehungKreuzungHeraus1.setHinein(Boolean.FALSE);
        fahrbeziehungKreuzungHeraus1.setHeraus(Boolean.TRUE);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzungHeraus1);

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzungHinein2 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzungHinein2.setIsKreuzung(false);
        fahrbeziehungKreuzungHinein2.setKnotenarm(2);
        fahrbeziehungKreuzungHinein2.setHinein(Boolean.TRUE);
        fahrbeziehungKreuzungHinein2.setHeraus(Boolean.FALSE);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzungHinein2);

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzungHeraus2 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzungHeraus2.setIsKreuzung(false);
        fahrbeziehungKreuzungHeraus2.setKnotenarm(2);
        fahrbeziehungKreuzungHeraus2.setHinein(Boolean.FALSE);
        fahrbeziehungKreuzungHeraus2.setHeraus(Boolean.TRUE);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzungHeraus2);

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzungHinein3 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzungHinein3.setIsKreuzung(false);
        fahrbeziehungKreuzungHinein3.setKnotenarm(3);
        fahrbeziehungKreuzungHinein3.setHinein(Boolean.TRUE);
        fahrbeziehungKreuzungHinein3.setHeraus(Boolean.FALSE);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzungHinein3);

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzungHeraus4 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzungHeraus4.setIsKreuzung(false);
        fahrbeziehungKreuzungHeraus4.setKnotenarm(4);
        fahrbeziehungKreuzungHeraus4.setHinein(Boolean.FALSE);
        fahrbeziehungKreuzungHeraus4.setHeraus(Boolean.TRUE);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzungHeraus4);

        final BearbeiteFahrbeziehungDTO fahrbeziehungKreuzungVorbei4 = new BearbeiteFahrbeziehungDTO();
        fahrbeziehungKreuzungVorbei4.setIsKreuzung(false);
        fahrbeziehungKreuzungVorbei4.setKnotenarm(4);
        fahrbeziehungKreuzungVorbei4.setHinein(Boolean.FALSE);
        fahrbeziehungKreuzungVorbei4.setHeraus(Boolean.FALSE);
        fahrbeziehungKreuzungVorbei4.setVorbei(Boolean.TRUE);
        ladeZaehlung.getFahrbeziehungen().add(fahrbeziehungKreuzungVorbei4);

        final FahrbeziehungenDTO optionsFahrbeziehungen = FahrbeziehungUtil.determinePossibleFahrbeziehungen(ladeZaehlung);

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
