package de.muenchen.dave.util.geo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.GeodeticCalculator;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class CoordinateUtil {

    private static final CoordinateReferenceSystem CRS_WGS84 = CommonCRS.WGS84.geographic();

    public static boolean arePositionsWithinGivenDistance(final double distance,
            final GeoPoint position1,
            final GeoPoint position2) {
        final PositionWGS84 wgs84Position1 = new PositionWGS84(position1.getLat(), position1.getLon());
        final PositionWGS84 wgs84Position2 = new PositionWGS84(position2.getLat(), position2.getLon());
        return arePositionsWithinGivenDistance(distance, wgs84Position1, wgs84Position2);
    }

    public static boolean arePositionsWithinGivenDistance(final double distance,
            final PositionWGS84 position1,
            final PositionWGS84 position2) {
        log.debug("Check if positions are within a distance of {} meters.", distance);
        final double geodesicDistance = calculateDistanceInMeter(position1, position2);
        return geodesicDistance <= distance;
    }

    public static double calculateDistanceInMeter(final PositionWGS84 position1,
            final PositionWGS84 position2) {
        log.debug("Calculate distance in meter.");
        final GeodeticCalculator calculator = GeodeticCalculator.create(CRS_WGS84);
        calculator.setStartGeographicPoint(position1.getLatitude(), position1.getLongitude());
        calculator.setEndGeographicPoint(position2.getLatitude(), position2.getLongitude());
        return calculator.getGeodesicDistance();
    }

    /**
     * In dieser Methode werden die Koordinaten im WGS84-Format in das Format UTM transformiert.
     *
     * @param positionWgs84 als {@link GeoPoint} in WGS84-Koordinatendarstellung
     * @return Position als Koordinatenangabe in MGRS / UTMREF (z.B. 13 W 542054.66 7353234.2).
     */
    public static PositionUTM transformFromWGS84ToUTM(final GeoPoint positionWgs84) {
        log.debug("Transform from WGS84 to UTM coordinates.");
        final var positionUtm = new PositionUTM();
        if (ObjectUtils.isNotEmpty(positionWgs84)) {
            final var wgsCoordinates = new WGS84(
                    positionWgs84.getLat(),
                    positionWgs84.getLon());
            var utm = new UTM(wgsCoordinates);
            positionUtm.setZone(utm.getZone());
            positionUtm.setLetter(utm.getLetter());
            positionUtm.setEasting(utm.getEasting());
            positionUtm.setNorthing(utm.getNorthing());
        }
        return positionUtm;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    @EqualsAndHashCode
    public static class PositionWGS84 {

        private final double latitude;

        private final double longitude;

    }

    @Data
    public static class PositionUTM {

        private Integer zone;

        private Character letter;

        private Double easting;

        private Double northing;

    }

}
