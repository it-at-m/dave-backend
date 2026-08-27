package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.pdf.assets.ImageAsset;
import de.muenchen.dave.domain.pdf.assets.TextAsset;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;

import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

@Service
public class SanitizationService {

    // Image URI Validierung
    public static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    public static final int MAX_WIDTH = 10000;
    public static final int MAX_HEIGHT = 10000;
    public static final long MAX_PIXELS = 50_000_000L; // 50 Megapixel
    public static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/png", "image/jpeg", "image/jpg");

    /**
     * Filtert aus dem übergebenen {@link String} unerwünschten HTML-Code heraus.
     *
     * @param inputHtml Der zu bereinigende HTML-{@link String}
     * @return Den bereinigten HTML-{@link String}
     */
    public String sanitizeAllowedHtml(final String inputHtml) {
        if (inputHtml == null) {
            return null;
        }

        Safelist safelist = Safelist.basic();
        safelist.removeEnforcedAttribute("a", "rel");
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.prettyPrint(false);

        String cleanedHtml = Jsoup.clean(inputHtml, "", safelist, settings);

        Document doc = Jsoup.parseBodyFragment(cleanedHtml);
        doc.outputSettings().prettyPrint(false).syntax(Document.OutputSettings.Syntax.xml);

        return doc.body().html();
    }

    /**
     * Bereinigt die Attribute vom Typ {@link String} eines {@link BearbeiteZaehlungDTO}. Unerwünschter
     * HTML-Code wird entfernt.
     *
     * @param zaehlungDTO Das {@link BearbeiteZaehlungDTO}, dessen Attribute bereinigt werden sollen
     */
    public void sanitizeBearbeiteZaehlungDto(final BearbeiteZaehlungDTO zaehlungDTO) {
        if (zaehlungDTO == null) return;
        zaehlungDTO.setProjektNummer(sanitizeAllowedHtml(zaehlungDTO.getProjektNummer()));
        zaehlungDTO.setProjektName(sanitizeAllowedHtml(zaehlungDTO.getProjektName()));
        zaehlungDTO.setKreuzungsname(sanitizeAllowedHtml(zaehlungDTO.getKreuzungsname()));
        zaehlungDTO.setZaehlsituation(sanitizeAllowedHtml(zaehlungDTO.getZaehlsituation()));
        zaehlungDTO.setZaehlsituationErweitert(sanitizeAllowedHtml(zaehlungDTO.getZaehlsituationErweitert()));
        zaehlungDTO.setSchulZeiten(sanitizeAllowedHtml(zaehlungDTO.getSchulZeiten()));
        zaehlungDTO.setKommentar(sanitizeAllowedHtml(zaehlungDTO.getKommentar()));
        zaehlungDTO.setDienstleisterkennung(sanitizeAllowedHtml(zaehlungDTO.getDienstleisterkennung()));

        if (zaehlungDTO.getCustomSuchwoerter() != null) {
            zaehlungDTO.setCustomSuchwoerter(zaehlungDTO.getCustomSuchwoerter().stream()
                    .filter(Objects::nonNull)
                    .map(this::sanitizeAllowedHtml)
                    .toList());
        }

        if (zaehlungDTO.getKnotenarme() != null) {
            zaehlungDTO.getKnotenarme().stream()
                    .filter(Objects::nonNull)
                    .forEach((knotenarmDTO -> knotenarmDTO.setStrassenname(sanitizeAllowedHtml(knotenarmDTO.getStrassenname()))));
        }
    }

    /**
     * Bereinigt die Attribute vom Typ {@link String} eines {@link BearbeiteZaehlstelleDTO}.
     * Unerwünschter HTML-Code wird entfernt.
     *
     * @param bearbeiteZaehlstelleDTO Das {@link BearbeiteZaehlstelleDTO}, dessen Attribute bereinigt
     *            werden sollen
     */
    public void sanitizeBearbeiteZaehlstelleDto(final BearbeiteZaehlstelleDTO bearbeiteZaehlstelleDTO) {
        if (bearbeiteZaehlstelleDTO == null) return;
        bearbeiteZaehlstelleDTO.setStadtbezirk(sanitizeAllowedHtml(bearbeiteZaehlstelleDTO.getStadtbezirk()));
        bearbeiteZaehlstelleDTO.setKommentar(sanitizeAllowedHtml(bearbeiteZaehlstelleDTO.getKommentar()));

        if (bearbeiteZaehlstelleDTO.getCustomSuchwoerter() != null) {
            bearbeiteZaehlstelleDTO.setCustomSuchwoerter(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().stream()
                    .filter(Objects::nonNull)
                    .map(this::sanitizeAllowedHtml)
                    .toList());
        }
    }

    /**
     * Bereinigt die Attribute vom Typ {@link String} eines {@link EditMessstelleDTO}. Unerwünschter
     * HTML-Code wird entfernt.
     *
     * @param messstelleDTO Das {@link EditMessstelleDTO}, dessen Attribute bereinigt werden sollen
     */
    public void sanitizeEditMessstelleDto(final EditMessstelleDTO messstelleDTO) {
        if (messstelleDTO == null) return;
        messstelleDTO.setName(sanitizeAllowedHtml(messstelleDTO.getName()));
        messstelleDTO.setBemerkung(sanitizeAllowedHtml(messstelleDTO.getBemerkung()));
        messstelleDTO.setStadtbezirk(sanitizeAllowedHtml(messstelleDTO.getStadtbezirk()));
        messstelleDTO.setHersteller(sanitizeAllowedHtml(messstelleDTO.getHersteller()));
        messstelleDTO.setKommentar(sanitizeAllowedHtml(messstelleDTO.getKommentar()));
        messstelleDTO.setStandort(sanitizeAllowedHtml(messstelleDTO.getStandort()));

        if (messstelleDTO.getCustomSuchwoerter() != null) {
            messstelleDTO.setCustomSuchwoerter(messstelleDTO.getCustomSuchwoerter().stream()
                    .filter(Objects::nonNull)
                    .map(this::sanitizeAllowedHtml)
                    .toList());
        }

        if (messstelleDTO.getMessquerschnitte() != null) {
            messstelleDTO.getMessquerschnitte().stream()
                    .filter(Objects::nonNull)
                    .forEach((this::sanitizeEditMessquerschnittDto));
        }
    }

    /**
     * Bereinigt die Attribute vom Typ {@link String} eines {@link EditMessquerschnittDTO}.
     * Unerwünschter HTML-Code wird entfernt.
     *
     * @param messquerschnittDTO Das {@link EditMessquerschnittDTO}, dessen Attribute bereinigt werden
     *            sollen
     */
    public void sanitizeEditMessquerschnittDto(final EditMessquerschnittDTO messquerschnittDTO) {
        if (messquerschnittDTO == null) return;
        messquerschnittDTO.setStrassenname(sanitizeAllowedHtml(messquerschnittDTO.getStrassenname()));
        messquerschnittDTO.setLageMessquerschnitt(sanitizeAllowedHtml(messquerschnittDTO.getLageMessquerschnitt()));
        messquerschnittDTO.setHersteller(sanitizeAllowedHtml(messquerschnittDTO.getHersteller()));
        messquerschnittDTO.setStandort(sanitizeAllowedHtml(messquerschnittDTO.getStandort()));
    }

    /**
     * Filtert aus dem HTML-String des übergebenen {@link TextAsset} alles Unerwünschte heraus und
     * aktualisiert den Text des Assets.
     *
     * @param asset TextAsset mit dem HTML-Text
     */
    public void sanitizeTextAsset(final TextAsset asset) {
        String inputHtml = asset.getText();
        String sanitizedHtml = sanitizeAllowedHtml(inputHtml);
        asset.setText(sanitizedHtml);
    }

    /**
     * Überprüft die im übergebenen {@link ImageAsset} enthaltene Image URI und ersetzt diese durch
     * eine neu kodierte sichere Kopie.
     *
     * @param asset ImageAsset mit dem src String des Bildes
     */
    public void sanitizeImageUri(ImageAsset asset) {
        String inputUri = asset.getImage();

        if (inputUri == null || inputUri.isBlank()) {
            throw new IllegalArgumentException("Image is empty");
        }

        if (!inputUri.startsWith("data:image/")) {
            throw new IllegalArgumentException("Only image data URIs are allowed");
        }

        int commaIndex = inputUri.indexOf(',');
        if (commaIndex < 0) {
            throw new IllegalArgumentException("Invalid data URI");
        }

        String header = inputUri.substring(0, commaIndex);
        String base64 = inputUri.substring(commaIndex + 1);

        // Nur png, jpeg und jpg akzeptieren (in URI-Header prüfen)
        String mime = header.substring("data:".length(), header.indexOf(';')).toLowerCase();
        if (!ALLOWED_MIME_TYPES.contains(mime)) {
            throw new IllegalArgumentException("Unsupported image type");
        }

        if (base64.isBlank()) {
            throw new IllegalArgumentException("Invalid data URI");
        }

        try {
            // Frühzeitige Abschätzung der Größe des dekodierten Images
            long estimatedDecodedSize = (long) base64.length() * 3 / 4;
            if (estimatedDecodedSize > MAX_IMAGE_SIZE_BYTES) {
                throw new IllegalArgumentException("Image exceeds maximum size");
            }

            byte[] imageBytes = Base64.getDecoder().decode(base64);

            // Nur png, jpeg und jpg akzeptieren (in Byte-String prüfen)
            String detectedMime = new Tika().detect(imageBytes);
            if (!ALLOWED_MIME_TYPES.contains(detectedMime)) {
                throw new IllegalArgumentException("Unsupported image type: " + detectedMime);
            }

            // Größe des dekodierten Images prüfen
            if (imageBytes.length > MAX_IMAGE_SIZE_BYTES) {
                throw new IllegalArgumentException("Image exceeds maximum size");
            }

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

            // Bilddimension und Auflösung prüfen
            int width = image.getWidth();
            int height = image.getHeight();
            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                throw new IllegalArgumentException("Image dimensions too large");
            }
            if ((long) width * height > MAX_PIXELS) {
                throw new IllegalArgumentException("Image has too many pixels");
            }

            // Neu kodieren
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);

            String safeUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
            asset.setImage(safeUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
