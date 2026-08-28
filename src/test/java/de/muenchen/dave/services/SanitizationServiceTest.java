package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteKnotenarmDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.*;
import de.muenchen.dave.domain.pdf.assets.ImageAsset;
import de.muenchen.dave.domain.pdf.assets.TextAsset;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
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
        bearbeiteZaehlungDTO.setZaehlart(Zaehlart.N.toString());
        bearbeiteZaehlungDTO.setProjektNummer(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setProjektName(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setKreuzungsname(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setZaehlsituation(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setZaehlsituationErweitert(ALLOWED_HTML_1);
        bearbeiteZaehlungDTO.setWetter(Wetter.CLOUDY.toString());
        bearbeiteZaehlungDTO.setStatus(Status.INSTRUCTED.toString());
        bearbeiteZaehlungDTO.setQuelle(Quelle.MANUALLY.toString());
        bearbeiteZaehlungDTO.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN.toString());
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

        bearbeiteZaehlungDTO.setZaehlart("P");
        assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeBearbeiteZaehlungDto(bearbeiteZaehlungDTO));
        bearbeiteZaehlungDTO.setZaehlart(null);
        bearbeiteZaehlungDTO.setWetter("Rain");
        assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeBearbeiteZaehlungDto(bearbeiteZaehlungDTO));
        bearbeiteZaehlungDTO.setWetter(null);
        bearbeiteZaehlungDTO.setStatus("kein");
        assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeBearbeiteZaehlungDto(bearbeiteZaehlungDTO));
        bearbeiteZaehlungDTO.setStatus(null);
        bearbeiteZaehlungDTO.setQuelle("unbekannt");
        assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeBearbeiteZaehlungDto(bearbeiteZaehlungDTO));
        bearbeiteZaehlungDTO.setQuelle(null);
        bearbeiteZaehlungDTO.setZaehldauer("24h");
        assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeBearbeiteZaehlungDto(bearbeiteZaehlungDTO));
    }

    @Test
    void sanitizeBearbeiteZaehlstelleDto_safeInputIsKept() {
        // Harmloser HTML-Input bleibt erhalten
        BearbeiteZaehlstelleDTO bearbeiteZaehlstelleDTO = new BearbeiteZaehlstelleDTO();
        bearbeiteZaehlstelleDTO.setStadtbezirk(Stadtbezirk.MOOSACH.toString());
        bearbeiteZaehlstelleDTO.setKommentar(ALLOWED_HTML_2);
        bearbeiteZaehlstelleDTO.setCustomSuchwoerter(List.of(ALLOWED_HTML_1, ALLOWED_HTML_2));

        sanitizationService.sanitizeBearbeiteZaehlstelleDto(bearbeiteZaehlstelleDTO);

        assertThat(bearbeiteZaehlstelleDTO.getStadtbezirk(), is(Stadtbezirk.MOOSACH.toString()));
        assertThat(bearbeiteZaehlstelleDTO.getKommentar(), is(ALLOWED_HTML_2));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().size(), is(2));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().get(0), is(ALLOWED_HTML_1));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().get(1), is(ALLOWED_HTML_2));
    }

    @Test
    void sanitizeBearbeiteZaehlstelleDto_unsafeInputIsRemoved() {
        // Schädlicher / nicht erlaubter HTML-Input wird entfernt
        BearbeiteZaehlstelleDTO bearbeiteZaehlstelleDTO = new BearbeiteZaehlstelleDTO();
        bearbeiteZaehlstelleDTO.setKommentar(NOT_ALLOWED_HTML_2);
        bearbeiteZaehlstelleDTO.setCustomSuchwoerter(List.of(NOT_ALLOWED_HTML_3, NOT_ALLOWED_HTML_4));

        sanitizationService.sanitizeBearbeiteZaehlstelleDto(bearbeiteZaehlstelleDTO);

        assertThat(bearbeiteZaehlstelleDTO.getKommentar(), is(EXPECTED_2));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().size(), is(2));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().get(0), is(EXPECTED_3));
        assertThat(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().get(1), is(EXPECTED_4));

        bearbeiteZaehlstelleDTO.setStadtbezirk("Südkreuz");
        assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeBearbeiteZaehlstelleDto(bearbeiteZaehlstelleDTO));
    }

    @Test
    void sanitizeMessstelle_safeInputIsKept() {
        // Harmloser HTML-Input bleibt erhalten
        Messstelle messstelle = new Messstelle();
        messstelle.setName(ALLOWED_HTML_1);
        messstelle.setBemerkung(ALLOWED_HTML_1);
        messstelle.setHersteller(ALLOWED_HTML_1);
        messstelle.setKommentar(ALLOWED_HTML_1);
        messstelle.setStandort(ALLOWED_HTML_1);
        messstelle.setSuchwoerter(List.of(ALLOWED_HTML_1));
        messstelle.setCustomSuchwoerter(List.of(ALLOWED_HTML_1));

        sanitizationService.sanitizeMessstelle(messstelle);

        assertThat(messstelle.getName(), is(ALLOWED_HTML_1));
        assertThat(messstelle.getBemerkung(), is(ALLOWED_HTML_1));
        assertThat(messstelle.getHersteller(), is(ALLOWED_HTML_1));
        assertThat(messstelle.getKommentar(), is(ALLOWED_HTML_1));
        assertThat(messstelle.getStandort(), is(ALLOWED_HTML_1));
        assertThat(messstelle.getSuchwoerter().size(), is(1));
        assertThat(messstelle.getSuchwoerter().getFirst(), is(ALLOWED_HTML_1));
        assertThat(messstelle.getCustomSuchwoerter().size(), is(1));
        assertThat(messstelle.getCustomSuchwoerter().getFirst(), is(ALLOWED_HTML_1));
    }

    @Test
    void sanitizeMessstelle_unsafeInputIsRemoved() {
        // Schädlicher / nicht erlaubter HTML-Input wird entfernt
        Messstelle messstelle = new Messstelle();
        messstelle.setName(NOT_ALLOWED_HTML_1);
        messstelle.setBemerkung(NOT_ALLOWED_HTML_2);
        messstelle.setHersteller(NOT_ALLOWED_HTML_4);
        messstelle.setKommentar(NOT_ALLOWED_HTML_1);
        messstelle.setStandort(NOT_ALLOWED_HTML_2);
        messstelle.setSuchwoerter(List.of(NOT_ALLOWED_HTML_3));
        messstelle.setCustomSuchwoerter(List.of(NOT_ALLOWED_HTML_4));

        sanitizationService.sanitizeMessstelle(messstelle);

        assertThat(messstelle.getName(), is(EXPECTED_1));
        assertThat(messstelle.getBemerkung(), is(EXPECTED_2));
        assertThat(messstelle.getHersteller(), is(EXPECTED_4));
        assertThat(messstelle.getKommentar(), is(EXPECTED_1));
        assertThat(messstelle.getStandort(), is(EXPECTED_2));
        assertThat(messstelle.getSuchwoerter().size(), is(1));
        assertThat(messstelle.getSuchwoerter().getFirst(), is(EXPECTED_3));
        assertThat(messstelle.getCustomSuchwoerter().size(), is(1));
        assertThat(messstelle.getCustomSuchwoerter().getFirst(), is(EXPECTED_4));
    }

    @Test
    void sanitizeMessquerschnitt_safeInputIsKept() {
        // Harmloser HTML-Input bleibt erhalten
        Messquerschnitt messquerschnitt = new Messquerschnitt();
        messquerschnitt.setStrassenname(ALLOWED_HTML_1);
        messquerschnitt.setLageMessquerschnitt(ALLOWED_HTML_2);
        messquerschnitt.setFahrtrichtung(ALLOWED_HTML_1);
        messquerschnitt.setStandort(ALLOWED_HTML_2);

        sanitizationService.sanitizeMessquerschnitt(messquerschnitt);

        assertThat(messquerschnitt.getStrassenname(), is(ALLOWED_HTML_1));
        assertThat(messquerschnitt.getLageMessquerschnitt(), is(ALLOWED_HTML_2));
        assertThat(messquerschnitt.getFahrtrichtung(), is(ALLOWED_HTML_1));
        assertThat(messquerschnitt.getStandort(), is(ALLOWED_HTML_2));
    }

    @Test
    void sanitizeMessquerschnitt_unsafeInputIsRemoved() {
        // Schädlicher / nicht erlaubter HTML-Input wird entfernt
        Messquerschnitt messquerschnitt = new Messquerschnitt();
        messquerschnitt.setStrassenname(NOT_ALLOWED_HTML_1);
        messquerschnitt.setLageMessquerschnitt(NOT_ALLOWED_HTML_2);
        messquerschnitt.setFahrtrichtung(NOT_ALLOWED_HTML_3);
        messquerschnitt.setStandort(NOT_ALLOWED_HTML_4);

        sanitizationService.sanitizeMessquerschnitt(messquerschnitt);

        assertThat(messquerschnitt.getStrassenname(), is(EXPECTED_1));
        assertThat(messquerschnitt.getLageMessquerschnitt(), is(EXPECTED_2));
        assertThat(messquerschnitt.getFahrtrichtung(), is(EXPECTED_3));
        assertThat(messquerschnitt.getStandort(), is(EXPECTED_4));
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

    @Test
    public void sanitizeImageUri() {
        // Leere Image URI
        ImageAsset asset1 = new ImageAsset();
        asset1.setImage("");
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeImageUri(asset1));
        assertThat(ex1.getMessage(), is("Image is empty"));

        // Keine Image URI
        ImageAsset asset2 = new ImageAsset();
        asset2.setImage("data:text/plain;");
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeImageUri(asset2));
        assertThat(ex2.getMessage(), is("Only image data URIs are allowed"));

        // Ungültige Image URI
        ImageAsset asset3 = new ImageAsset();
        asset3.setImage("data:image/png;base642141");
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeImageUri(asset3));
        assertThat(ex3.getMessage(), is("Invalid data URI"));

        // Falscher MIME-Type im Header
        ImageAsset asset4 = new ImageAsset();
        asset4.setImage("data:image/svg+xml;base64,");
        IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeImageUri(asset4));
        assertThat(ex4.getMessage(), is("Unsupported image type"));

        // Leerer base64 Teil der URI
        ImageAsset asset5 = new ImageAsset();
        asset5.setImage("data:image/png;base64,");
        IllegalArgumentException ex5 = assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeImageUri(asset5));
        assertThat(ex5.getMessage(), is("Invalid data URI"));

        // Image ist zu groß
        ImageAsset asset6 = new ImageAsset();
        asset6.setImage(createTooLargeTestImage());
        IllegalArgumentException ex6 = assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeImageUri(asset6));
        assertThat(ex6.getMessage(), is("Image exceeds maximum size"));

        // Dimension width des Images ist zu groß
        ImageAsset asset7 = new ImageAsset();
        asset7.setImage(createTestImageURI(12000, 1000));
        IllegalArgumentException ex7 = assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeImageUri(asset7));
        assertThat(ex7.getMessage(), is("Image dimensions too large"));

        // Dimension height des Images ist zu groß
        ImageAsset asset8 = new ImageAsset();
        asset8.setImage(createTestImageURI(1000, 12000));
        IllegalArgumentException ex8 = assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeImageUri(asset8));
        assertThat(ex8.getMessage(), is("Image dimensions too large"));

        // Image hat zu große Auflösung
        ImageAsset asset9 = new ImageAsset();
        asset9.setImage(createTestImageURI(8000, 7000));
        IllegalArgumentException ex9 = assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeImageUri(asset9));
        assertThat(ex9.getMessage(), is("Image has too many pixels"));

        // Falscher MIME-Type im Byte-String
        ImageAsset asset10 = new ImageAsset();
        String imageUriWithWrongMimeType = createTestSvgImageURIWithPngInHeader();
        asset10.setImage(imageUriWithWrongMimeType);
        IllegalArgumentException ex10 = assertThrows(IllegalArgumentException.class, () -> sanitizationService.sanitizeImageUri(asset10));
        assertThat(ex10.getMessage(), is("Unsupported image type: image/svg+xml"));

        // Zufällig erzeugtes Test-Bild wird akzeptiert
        ImageAsset asset11 = new ImageAsset();
        String testImageUri = createTestImageURI(2000, 2000);
        asset11.setImage(testImageUri);
        sanitizationService.sanitizeImageUri(asset11);
        assertThat(asset11.getImage(), is(testImageUri));

    }

    /**
     * Erzeugt eine Image URI zum Testen mit den übergebenen Werten für Höhe und Breite des Images.
     *
     * @param width Breite des Images
     * @param height Höhe des Images
     * @return Image URI
     */
    public String createTestImageURI(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
    }

    /**
     * Erzeugt eine Image URI eines sehr großen Images zum Testen.
     *
     * @return Image URI
     */
    public String createTooLargeTestImage() {
        BufferedImage image = new BufferedImage(5000, 5000, BufferedImage.TYPE_INT_RGB);

        Random random = new Random();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = random.nextInt(0xFFFFFF);
                image.setRGB(x, y, rgb);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
    }

    /**
     * Erzeugt eine Image URI eines SVG-Bildes. Im Header der Image URI steht 'png' statt 'svg+xml'.
     *
     * @return Image URI
     */
    public String createTestSvgImageURIWithPngInHeader() {
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg">
                    <rect width="100" height="100"/>
                </svg>
                """;

        return "data:image/png;base64," +
                Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }
}
