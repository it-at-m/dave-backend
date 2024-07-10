package de.muenchen.dave.spring.services.pdfgenerator;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.enums.AssetType;
import de.muenchen.dave.domain.pdf.assets.BaseAsset;
import de.muenchen.dave.domain.pdf.assets.HeadingAsset;
import de.muenchen.dave.domain.pdf.assets.ImageAsset;
import de.muenchen.dave.domain.pdf.assets.PagebreakAsset;
import de.muenchen.dave.domain.pdf.assets.TextAsset;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.services.pdfgenerator.ReportService;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = { DaveBackendApplication.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE"}
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
public class ReportServiceSpringTest {

    @MockBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @MockBean
    private MessstelleIndex messstelleIndex;

    @MockBean
    private CustomSuggestIndex customSuggestIndex;

    @Autowired
    ReportService reportService;

    @Test
    public void generateReportHtml() {
        final ImageAsset ia1 = new ImageAsset();
        ia1.setType(AssetType.IMAGE);
        ia1.setCaption("Caption1");
        ia1.setImage("image");

        final TextAsset ta1 = new TextAsset();
        ta1.setType(AssetType.TEXT);
        ta1.setText("Testtest");
        ta1.setSize("medium");

        final TextAsset ta2 = new TextAsset();
        ta2.setType(AssetType.TEXT);
        ta2.setSize("medium");
        ta2.setText(
                "Lorem ipsum dolor sit amet, per ut probo velit, in pri invenire repudiare complectitur, meliore detraxit recusabo ut vim. Ut modus nonumy eum. Quo ipsum legere et. Usu latine reformidans ullamcorper et, suscipit eleifend facilisis id usu. Eum probo soluta an.\n"
                        +
                        "\n" +
                        "Ei soluta moderatius intellegam duo, nam cu falli rationibus. Eos no choro ubique. Ex postulant suscipiantur qui. Oratio vivendo recteque te duo. Qualisque maiestatis consectetuer mea in, persius cotidieque intellegebat eu eam.\n"
                        +
                        "\n" +
                        "Mel stet errem dolorem ei, ei ius ipsum convenire intellegam. Vix an inimicus voluptatum. Dolorem omnesque id mei. His ei omnium necessitatibus.\n"
                        +
                        "\n" +
                        "Id mazim debitis eam. Aeterno reprimique adversarium eos ea, putant timeam eloquentiam et his. Et labore consetetur dissentiunt nam, et has erroribus persecuti democritum, has dignissim gubergren ad. Facilis deserunt explicari ad eam, ubique vivendum nam ea, ex agam possit iuvaret vel. Eruditi accumsan rationibus sed ex, mutat putant democritum eu eos. Te iuvaret facilis ocurreret mei, mea in iudico invidunt.\n"
                        +
                        "\n" +
                        "Ex mel mazim debet tritani. Ex vim nominavi inciderint necessitatibus, te nec ferri habeo mundi. Duo persius indoctum tractatos an, cum velit inciderint at. Sit ne magna virtute conclusionemque, ignota putant aliquip ne cum, in iudico utinam sea. Ei cum nullam detraxit, at has nulla sadipscing, no unum civibus consequat nec.");

        final ImageAsset ia2 = new ImageAsset();
        ia2.setType(AssetType.IMAGE);
        ia2.setCaption("Längere Caption mit Mitlaut");
        ia2.setImage("image_als_base64");

        final HeadingAsset ha = new HeadingAsset();
        ha.setType(AssetType.HEADING2);
        ha.setText("Headline");

        final PagebreakAsset pa = new PagebreakAsset();
        pa.setType(AssetType.PAGEBREAK);

        final List<BaseAsset> list = new ArrayList<>();
        list.add(ia1);
        list.add(ta1);
        list.add(pa);
        list.add(ha);
        list.add(ta2);
        list.add(ia2);

        final String html = this.reportService.generateReportBody(list);

        System.out.println(html);

        String result = "<figure style=\"margin-bottom: 20px; page-break-inside: avoid;\">\n  <div class=\"report\">\n    <img style=\"width: %; display: block;\" src=\"image\"/>\n    <figcaption class=\"report\">Caption1</figcaption>\n  </div>\n</figure><p style=\"font-size: medium\" class=\"report\">Testtest</p><page-break-before></page-break-before><h2 class=\"report\">Headline</h2><p style=\"font-size: medium\" class=\"report\">Lorem ipsum dolor sit amet, per ut probo velit, in pri invenire repudiare complectitur, meliore detraxit recusabo ut vim. Ut modus nonumy eum. Quo ipsum legere et. Usu latine reformidans ullamcorper et, suscipit eleifend facilisis id usu. Eum probo soluta an.\n\nEi soluta moderatius intellegam duo, nam cu falli rationibus. Eos no choro ubique. Ex postulant suscipiantur qui. Oratio vivendo recteque te duo. Qualisque maiestatis consectetuer mea in, persius cotidieque intellegebat eu eam.\n\nMel stet errem dolorem ei, ei ius ipsum convenire intellegam. Vix an inimicus voluptatum. Dolorem omnesque id mei. His ei omnium necessitatibus.\n\nId mazim debitis eam. Aeterno reprimique adversarium eos ea, putant timeam eloquentiam et his. Et labore consetetur dissentiunt nam, et has erroribus persecuti democritum, has dignissim gubergren ad. Facilis deserunt explicari ad eam, ubique vivendum nam ea, ex agam possit iuvaret vel. Eruditi accumsan rationibus sed ex, mutat putant democritum eu eos. Te iuvaret facilis ocurreret mei, mea in iudico invidunt.\n\nEx mel mazim debet tritani. Ex vim nominavi inciderint necessitatibus, te nec ferri habeo mundi. Duo persius indoctum tractatos an, cum velit inciderint at. Sit ne magna virtute conclusionemque, ignota putant aliquip ne cum, in iudico utinam sea. Ei cum nullam detraxit, at has nulla sadipscing, no unum civibus consequat nec.</p><figure style=\"margin-bottom: 20px; page-break-inside: avoid;\">\n  <div class=\"report\">\n    <img style=\"width: %; display: block;\" src=\"image_als_base64\"/>\n    <figcaption class=\"report\">Längere Caption mit Mitlaut</figcaption>\n  </div>\n</figure>";
        assertThat(html, is(result));
    }
}
