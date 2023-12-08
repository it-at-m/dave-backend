package de.muenchen.dave.util;

import com.google.common.base.Splitter;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.Wetter;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.services.IndexServiceUtils;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SuchwortUtil {

    public static final String SUCHWORT_SONDERZAEHLUNG = "Sonderzählung";

    public static final String SUCHWORT_KREISVERKEHR = "Kreisverkehr";

    public static Set<String> generateSuchworteOfZaehlstelle(final Zaehlstelle bean) {
        final Set<String> suchwoerter = new HashSet<>();
        if (StringUtils.isNotEmpty(bean.getStadtbezirk())) {
            final Set<String> stadtbezirke = new HashSet<>(Splitter.on("-").omitEmptyStrings().trimResults().splitToList(bean.getStadtbezirk()));
            suchwoerter.addAll(stadtbezirke);
            if (CollectionUtils.isNotEmpty(stadtbezirke) && stadtbezirke.size() > 1) {
                suchwoerter.add(bean.getStadtbezirk());
            }
        }
        return suchwoerter;
    }

    public static Set<String> generateSuchworteOfZaehlung(final Zaehlung bean) {
        final Set<String> suchwoerter = new HashSet<>();

        suchwoerter.addAll(getSuchworteOfWetter(bean.getWetter()));

        suchwoerter.addAll(getSuchworteOfZaehldauer(bean.getZaehldauer()));

        // Zaehlart ist in der Geographie enthalten
        if (ObjectUtils.isNotEmpty(bean.getGeographie())) {
            suchwoerter.addAll(bean.getGeographie());
        }
        if (StringUtils.isNotEmpty(bean.getJahreszeit())) {
            suchwoerter.add(bean.getJahreszeit());
        }
        if (StringUtils.isNotEmpty(bean.getProjektName())) {
            suchwoerter.add(bean.getProjektName());
        }
        if (StringUtils.isNotEmpty(bean.getJahr())) {
            suchwoerter.add(bean.getJahr());
        }
        if (StringUtils.isNotEmpty(bean.getMonat())) {
            suchwoerter.add(bean.getMonat());
        }
        if (StringUtils.isNotEmpty(bean.getTagesTyp())) {
            suchwoerter.add(bean.getTagesTyp());
        }
        if (bean.getDatum() != null) {
            suchwoerter.addAll(getSuchworteOfDatum(bean.getDatum()));
        }
        if (StringUtils.isNotEmpty(bean.getKreuzungsname())) {
            suchwoerter.add(bean.getKreuzungsname());
        }
        if (CollectionUtils.isNotEmpty(bean.getKnotenarme())) {
            suchwoerter.addAll(getSuchworteOfKnotenarme(bean.getKnotenarme()));
        }

        if (bean.getSonderzaehlung() != null && bean.getSonderzaehlung()) {
            suchwoerter.add(SUCHWORT_SONDERZAEHLUNG);
        }
        if (bean.getKreisverkehr() != null && bean.getKreisverkehr()) {
            suchwoerter.add(SUCHWORT_KREISVERKEHR);
        }

        return suchwoerter;
    }

    public static List<String> getSuchworteOfWetter(final String wetterAsString) {
        List<String> wetterSuchworte;
        try {
            wetterSuchworte = Wetter.valueOf(wetterAsString).getSuchwoerter();
        } catch (IllegalArgumentException | NullPointerException exception) {
            wetterSuchworte = Wetter.NO_INFORMATION.getSuchwoerter();
        }
        return wetterSuchworte;
    }

    public static List<String> getSuchworteOfZaehldauer(final String zaehldauerAsString) {
        List<String> zaehldauerSuchworte;
        try {
            zaehldauerSuchworte = Zaehldauer.valueOf(zaehldauerAsString).getSuchwoerter();
        } catch (IllegalArgumentException | NullPointerException exception) {
            zaehldauerSuchworte = Collections.emptyList();
        }
        return zaehldauerSuchworte;
    }

    public static List<String> getSuchworteOfDatum(final LocalDate datum) {
        final List<String> datumSuchworte = new ArrayList<>();
        if (datum != null) {
            final String datumAsString = datum.format(IndexServiceUtils.DDMMYYYY);
            if (StringUtils.isNotEmpty(datumAsString)) {
                datumSuchworte.add(datumAsString);
            }
            datumSuchworte.add(datum.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMANY));
        }
        return datumSuchworte;
    }

    public static List<String> getSuchworteOfKnotenarme(final List<Knotenarm> knotenarme) {
        final List<String> knotenarmeSuchworte = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(knotenarme)) {
            final Set<String> strassennamen = new HashSet<>();
            knotenarme.forEach(knotenarm -> {
                if (StringUtils.isNotEmpty(knotenarm.getStrassenname())) {
                    strassennamen.add(knotenarm.getStrassenname());
                }
            });
            knotenarmeSuchworte.addAll(strassennamen);

            switch (knotenarme.size()) {
            case 2:
                knotenarmeSuchworte.addAll(Arrays.asList("Querschnitt", "zweiarmig"));
                break;
            case 3:
                knotenarmeSuchworte.addAll(Arrays.asList("Einmündung", "dreiarmig"));
                break;
            case 4:
                knotenarmeSuchworte.addAll(Arrays.asList("Kreuzung", "vierarmig"));
                break;
            case 5:
                knotenarmeSuchworte.addAll(Arrays.asList("Kreuzung", "fünfarmig"));
                break;
            case 6:
                knotenarmeSuchworte.addAll(Arrays.asList("Kreuzung", "sechsarmig"));
                break;
            case 7:
                knotenarmeSuchworte.addAll(Arrays.asList("Kreuzung", "siebenarmig"));
                break;
            case 8:
                knotenarmeSuchworte.addAll(Arrays.asList("Kreuzung", "achtarmig"));
                break;
            }
        }
        return knotenarmeSuchworte;
    }

    public static Set<String> generateSuchworteOfMessstelle(final Messstelle bean) {
        final Set<String> suchwoerter = new HashSet<>();
        if (StringUtils.isNotEmpty(bean.getStadtbezirk())) {
            final Set<String> stadtbezirke = new HashSet<>(Splitter.on("-").omitEmptyStrings().trimResults().splitToList(bean.getStadtbezirk()));
            suchwoerter.addAll(stadtbezirke);
            if (CollectionUtils.isNotEmpty(stadtbezirke) && stadtbezirke.size() > 1) {
                suchwoerter.add(bean.getStadtbezirk());
            }
        }
        if (StringUtils.isNotEmpty(bean.getId())) {
            suchwoerter.add(bean.getId());
        }
        if (StringUtils.isNotEmpty(bean.getName())) {
            suchwoerter.add(bean.getName());
        }
        return suchwoerter;
    }
}
