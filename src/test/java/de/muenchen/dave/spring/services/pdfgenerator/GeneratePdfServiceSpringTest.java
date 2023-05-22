package de.muenchen.dave.spring.services.pdfgenerator;

import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester;
import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester.PdfCompareResult;
import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.services.pdfgenerator.GeneratePdfService;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE",
        "refarch.gracefulshutdown.pre-wait-seconds=0" })
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
public class GeneratePdfServiceSpringTest {

    private static final String TEST_OUTPUT_PATH = "target/unit-tests/";
    private static final String EXPECTED_RES_PATH = "/pdf/expected-pdfs/";

    @Autowired
    GeneratePdfService generatePdfService;

    /**
     * Übernommen von <a href=
     * "https://github.com/danfickle/openhtmltopdf/wiki/Testing-Your-PDF-Document-Output">...</a>
     * <p>
     * Überprüft ob die übergebene PDF mit einer bereits hinterlegten übereinstimmt.
     * Bei einem Negativergebnis werden Screenshots von beider PDFs und eines "diffs" nach
     * target/unit-tests/ gelegt.
     *
     * @param resource
     * @param actualPdfBytes
     * @return
     * @throws IOException
     */
    private boolean isSamePdf(String resource, byte[] actualPdfBytes) throws IOException {
        Files.createDirectories(Paths.get(TEST_OUTPUT_PATH));

        // Load expected PDF document from resources, change class below.
        byte[] expectedPdfBytes;
        try (InputStream expectedIs = this.getClass().getResourceAsStream(EXPECTED_RES_PATH + resource + ".pdf")) {

            expectedPdfBytes = IOUtils.toByteArray(expectedIs);
        }

        // Get a list of results.
        List<PdfCompareResult> problems = PdfVisualTester.comparePdfDocuments(expectedPdfBytes, actualPdfBytes, resource, false);

        if (!problems.isEmpty()) {
            System.err.println("Found problems with test case (" + resource + "):");
            System.err.println(problems.stream().map(p -> p.logMessage).collect(Collectors.joining("\n    ", "[\n    ", "\n]")));

            System.err.println("For test case (" + resource + ") writing failure artefacts to '" + TEST_OUTPUT_PATH + "'");
            File outPdf = new File(TEST_OUTPUT_PATH, resource + "---actual.pdf");
            Files.write(outPdf.toPath(), actualPdfBytes);
        }

        for (PdfCompareResult result : problems) {
            if (result.testImages != null) {
                File output = new File(TEST_OUTPUT_PATH, resource + "---" + result.pageNumber + "---diff.png");
                ImageIO.write(result.testImages.createDiff(), "png", output);

                output = new File(TEST_OUTPUT_PATH, resource + "---" + result.pageNumber + "---actual.png");
                ImageIO.write(result.testImages.getActual(), "png", output);

                output = new File(TEST_OUTPUT_PATH, resource + "---" + result.pageNumber + "---expected.png");
                ImageIO.write(result.testImages.getExpected(), "png", output);
            }
        }

        return problems.isEmpty();
    }

    @Test
    public void createPdf() throws IOException {
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <style>\n" +
                "  @page {\n" +
                "    size: portrait a4;\n" +
                "    margin-top: 1cm;\n" +
                "    margin-right: 1cm;\n" +
                "    margin-bottom: 2.5cm;\n" +
                "    margin-left: 2.5cm;\n" +
                "  }\n" +
                "  body {\n" +
                "    font-size: small;\n" +
                "    font-family: \"Roboto\", sans-serif;\n" +
                "  }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Test-PDF</h1>\n" +
                "<span>Nur ein Test.</span>\n" +
                "</body>\n" +
                "</html>";

        byte[] generatedPdf = generatePdfService.createPdf(html);

        // Überprüft, ob das Aussehen der erstellen PDF mit der hinterlegten übereinstimmt
        assertThat(isSamePdf("createPdfTest", generatedPdf), is(true));
    }
}
