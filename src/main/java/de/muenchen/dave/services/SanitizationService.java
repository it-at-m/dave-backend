package de.muenchen.dave.services;

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
}
