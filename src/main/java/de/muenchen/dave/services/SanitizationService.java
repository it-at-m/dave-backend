package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import java.util.Objects;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

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
                    .forEach((knotenarmDTO -> knotenarmDTO.setStrassenname(sanitizeAllowedHtml(knotenarmDTO.getStrassenname()))));
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
        messquerschnittDTO.setStrassenname(messquerschnittDTO.getStrassenname());
        messquerschnittDTO.setLageMessquerschnitt(messquerschnittDTO.getLageMessquerschnitt());
        messquerschnittDTO.setHersteller(messquerschnittDTO.getHersteller());
        messquerschnittDTO.setStandort(messquerschnittDTO.getStandort());
    }
}
