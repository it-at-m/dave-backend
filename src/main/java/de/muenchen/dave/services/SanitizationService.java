package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SanitizationService {

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
                    .forEach((knotenarmDTO ->
                            knotenarmDTO.setStrassenname(sanitizeAllowedHtml(knotenarmDTO.getStrassenname()))));
        }
    }
}
