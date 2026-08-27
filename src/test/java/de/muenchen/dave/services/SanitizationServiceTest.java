package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteKnotenarmDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.pdf.assets.TextAsset;
import java.util.List;
import org.junit.jupiter.api.Test;

class SanitizationServiceTest {

    private final SanitizationService sanitizationService = new SanitizationService();

    public static final String ALLOWED_HTML_1 = "<p>Hello <strong>World</strong>. Visit <a href=\"https://example.com\">Link</a> or <a href=\"mailto:foo@example.com\">Email</a></p>";
    public static final String ALLOWED_HTML_2 = "Knotenarme:<br />1 1<br />2 2<br />";

    public static final String NOT_ALLOWED_HTML_1 = "<p>Click <a href=\"javascript:alert('XSS')\">here</a></p><script>alert('x')</script>";
    public static final String NOT_ALLOWED_HTML_2 = "<p onclick=\"doEvil()\" style=\"color:red\" class=\"foo\">Hi</p>";
    public static final String NOT_ALLOWED_HTML_3 = "Before<img src=\"https://example.com/pic.png\" alt=\"pic\">After";
    public static final String NOT_ALLOWED_HTML_4 = "<a href=\"/local/path\">Local</a>";
    public static final String EXPECTED_1 = "<p>Click <a>here</a></p>"; // href mit nicht erlaubtem uri scheme sowie script tags werden entfernt
    public static final String EXPECTED_2 = "<p>Hi</p>"; // onclick wird entfernt
    public static final String EXPECTED_3 = "BeforeAfter"; // img wird entfernt
    public static final String EXPECTED_4 = "<a>Local</a>"; // relative URL wird entfernt

    @Test
    void sanitizeAllowedHtml_safeInputIsKept() {
        // Harmloser HTML-Input bleibt erhalten
        assertThat(sanitizationService.sanitizeAllowedHtml(ALLOWED_HTML_1), is(ALLOWED_HTML_1));

        // Closing Tags (z.B. <br />) bleiben erhalten und werden nicht umgewandelt (z.B. <br /> -> <br> => Fehler bei der PDF-Generierung)
        assertThat(sanitizationService.sanitizeAllowedHtml(ALLOWED_HTML_2), is(ALLOWED_HTML_2));
    }

    @Test
    void sanitizeAllowedHtml_unsafeInputIsRemoved() {
        // Schädlicher / nicht erlaubter HTML-Input wird entfernt
        assertThat(sanitizationService.sanitizeAllowedHtml(NOT_ALLOWED_HTML_1), is(EXPECTED_1));
        assertThat(sanitizationService.sanitizeAllowedHtml(NOT_ALLOWED_HTML_2), is(EXPECTED_2));
        assertThat(sanitizationService.sanitizeAllowedHtml(NOT_ALLOWED_HTML_3), is(EXPECTED_3));
        assertThat(sanitizationService.sanitizeAllowedHtml(NOT_ALLOWED_HTML_4), is(EXPECTED_4));
    }

    @Test
    void sanitizeBearbeiteZaehlungDto_safeInputIsKept() {
        // Harmloser HTML-Input bleibt erhalten
        BearbeiteZaehlungDTO bearbeiteZaehlungDTO = new BearbeiteZaehlungDTO();
        bearbeiteZaehlungDTO.setProjektNummer(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setProjektName(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setKreuzungsname(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setZaehlsituation(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setZaehlsituationErweitert(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setSchulZeiten(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setKommentar(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setDienstleisterkennung(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setCustomSuchwoerter(List.of(ALLOWED_HTML_1));
        BearbeiteKnotenarmDTO knotenarmDTO = new BearbeiteKnotenarmDTO();
        knotenarmDTO.setStrassenname(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setKnotenarme(List.of(knotenarmDTO));

        sanitizationService.sanitizeBearbeiteZaehlungDto(bearbeiteZaehlungDTO);

        assertThat(bearbeiteZaehlungDTO.getProjektNummer(), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlungDTO.getProjektName(), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlungDTO.getKreuzungsname(), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlungDTO.getZaehlsituation(), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlungDTO.getZaehlsituationErweitert(), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlungDTO.getSchulZeiten(), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlungDTO.getKommentar(), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlungDTO.getDienstleisterkennung(), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlungDTO.getCustomSuchwoerter().size(), is(1));
        assertThat(bearbeiteZaehlungDTO.getCustomSuchwoerter().getFirst(), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlungDTO.getKnotenarme().size(), is(1));
        assertThat(bearbeiteZaehlungDTO.getKnotenarme().getFirst().getStrassenname(), is(ALLOWED_HTML_1));
    }

    @Test
    void sanitizeBearbeiteZaehlungDto_unsafeInputIsRemoved() {
        // Schädlicher / nicht erlaubter HTML-Input wird entfernt
        BearbeiteZaehlungDTO bearbeiteZaehlungDTO = new BearbeiteZaehlungDTO();
        bearbeiteZaehlungDTO.setProjektNummer(NOT_ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setProjektName(NOT_ALLOWED_HTML_2);
        bearbeiteZaehlungDTO.setKreuzungsname(NOT_ALLOWED_HTML_3);
        bearbeiteZaehlungDTO.setZaehlsituation(NOT_ALLOWED_HTML_4);
        bearbeiteZaehlungDTO.setZaehlsituationErweitert(NOT_ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setSchulZeiten(NOT_ALLOWED_HTML_2);
        bearbeiteZaehlungDTO.setKommentar(NOT_ALLOWED_HTML_3);
        bearbeiteZaehlungDTO.setDienstleisterkennung(NOT_ALLOWED_HTML_4);
        bearbeiteZaehlungDTO.setCustomSuchwoerter(List.of(NOT_ALLOWED_HTML_1));
        BearbeiteKnotenarmDTO knotenarmDTO = new BearbeiteKnotenarmDTO();
        knotenarmDTO.setStrassenname(NOT_ALLOWED_HTML_2);
        bearbeiteZaehlungDTO.setKnotenarme(List.of(knotenarmDTO));

        sanitizationService.sanitizeBearbeiteZaehlungDto(bearbeiteZaehlungDTO);

        assertThat(bearbeiteZaehlungDTO.getProjektNummer(), is(EXPECTED_1));
        assertThat(bearbeiteZaehlungDTO.getProjektName(), is(EXPECTED_2));
        assertThat(bearbeiteZaehlungDTO.getKreuzungsname(), is(EXPECTED_3));
        assertThat(bearbeiteZaehlungDTO.getZaehlsituation(), is(EXPECTED_4));
        assertThat(bearbeiteZaehlungDTO.getZaehlsituationErweitert(), is(EXPECTED_1));
        assertThat(bearbeiteZaehlungDTO.getSchulZeiten(), is(EXPECTED_2));
        assertThat(bearbeiteZaehlungDTO.getKommentar(), is(EXPECTED_3));
        assertThat(bearbeiteZaehlungDTO.getDienstleisterkennung(), is(EXPECTED_4));
        assertThat(bearbeiteZaehlungDTO.getCustomSuchwoerter().size(), is(1));
        assertThat(bearbeiteZaehlungDTO.getCustomSuchwoerter().getFirst(), is(EXPECTED_1));
        assertThat(bearbeiteZaehlungDTO.getKnotenarme().size(), is(1));
        assertThat(bearbeiteZaehlungDTO.getKnotenarme().getFirst().getStrassenname(), is(EXPECTED_2));
    }

    @Test
    void sanitizeBearbeiteZaehlstelleDto_safeInputIsKept() {
        // Harmloser HTML-Input bleibt erhalten
        BearbeiteZaehlstelleDTO bearbeiteZaehlstelleDTO = new BearbeiteZaehlstelleDTO();
        bearbeiteZaehlstelleDTO.setStadtbezirk(ALLOWED_HTML_1);
        bearbeiteZaehlstelleDTO.setKommentar(ALLOWED_HTML_2);
        bearbeiteZaehlstelleDTO.setCustomSuchwoerter(List.of(ALLOWED_HTML_1, ALLOWED_HTML_2));

        sanitizationService.sanitizeBearbeiteZaehlstelleDto(bearbeiteZaehlstelleDTO);

        assertThat(bearbeiteZaehlstelleDTO.getStadtbezirk(), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlstelleDTO.getKommentar(), is(ALLOWED_HTML_2));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().size(), is(2));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().get(0), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().get(1), is(ALLOWED_HTML_2));
    }

    @Test
    void sanitizeBearbeiteZaehlstelleDto_unsafeInputIsRemoved() {
        // Schädlicher / nicht erlaubter HTML-Input wird entfernt
        BearbeiteZaehlstelleDTO bearbeiteZaehlstelleDTO = new BearbeiteZaehlstelleDTO();
        bearbeiteZaehlstelleDTO.setStadtbezirk(NOT_ALLOWED_HTML_1);
        bearbeiteZaehlstelleDTO.setKommentar(NOT_ALLOWED_HTML_2);
        bearbeiteZaehlstelleDTO.setCustomSuchwoerter(List.of(NOT_ALLOWED_HTML_3, NOT_ALLOWED_HTML_4));

        sanitizationService.sanitizeBearbeiteZaehlstelleDto(bearbeiteZaehlstelleDTO);

        assertThat(bearbeiteZaehlstelleDTO.getStadtbezirk(), is(EXPECTED_1));
        assertThat(bearbeiteZaehlstelleDTO.getKommentar(), is(EXPECTED_2));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().size(), is(2));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().get(0), is(EXPECTED_3));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().get(1), is(EXPECTED_4));
    }

    @Test
    void sanitizeEditMessstelleDto_safeInputIsKept() {
        // Harmloser HTML-Input bleibt erhalten
        EditMessstelleDTO editMessstelleDTO = new EditMessstelleDTO();
        editMessstelleDTO.setName(ALLOWED_HTML_1);
        editMessstelleDTO.setBemerkung(ALLOWED_HTML_1);
        editMessstelleDTO.setStadtbezirk(ALLOWED_HTML_1);
        editMessstelleDTO.setHersteller(ALLOWED_HTML_1);
        editMessstelleDTO.setKommentar(ALLOWED_HTML_1);
        editMessstelleDTO.setStandort(ALLOWED_HTML_1);
        editMessstelleDTO.setCustomSuchwoerter(List.of(ALLOWED_HTML_1));

        sanitizationService.sanitizeEditMessstelleDto(editMessstelleDTO);

        assertThat(editMessstelleDTO.getName(), is(ALLOWED_HTML_1));
        assertThat(editMessstelleDTO.getBemerkung(), is(ALLOWED_HTML_1));
        assertThat(editMessstelleDTO.getStadtbezirk(), is(ALLOWED_HTML_1));
        assertThat(editMessstelleDTO.getHersteller(), is(ALLOWED_HTML_1));
        assertThat(editMessstelleDTO.getKommentar(), is(ALLOWED_HTML_1));
        assertThat(editMessstelleDTO.getStandort(), is(ALLOWED_HTML_1));
        assertThat(editMessstelleDTO.getCustomSuchwoerter().size(), is(1));
        assertThat(editMessstelleDTO.getCustomSuchwoerter().getFirst(), is(ALLOWED_HTML_1));
    }

    @Test
    void sanitizeEditMessstelleDto_unsafeInputIsRemoved() {
        // Schädlicher / nicht erlaubter HTML-Input wird entfernt
        EditMessstelleDTO editMessstelleDTO = new EditMessstelleDTO();
        editMessstelleDTO.setName(NOT_ALLOWED_HTML_1);
        editMessstelleDTO.setBemerkung(NOT_ALLOWED_HTML_2);
        editMessstelleDTO.setStadtbezirk(NOT_ALLOWED_HTML_3);
        editMessstelleDTO.setHersteller(NOT_ALLOWED_HTML_4);
        editMessstelleDTO.setKommentar(NOT_ALLOWED_HTML_1);
        editMessstelleDTO.setStandort(NOT_ALLOWED_HTML_2);
        editMessstelleDTO.setCustomSuchwoerter(List.of(NOT_ALLOWED_HTML_3));

        sanitizationService.sanitizeEditMessstelleDto(editMessstelleDTO);

        assertThat(editMessstelleDTO.getName(), is(EXPECTED_1));
        assertThat(editMessstelleDTO.getBemerkung(), is(EXPECTED_2));
        assertThat(editMessstelleDTO.getStadtbezirk(), is(EXPECTED_3));
        assertThat(editMessstelleDTO.getHersteller(), is(EXPECTED_4));
        assertThat(editMessstelleDTO.getKommentar(), is(EXPECTED_1));
        assertThat(editMessstelleDTO.getStandort(), is(EXPECTED_2));
        assertThat(editMessstelleDTO.getCustomSuchwoerter().size(), is(1));
        assertThat(editMessstelleDTO.getCustomSuchwoerter().getFirst(), is(EXPECTED_3));
    }

    @Test
    void sanitizeEditMessquerschnittDto_safeInputIsKept() {
        // Harmloser HTML-Input bleibt erhalten
        EditMessquerschnittDTO editMessquerschnittDTO = new EditMessquerschnittDTO();
        editMessquerschnittDTO.setStrassenname(ALLOWED_HTML_1);
        editMessquerschnittDTO.setLageMessquerschnitt(ALLOWED_HTML_2);
        editMessquerschnittDTO.setHersteller(ALLOWED_HTML_1);
        editMessquerschnittDTO.setStandort(ALLOWED_HTML_2);

        sanitizationService.sanitizeEditMessquerschnittDto(editMessquerschnittDTO);

        assertThat(editMessquerschnittDTO.getStrassenname(), is(ALLOWED_HTML_1));
        assertThat(editMessquerschnittDTO.getLageMessquerschnitt(), is(ALLOWED_HTML_2));
        assertThat(editMessquerschnittDTO.getHersteller(), is(ALLOWED_HTML_1));
        assertThat(editMessquerschnittDTO.getStandort(), is(ALLOWED_HTML_2));
    }

    @Test
    void sanitizeEditMessquerschnittDto_unsafeInputIsRemoved() {
        // Schädlicher / nicht erlaubter HTML-Input wird entfernt
        EditMessquerschnittDTO editMessquerschnittDTO = new EditMessquerschnittDTO();
        editMessquerschnittDTO.setStrassenname(NOT_ALLOWED_HTML_1);
        editMessquerschnittDTO.setLageMessquerschnitt(NOT_ALLOWED_HTML_2);
        editMessquerschnittDTO.setHersteller(NOT_ALLOWED_HTML_3);
        editMessquerschnittDTO.setStandort(NOT_ALLOWED_HTML_4);

        sanitizationService.sanitizeEditMessquerschnittDto(editMessquerschnittDTO);

        assertThat(editMessquerschnittDTO.getStrassenname(), is(EXPECTED_1));
        assertThat(editMessquerschnittDTO.getLageMessquerschnitt(), is(EXPECTED_2));
        assertThat(editMessquerschnittDTO.getHersteller(), is(EXPECTED_3));
        assertThat(editMessquerschnittDTO.getStandort(), is(EXPECTED_4));
    }

    @Test
    void sanitizeTextAsset_safeInputIsKept() {
        // Harmloser HTML-Input bleibt erhalten
        TextAsset assetWithAllowedHtml = new TextAsset();
        assetWithAllowedHtml.setText(ALLOWED_HTML_1);
        sanitizationService.sanitizeTextAsset(assetWithAllowedHtml);
        assertThat(assetWithAllowedHtml.getText(), is(ALLOWED_HTML_1));

        // Closing Tags (z.B. <br />) bleiben erhalten und werden nicht umgewandelt (z.B. <br /> -> <br> => Fehler bei der PDF-Generierung)
        TextAsset assetWithAllowedHtml2 = new TextAsset();
        assetWithAllowedHtml2.setText(ALLOWED_HTML_2);
        sanitizationService.sanitizeTextAsset(assetWithAllowedHtml2);
        assertThat(assetWithAllowedHtml2.getText(), is(ALLOWED_HTML_2));
    }

    @Test
    void sanitizeTextAsset_unsafeInputIsRemoved() {
        // Schädlicher / nicht erlaubter HTML-Input wird entfernt
        TextAsset assetWithNotAllowedHtml1 = new TextAsset();
        assetWithNotAllowedHtml1.setText(NOT_ALLOWED_HTML_1);
        sanitizationService.sanitizeTextAsset(assetWithNotAllowedHtml1);
        assertThat(assetWithNotAllowedHtml1.getText(), is(EXPECTED_1));

        TextAsset assetWithNotAllowedHtml2 = new TextAsset();
        assetWithNotAllowedHtml2.setText(NOT_ALLOWED_HTML_2);
        sanitizationService.sanitizeTextAsset(assetWithNotAllowedHtml2);
        assertThat(assetWithNotAllowedHtml2.getText(), is(EXPECTED_2));

        TextAsset assetWithNotAllowedHtml3 = new TextAsset();
        assetWithNotAllowedHtml3.setText(NOT_ALLOWED_HTML_3);
        sanitizationService.sanitizeTextAsset(assetWithNotAllowedHtml3);
        assertThat(assetWithNotAllowedHtml3.getText(), is(EXPECTED_3));

        TextAsset assetWithNotAllowedHtml4 = new TextAsset();
        assetWithNotAllowedHtml4.setText(NOT_ALLOWED_HTML_4);
        sanitizationService.sanitizeTextAsset(assetWithNotAllowedHtml4);
        assertThat(assetWithNotAllowedHtml4.getText(), is(EXPECTED_4));
    }
}
