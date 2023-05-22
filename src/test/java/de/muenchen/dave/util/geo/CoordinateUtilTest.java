package de.muenchen.dave.util.geo;

import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CoordinateUtilTest {

    @Test
    public void arePositionsWithinGivenDistance() {
        CoordinateUtil.PositionWGS84 position1 = new CoordinateUtil.PositionWGS84(48.133453766001075, 11.54928191096542);
        CoordinateUtil.PositionWGS84 position2 = new CoordinateUtil.PositionWGS84(48.13251574474983, 11.549094156334377);
        boolean result = CoordinateUtil.arePositionsWithinGivenDistance(
                106,
                position1,
                position2);
        assertThat(result, is(true));

        result = CoordinateUtil.arePositionsWithinGivenDistance(
                105,
                position1,
                position2);
        assertThat(result, is(false));
    }

    @Test
    public void calculateDistanceInMeter() {
        CoordinateUtil.PositionWGS84 position1 = new CoordinateUtil.PositionWGS84(48.1334537, 11.5492819);
        CoordinateUtil.PositionWGS84 position2 = new CoordinateUtil.PositionWGS84(48.1325157, 11.5490941);
        double result = CoordinateUtil.calculateDistanceInMeter(position1, position2);
        assertThat(result, is(105.23151390323481));
    }

    /**
     * Testdaten für Koordinatenumrechnung:
     * <a href="https://www.koordinaten-umrechner.de/decimal/66.296139,-104.062500?karte=OpenStreetMap&zoom=4">...</a>
     */
    @Test
    public void transformFromWGS84ToUTM() {
        GeoPoint positionWgs84 = new GeoPoint(48.1335025, 11.6905552);
        CoordinateUtil.PositionUTM expectedUtm = new CoordinateUtil.PositionUTM();
        expectedUtm.setZone(32);
        expectedUtm.setLetter('U');
        expectedUtm.setEasting(700177.0);
        expectedUtm.setNorthing(5334640.0);
        CoordinateUtil.PositionUTM resultUtm = CoordinateUtil.transformFromWGS84ToUTM(positionWgs84);
        assertThat(resultUtm, is(expectedUtm));

        positionWgs84 = new GeoPoint(49.297146, 12.941895);
        expectedUtm.setZone(33);
        expectedUtm.setLetter('U');
        expectedUtm.setEasting(350367.31);
        expectedUtm.setNorthing(5462526.7);
        resultUtm = CoordinateUtil.transformFromWGS84ToUTM(positionWgs84);
        assertThat(resultUtm, is(expectedUtm));

        positionWgs84 = new GeoPoint(66.296139, -104.062500);
        expectedUtm.setZone(13);
        expectedUtm.setLetter('W');
        expectedUtm.setEasting(542054.66);
        expectedUtm.setNorthing(7353234.2);
        resultUtm = CoordinateUtil.transformFromWGS84ToUTM(positionWgs84);
        assertThat(resultUtm, is(expectedUtm));

        positionWgs84 = new GeoPoint(0, 0);
        expectedUtm.setZone(31);
        expectedUtm.setLetter('N');
        expectedUtm.setEasting(166021.44);
        expectedUtm.setNorthing(0.0);
        resultUtm = CoordinateUtil.transformFromWGS84ToUTM(positionWgs84);
        assertThat(resultUtm, is(expectedUtm));

        resultUtm = CoordinateUtil.transformFromWGS84ToUTM(null);
        assertThat(resultUtm, is(new CoordinateUtil.PositionUTM()));
    }

}
