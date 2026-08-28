package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.*;
import de.muenchen.dave.domain.pdf.assets.ImageAsset;
import de.muenchen.dave.domain.pdf.assets.TextAsset;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

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
     * Validiert, dass der übergebene {@link String} ein gültiger Wert des Enums {@link Zaehlart} ist.
     * Ist der Wert ungültig, wird eine {@link IllegalArgumentException} geworfen.
     *
     * @param zaehlart Zu validierende Zählart als {@link String}
     */
    public void validateZaehlart(final String zaehlart) {
        if (zaehlart == null) return;
        List<String> allowedZaehlarten = Arrays.stream(Zaehlart.values()).map(Zaehlart::toString).toList();
        if (!allowedZaehlarten.contains(zaehlart)) {
            throw new IllegalArgumentException("Ungültige Zaehlart: " + zaehlart);
        }
    }

    /**
     * Validiert, dass der übergebene {@link String} ein gültiger Wert des Enums {@link Wetter} ist.
     * Ist der Wert ungültig, wird eine {@link IllegalArgumentException} geworfen.
     *
     * @param wetter Zu validierendes Wetter als {@link String}
     */
    public void validateWetter(final String wetter) {
        if (wetter == null) return;
        List<String> allowedWetter = Arrays.stream(Wetter.values()).map(Wetter::toString).toList();
        if (!allowedWetter.contains(wetter)) {
            throw new IllegalArgumentException("Ungültiges Wetter: " + wetter);
        }
    }

    /**
     * Validiert, dass der übergebene {@link String} ein gültiger Wert des Enums {@link Status} ist.
     * Ist der Wert ungültig, wird eine {@link IllegalArgumentException} geworfen.
     *
     * @param status Zu validierender Status als {@link String}
     */
    public void validateStatus(final String status) {
        if (status == null) return;
        List<String> allowedStatus = Arrays.stream(Status.values()).map(Status::toString).toList();
        if (!allowedStatus.contains(status)) {
            throw new IllegalArgumentException("Ungültiger Status: " + status);
        }
    }

    /**
     * Validiert, dass der übergebene {@link String} ein gültiger Wert des Enums {@link Quelle} ist.
     * Ist der Wert ungültig, wird eine {@link IllegalArgumentException} geworfen.
     *
     * @param quelle Zu validierende Quelle als {@link String}
     */
    public void validateQuelle(final String quelle) {
        if (quelle == null) return;
        List<String> allowedQuellen = Arrays.stream(Quelle.values()).map(Quelle::toString).toList();
        if (!allowedQuellen.contains(quelle)) {
            throw new IllegalArgumentException("Ungültige Quelle: " + quelle);
        }
    }

    /**
     * Validiert, dass der übergebene {@link String} ein gültiger Wert des Enums {@link Zaehldauer} ist.
     * Ist der Wert ungültig, wird eine {@link IllegalArgumentException} geworfen.
     *
     * @param zaehldauer Zu validierende Zähldauer als {@link String}
     */
    public void validateZaehldauer(final String zaehldauer) {
        if (zaehldauer == null) return;
        List<String> allowedZaehldauern = Arrays.stream(Zaehldauer.values()).map(Zaehldauer::toString).toList();
        if (!allowedZaehldauern.contains(zaehldauer)) {
            throw new IllegalArgumentException("Ungültige Zaehldauer: " + zaehldauer);
        }
    }

    /**
     * Validiert, dass der übergebene {@link String} ein gültiger Wert des Enums {@link Stadtbezirk}
     * ist. Ist der Wert ungültig, wird eine {@link IllegalArgumentException} geworfen.
     *
     * @param stadtbezirk Zu validierender Stadtbezirk als {@link String}
     */
    public void validateStadtbezirk(final String stadtbezirk) {
        if (stadtbezirk == null) return;
        List<String> allowedStadtbezirke = Arrays.stream(Stadtbezirk.values()).map(Stadtbezirk::toString).toList();
        if (!allowedStadtbezirke.contains(stadtbezirk)) {
            throw new IllegalArgumentException("Ungültiger Stadtbezirk: " + stadtbezirk);
        }
    }

    /**
     * Bereinigt die Attribute vom Typ {@link String} eines {@link BearbeiteZaehlungDTO}. Unerwünschter
     * HTML-Code wird entfernt.
     *
     * @param zaehlungDTO Das {@link BearbeiteZaehlungDTO}, dessen Attribute bereinigt werden sollen
     */
    public void sanitizeBearbeiteZaehlungDto(final BearbeiteZaehlungDTO zaehlungDTO) {
        if (zaehlungDTO == null) return;
        validateZaehlart(zaehlungDTO.getZaehlart());
        zaehlungDTO.setProjektNummer(sanitizeAllowedHtml(zaehlungDTO.getProjektNummer()));
        zaehlungDTO.setProjektName(sanitizeAllowedHtml(zaehlungDTO.getProjektName()));
        zaehlungDTO.setKreuzungsname(sanitizeAllowedHtml(zaehlungDTO.getKreuzungsname()));
        zaehlungDTO.setZaehlsituation(sanitizeAllowedHtml(zaehlungDTO.getZaehlsituation()));
        zaehlungDTO.setZaehlsituationErweitert(sanitizeAllowedHtml(zaehlungDTO.getZaehlsituationErweitert()));
        validateWetter(zaehlungDTO.getWetter());
        validateStatus(zaehlungDTO.getStatus());
        validateQuelle(zaehlungDTO.getQuelle());
        validateZaehldauer(zaehlungDTO.getZaehldauer());
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
        validateStadtbezirk(bearbeiteZaehlstelleDTO.getStadtbezirk());
        bearbeiteZaehlstelleDTO.setKommentar(sanitizeAllowedHtml(bearbeiteZaehlstelleDTO.getKommentar()));

        if (bearbeiteZaehlstelleDTO.getCustomSuchwoerter() != null) {
            bearbeiteZaehlstelleDTO.setCustomSuchwoerter(bearbeiteZaehlstelleDTO.getCustomSuchwoerter().stream()
                    .filter(Objects::nonNull)
                    .map(this::sanitizeAllowedHtml)
                    .toList());
        }
    }

    /**
     * Bereinigt die Attribute vom Typ {@link String} einer {@link Messstelle}. Unerwünschter
     * HTML-Code wird entfernt.
     *
     * @param messstelle Die {@link Messstelle}, deren Attribute bereinigt werden sollen
     */
    public void sanitizeMessstelle(final Messstelle messstelle) {
        if (messstelle == null) return;
        messstelle.setName(sanitizeAllowedHtml(messstelle.getName()));
        messstelle.setBemerkung(sanitizeAllowedHtml(messstelle.getBemerkung()));
        messstelle.setHersteller(sanitizeAllowedHtml(messstelle.getHersteller()));
        messstelle.setKommentar(sanitizeAllowedHtml(messstelle.getKommentar()));
        messstelle.setStandort(sanitizeAllowedHtml(messstelle.getStandort()));

        if (messstelle.getSuchwoerter() != null) {
            messstelle.setSuchwoerter(messstelle.getSuchwoerter().stream()
                    .filter(Objects::nonNull)
                    .map(this::sanitizeAllowedHtml)
                    .toList());
        }

        if (messstelle.getCustomSuchwoerter() != null) {
            messstelle.setCustomSuchwoerter(messstelle.getCustomSuchwoerter().stream()
                    .filter(Objects::nonNull)
                    .map(this::sanitizeAllowedHtml)
                    .toList());
        }

        if (messstelle.getMessquerschnitte() != null) {
            messstelle.getMessquerschnitte().stream()
                    .filter(Objects::nonNull)
                    .forEach((this::sanitizeMessquerschnitt));
        }
    }

    /**
     * Bereinigt die Attribute vom Typ {@link String} eines {@link Messquerschnitt}s.
     * Unerwünschter HTML-Code wird entfernt.
     *
     * @param messquerschnitt Der {@link Messquerschnitt}, dessen Attribute bereinigt werden
     *            sollen
     */
    public void sanitizeMessquerschnitt(Messquerschnitt messquerschnitt) {
        if (messquerschnitt == null) return;
        messquerschnitt.setStrassenname(sanitizeAllowedHtml(messquerschnitt.getStrassenname()));
        messquerschnitt.setLageMessquerschnitt(sanitizeAllowedHtml(messquerschnitt.getLageMessquerschnitt()));
        messquerschnitt.setFahrtrichtung(sanitizeAllowedHtml(messquerschnitt.getFahrtrichtung()));
        messquerschnitt.setStandort(sanitizeAllowedHtml(messquerschnitt.getStandort()));
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
