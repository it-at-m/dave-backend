package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.pdf.assets.BaseAsset;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.pdfgenerator.GeneratePdfService;
import de.muenchen.dave.services.pdfgenerator.ReportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/generate-pdf")
@PreAuthorize(
    "hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
            "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())"
)
public class GeneratePdfController {

    public static final String TYPE_BELASTUNGSPLAN = "belastungsplan";
    public static final String TYPE_GANGLINIE = "ganglinie";
    public static final String TYPE_DATENTABELLE = "datentabelle";
    public static final String TYPE_ZEITREIHE = "zeitreihe";
    private static final String HTTP_HEADER_CACHE_CONTROL = "must-revalidate, post-check=0, pre-check=0";
    private static final String HTTP_HEADER_ATTACHMENT = "attachment";
    private static final String HTTP_HEADER_FILENAME = "davePdf.pdf";
    private static final String REQUEST_PART_DEPARTMENT = "department";
    private static final String REQUEST_PART_OPTIONS = "options";
    private static final String REQUEST_PART_CHART_AS_BASE64_PNG = "chartAsBase64Png";
    private static final String REQUEST_PART_SCHEMATISCHE_UEBERSICHT_AS_BASE64_PNG = "schematischeUebersichtAsBase64Png";
    private static final String REQUEST_PARAMETER_CHARTTYPE = "charttype";
    private static final String REQUEST_PARAMETER_FACH_ID = "fach_id";

    private static final String FEHLER_PDF_ERSTELLUNG = "Es ist ein unerwarteter Fehler beim Erstellen der PDF-Datei aufgetreten.";

    private final GeneratePdfService generatePdfService;
    private final ReportService reportService;

    public GeneratePdfController(final GeneratePdfService generatePdfService,
            final ReportService reportService) {
        this.generatePdfService = generatePdfService;
        this.reportService = reportService;
    }

    /**
     * Nimmt Daten aus dem Frontend entgegen und gibt eine PDF als byte[] zurück
     *
     * @param zaehlungId Die im Frontend ausgewählte Zählung.
     * @param charttype Der angeforderte PDF Typ (z. B. Belastungsplan, Ganglinie, ...). Je nach Typ
     *            werden
     *            andere Mustache Templates verwendet.
     * @param department Organisationseinheit des Benutzers
     * @param options Die im Frontend ausgewählten Optionen.
     * @param chartAsBase64Png Ein Graph als PNG in Base64.
     * @param schematischeUebersichtAsBase64Png Die Schematische Übersicht als PNG in Base 64
     * @return ResponseEntity of type byte-Array
     */
    @PostMapping(value = "/zaehlung")
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam(value = REQUEST_PARAMETER_FACH_ID) @NotEmpty final String zaehlungId,
            @RequestParam(value = REQUEST_PARAMETER_CHARTTYPE) @NotEmpty final String charttype,
            @RequestPart(value = REQUEST_PART_DEPARTMENT) @NotEmpty final String department,
            @Valid @RequestPart(value = REQUEST_PART_OPTIONS) @NotNull final OptionsDTO options,
            @RequestPart(value = REQUEST_PART_CHART_AS_BASE64_PNG, required = false) final String chartAsBase64Png,
            @RequestPart(
                    value = REQUEST_PART_SCHEMATISCHE_UEBERSICHT_AS_BASE64_PNG, required = false
            ) final String schematischeUebersichtAsBase64Png) {
        try {
            final byte[] pdf;
            if (StringUtils.equalsIgnoreCase(charttype, TYPE_BELASTUNGSPLAN)) {
                pdf = generatePdfService.generateBelastungsplanPdf(zaehlungId, options, chartAsBase64Png, department);
            } else if (StringUtils.equalsIgnoreCase(charttype, TYPE_GANGLINIE)) {
                pdf = generatePdfService.generateGangliniePdf(zaehlungId, options, chartAsBase64Png, schematischeUebersichtAsBase64Png, department);
            } else if (StringUtils.equalsIgnoreCase(charttype, TYPE_DATENTABELLE)) {
                pdf = generatePdfService.generateDatentabellePdf(zaehlungId, options, schematischeUebersichtAsBase64Png, department);
            } else if (StringUtils.equalsIgnoreCase(charttype, TYPE_ZEITREIHE)) {
                pdf = generatePdfService.generateZeitreihePdf(zaehlungId, options, chartAsBase64Png, schematischeUebersichtAsBase64Png, department);
            } else {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, charttype + " nicht implementiert.");
            }

            final HttpHeaders headers = getHttpHeadersForPdfFile(pdf.length);

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (IOException ioe) {
            log.error(FEHLER_PDF_ERSTELLUNG, ioe);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FEHLER_PDF_ERSTELLUNG);
        } catch (DataNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Nimmt Daten aus dem Frontend entgegen und gibt eine PDF als byte[] zurück
     *
     * @param messstelleId Die im Frontend ausgewählte Messstelle.
     * @param type Der angeforderte PDF Typ (z. B. Belastungsplan, Ganglinie, ...). Je nach Typ werden
     *            andere Mustache Templates verwendet.
     * @param department Organisationseinheit des Benutzers
     * @param options Die im Frontend ausgewählten Optionen.
     * @param chartAsBase64Png Ein Graph als PNG in Base64.
     * @param schematischeUebersichtAsBase64Png Die Schematische Übersicht als PNG in Base 64
     * @return ResponseEntity of type byte-Array
     */
    @PostMapping(value = "/messstelle")
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam(value = REQUEST_PARAMETER_FACH_ID) @NotEmpty final String messstelleId,
            @RequestParam(value = REQUEST_PARAMETER_CHARTTYPE) @NotEmpty final String type,
            @RequestPart(value = REQUEST_PART_DEPARTMENT) @NotEmpty final String department,
            @Valid @RequestPart(value = REQUEST_PART_OPTIONS) @NotNull final MessstelleOptionsDTO options,
            @RequestPart(value = REQUEST_PART_CHART_AS_BASE64_PNG, required = false) final String chartAsBase64Png,
            @RequestPart(
                    value = REQUEST_PART_SCHEMATISCHE_UEBERSICHT_AS_BASE64_PNG, required = false
            ) final String schematischeUebersichtAsBase64Png) {
        try {
            final byte[] pdf;
            if (StringUtils.equalsIgnoreCase(type, TYPE_BELASTUNGSPLAN)) {
                pdf = generatePdfService.generateBelastungsplanPdf(messstelleId, options, chartAsBase64Png, department);
            } else if (StringUtils.equalsIgnoreCase(type, TYPE_GANGLINIE)) {
                pdf = generatePdfService.generateGangliniePdf(messstelleId, options, chartAsBase64Png, schematischeUebersichtAsBase64Png, department);
            } else if (StringUtils.equalsIgnoreCase(type, TYPE_DATENTABELLE)) {
                pdf = generatePdfService.generateDatentabellePdf(messstelleId, options, schematischeUebersichtAsBase64Png, department);
            } else {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, type + " nicht implementiert.");
            }

            final HttpHeaders headers = getHttpHeadersForPdfFile(pdf.length);

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (IOException ioe) {
            log.error(FEHLER_PDF_ERSTELLUNG, ioe);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FEHLER_PDF_ERSTELLUNG);
        } catch (DataNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

    }

    /**
     * Generiert einen PDF-Report aus den gelieferten Assets
     *
     * @param assetList Assets für den Report
     * @param department Organisationseinheit des Benutzers
     * @return PDF-Datei
     */
    @PostMapping(value = "/report")
    public ResponseEntity<byte[]> generatePdfReport(@RequestPart(value = "assets") @NotNull final List<BaseAsset> assetList,
            @RequestPart(value = REQUEST_PART_DEPARTMENT) @NotEmpty final String department) {
        try {
            final byte[] pdf;
            pdf = reportService.generateReportPdf(assetList, department);
            final HttpHeaders headers = getHttpHeadersForPdfFile(pdf.length);
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (IOException ioe) {
            log.error(FEHLER_PDF_ERSTELLUNG, ioe);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, FEHLER_PDF_ERSTELLUNG);
        }
    }

    HttpHeaders getHttpHeadersForPdfFile(int fileLength) {
        final HttpHeaders headers = new HttpHeaders();

        headers.setContentLength(fileLength);
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setCacheControl(HTTP_HEADER_CACHE_CONTROL);
        headers.setContentDispositionFormData(HTTP_HEADER_ATTACHMENT, HTTP_HEADER_FILENAME);
        return headers;
    }
}
