package de.muenchen.dave.controller;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.pdf.assets.BaseAsset;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.pdfgenerator.GeneratePdfService;
import de.muenchen.dave.services.pdfgenerator.ReportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole(T(de.muenchen.dave.security.AuthoritiesEnum).ANWENDER.name(), " +
        "T(de.muenchen.dave.security.AuthoritiesEnum).POWERUSER.name())")
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
    private static final String REQUEST_PARAMETER_TYPE = "type";
    private static final String REQUEST_PARAMETER_ZAEHLUNG_ID = "zaehlung_id";

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
     * @param type Der angeforderte PDF Typ (z. B. Belastungsplan, Ganglinie, ...). Je nach Typ werden
     *            andere Mustache Templates verwendet.
     * @param department Organisationseinheit des Benutzers
     * @param options Die im Frontend ausgewählten Optionen.
     * @param chartAsBase64Png Ein Graph als PNG in Base64.
     * @param schematischeUebersichtAsBase64Png Die Schematische Übersicht als PNG in Base 64
     * @return ResponseEntity of type byte-Array
     */
    @PostMapping(value = "/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam(value = REQUEST_PARAMETER_ZAEHLUNG_ID) @NotEmpty final String zaehlungId,
            @RequestParam(value = REQUEST_PARAMETER_TYPE) @NotEmpty final String type,
            @RequestPart(value = REQUEST_PART_DEPARTMENT) @NotEmpty final String department,
            @Valid @RequestPart(value = REQUEST_PART_OPTIONS) @NotNull final OptionsDTO options,
            @RequestPart(value = REQUEST_PART_CHART_AS_BASE64_PNG, required = false) @NotNull final String chartAsBase64Png,
            @RequestPart(value = REQUEST_PART_SCHEMATISCHE_UEBERSICHT_AS_BASE64_PNG, required = false) @NotNull final String schematischeUebersichtAsBase64Png) {
        try {
            final byte[] pdf;
            if (StringUtils.equalsIgnoreCase(type, TYPE_BELASTUNGSPLAN)) {
                pdf = generatePdfService.generateBelastungsplanPdf(zaehlungId, options, chartAsBase64Png, department);
            } else if (StringUtils.equalsIgnoreCase(type, TYPE_GANGLINIE)) {
                pdf = generatePdfService.generateGangliniePdf(zaehlungId, options, chartAsBase64Png, schematischeUebersichtAsBase64Png, department);
            } else if (StringUtils.equalsIgnoreCase(type, TYPE_DATENTABELLE)) {
                pdf = generatePdfService.generateDatentabellePdf(zaehlungId, options, schematischeUebersichtAsBase64Png, department);
            } else if (StringUtils.equalsIgnoreCase(type, TYPE_ZEITREIHE)) {
                pdf = generatePdfService.generateZeitreihePdf(zaehlungId, options, chartAsBase64Png, schematischeUebersichtAsBase64Png, department);
            } else {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, type + " nicht implementiert.");
            }

            final HttpHeaders headers = getHttpHeadersForPdfFile(pdf.length);

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Erstellen der PDF-Datei aufgetreten.");
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
    @PostMapping(value = "/generate-pdf/report")
    public ResponseEntity<byte[]> generatePdfReport(@RequestPart(value = "assets") @NotNull final List<BaseAsset> assetList,
            @RequestPart(value = REQUEST_PART_DEPARTMENT) @NotEmpty final String department) {
        try {
            final byte[] pdf;
            pdf = reportService.generateReportPdf(assetList, department);
            final HttpHeaders headers = getHttpHeadersForPdfFile(pdf.length);
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Es ist ein unerwarteter Fehler beim Erstellen der PDF-Datei aufgetreten.");
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
