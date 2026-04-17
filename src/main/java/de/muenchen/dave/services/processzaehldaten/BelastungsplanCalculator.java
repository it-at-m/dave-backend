package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.dtos.laden.BelastungsplanDataDTO;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.util.CalculationUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BelastungsplanCalculator {

    public static final String SUM = "sum";
    public static final String SUM_IN = "sumIn";
    public static final String SUM_OUT = "sumOut";

    /**
     * Subtrahiert eine BigDecimal[][]-Matrize von einer anderen.
     *
     * @param basis Minuend-Matrize
     * @param vergleich Subtrahend-Matrize
     * @return Differenzwert-Matrize
     */
    public static BigDecimal[][] subtractMatrice(final BigDecimal[][] basis, final BigDecimal[][] vergleich) {
        final int rows = basis.length;
        final int cols = basis[0].length;

        final BigDecimal[][] diff = new BigDecimal[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                diff[i][j] = basis[i][j].subtract(vergleich[i][j]);
            }
        }
        return diff;
    }

    /**
     * Berechnet aus eine zweidimensionalen Array die einzelnen Summen pro Knotenarm für Kreuzungen.
     *
     * @param values Werte der Kreuzung pro Knotenarm
     * @return Map mit den einzelnen Summen pro Knotenarm
     */
    public static Map<String, BigDecimal[]> calcSumsKreuzung(final BigDecimal[][] values) {
        final Map<Integer, List<BigDecimal>> listOut = new HashMap<>();
        final Map<Integer, List<BigDecimal>> listIn = new HashMap<>();
        final Map<Integer, List<BigDecimal>> listBoth = new HashMap<>();
        for (int outerIndex = 0; outerIndex < values.length; outerIndex++) { // von
            final ArrayList<BigDecimal> out = new ArrayList<>();
            final ArrayList<BigDecimal> in = new ArrayList<>();
            final ArrayList<BigDecimal> both = new ArrayList<>();
            for (int innerIndex = 0; innerIndex < values[outerIndex].length; innerIndex++) { // nach
                in.add(values[outerIndex][innerIndex]);
                out.add(values[innerIndex][outerIndex]);
                both.add(values[outerIndex][innerIndex]);
                both.add(values[innerIndex][outerIndex]);
            }
            listOut.put(outerIndex, out);
            listIn.put(outerIndex, in);
            listBoth.put(outerIndex, both);
        }

        final Map<String, BigDecimal[]> sums = new HashMap<>();
        sums.put(SUM_IN, sumValuesOfList(listIn));
        sums.put(SUM_OUT, sumValuesOfList(listOut));
        sums.put(SUM, sumValuesOfList(listBoth));
        return sums;
    }

    /**
     * Berechnet aus eine zweidimensionalen Array die einzelnen Summen (Einfahrend, Ausfahren, Beide
     * zusammen) pro Knotenarm für Kreisverkehre.
     *
     * @param values Werte des Kreisverkehrs pro Knotenarm
     * @return Map mit den einzelnen Summen pro Knotenarm
     */
    public static Map<String, BigDecimal[]> calcSumsKreisverkehr(final BigDecimal[][] values) {

        final Map<Integer, List<BigDecimal>> listIn = new HashMap<>();
        final Map<Integer, List<BigDecimal>> listBoth = new HashMap<>();
        for (int outerIndex = 0; outerIndex < values.length; outerIndex++) { // von
            final ArrayList<BigDecimal> in = new ArrayList<>();
            final ArrayList<BigDecimal> both = new ArrayList<>();
            both.add(values[outerIndex][0]); // in den Kreis
            both.add(values[outerIndex][2]); // aus dem Kreis

            in.add(values[outerIndex][0]); // in den Kreis
            in.add(values[outerIndex][1]); // vorbei am Arm
            listIn.put(outerIndex, in);
            listBoth.put(outerIndex, both);
        }

        final Map<String, BigDecimal[]> sums = new HashMap<>();
        sums.put(SUM_IN, sumValuesOfList(listIn));
        //        sums.put(SUM_OUT, this.sumValuesOfList(listOut));
        sums.put(SUM, sumValuesOfList(listBoth));
        return sums;
    }

    /**
     * Berechnet den Prozentwert pro ArrayElement
     *
     * @param kfz kfz
     * @param svOrGv sv oder gv
     * @return Array
     */
    public static BigDecimal[] calculateAnteilProzent(final BigDecimal[] kfz, final BigDecimal[] svOrGv) {
        if (kfz != null && svOrGv != null) {
            final BigDecimal[] sumInSvpOrGvp = new BigDecimal[svOrGv.length];
            for (int index = 0; index < svOrGv.length; index++) {
                sumInSvpOrGvp[index] = CalculationUtil.calculateAnteilProzent(svOrGv[index], kfz[index]);
            }
            return sumInSvpOrGvp;
        } else {
            return new BigDecimal[0];
        }
    }

    /**
     * Reichert das übergebene BelastungsplanDataDTO-Objekt um die Summen der einzelnen Knotenarme an.
     *
     * @param data BelastungsplanDataDTO-Objekt, welches um die Summen angereichert werden soll
     * @param sumsKfz Datengrundlage von KFZ zur Berechnung der %-Anteile
     * @param sumsSv Datengrundlage von SV zur Berechnung der SV%-Anteile
     * @param sumsGv Datengrundlage von GV zur Berechnung der GV%-Anteile
     * @return gibt das um die Summen erweiterte BelastungsplanDataDTO-Objekt zurück
     */
    public static BelastungsplanDataDTO calculateSumsForBelastungsplanDataDto(final BelastungsplanDataDTO data, final Map<String, BigDecimal[]> sumsKfz,
            final Map<String, BigDecimal[]> sumsSv, final Map<String, BigDecimal[]> sumsGv, final boolean isKreisverkehr) {

        if (data.getLabel().equalsIgnoreCase(Fahrzeug.SV_P.getName()) && sumsKfz != null && sumsSv != null) {
            final Map<String, BigDecimal[]> sumSvp = calculateSumsSvpOrGvpKreuzung(sumsKfz, sumsSv);
            data.setSum(sumSvp.get(BelastungsplanCalculator.SUM));
            data.setSumIn(sumSvp.get(BelastungsplanCalculator.SUM_IN));
            data.setSumOut(sumSvp.get(BelastungsplanCalculator.SUM_OUT));
        } else if (data.getLabel().equalsIgnoreCase(Fahrzeug.GV_P.getName()) && sumsKfz != null && sumsGv != null) {
            final Map<String, BigDecimal[]> sumGvp = calculateSumsSvpOrGvpKreuzung(sumsKfz, sumsGv);
            data.setSum(sumGvp.get(BelastungsplanCalculator.SUM));
            data.setSumIn(sumGvp.get(BelastungsplanCalculator.SUM_IN));
            data.setSumOut(sumGvp.get(BelastungsplanCalculator.SUM_OUT));
        } else {
            final Map<String, BigDecimal[]> sums;
            if (isKreisverkehr) {
                sums = BelastungsplanCalculator.calcSumsKreisverkehr(data.getValues());
            } else {
                sums = BelastungsplanCalculator.calcSumsKreuzung(data.getValues());
            }
            data.setSum(sums.get(BelastungsplanCalculator.SUM));
            data.setSumIn(sums.get(BelastungsplanCalculator.SUM_IN));
            data.setSumOut(sums.get(BelastungsplanCalculator.SUM_OUT));
        }
        return data;
    }

    /**
     * Berechnet pro Summe den Prozentwert
     *
     * @param sumsKfz Summen von KFZ
     * @param sumsSvOrGv Summen von SV oder GV
     * @return Summen von SV% oder GV%
     */
    private static Map<String, BigDecimal[]> calculateSumsSvpOrGvpKreuzung(final Map<String, BigDecimal[]> sumsKfz,
            final Map<String, BigDecimal[]> sumsSvOrGv) {
        final Map<String, BigDecimal[]> sumsSvpOrGvp = new HashMap<>();
        sumsSvpOrGvp.put(BelastungsplanCalculator.SUM_IN,
                BelastungsplanCalculator.calculateAnteilProzent(sumsKfz.get(BelastungsplanCalculator.SUM_IN), sumsSvOrGv.get(BelastungsplanCalculator.SUM_IN)));
        sumsSvpOrGvp.put(BelastungsplanCalculator.SUM_OUT, BelastungsplanCalculator.calculateAnteilProzent(sumsKfz.get(BelastungsplanCalculator.SUM_OUT),
                sumsSvOrGv.get(BelastungsplanCalculator.SUM_OUT)));
        sumsSvpOrGvp.put(BelastungsplanCalculator.SUM,
                BelastungsplanCalculator.calculateAnteilProzent(sumsKfz.get(BelastungsplanCalculator.SUM), sumsSvOrGv.get(BelastungsplanCalculator.SUM)));
        return sumsSvpOrGvp;
    }

    /**
     * Summiert die einzelnen Werte in der List auf und packt sie an die entsprechende Stelle im Array
     *
     * @param listToSum Map mit allen zu addierenden Werten pro Knotenarm
     * @return Array mit allen Summen pro Knotenarm
     */
    private static BigDecimal[] sumValuesOfList(final Map<Integer, List<BigDecimal>> listToSum) {
        final BigDecimal[] sumPerNode = new BigDecimal[listToSum.size()];
        listToSum.forEach((key, value) -> {
            BigDecimal sum = new BigDecimal(0);
            for (BigDecimal bd : value) {
                sum = sum.add(bd);
            }
            sumPerNode[key] = sum;
        });
        return sumPerNode;
    }
}
