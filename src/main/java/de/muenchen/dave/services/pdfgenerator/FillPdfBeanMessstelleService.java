package de.muenchen.dave.services.pdfgenerator;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteListenausgabeDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOptionsDTO;
import de.muenchen.dave.domain.mapper.DatentabellePdfMessstelleMapper;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldaten;
import de.muenchen.dave.services.messstelle.MesswerteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FillPdfBeanMessstelleService {

    private final MesswerteService messwerteService;
    private final DatentabellePdfMessstelleMapper datentabellePdfMessstelleMapper;

    /**
     * Diese Methode befüllt ein Objekt der Klasse {@link DatentabellePdfZaehldaten} und gibt dieses
     * zurück.
     *
     * @param options Die im Frontend ausgewählten Optionen.
     * @param mstId ID der im Frontend ausgewählten Messstelle
     * @return Befülltes Objekt vom Typ {@link DatentabellePdfZaehldaten}.
     */
    public DatentabellePdfZaehldaten getDatentabellePdf(final MessstelleOptionsDTO options, final String mstId) {
        final LadeMesswerteListenausgabeDTO zaehldatenTable = messwerteService.ladeMesswerte(mstId, options).getZaehldatenTable();
        final List<LadeMesswerteDTO> zaehlwerteDtos = zaehldatenTable.getZaehldaten();

        final FahrzeugOptionsDTO optionsFahrzeuge = options.getFahrzeuge();
        final DatentabellePdfZaehldaten datentabellePdfZaehldaten = this.datentabellePdfMessstelleMapper
                .fahrzeugOptionsToDatentabellePdfZaehldaten(optionsFahrzeuge);
        datentabellePdfZaehldaten.setActiveTabsFahrzeugtypen(this.calcActiveTabsFahrzeugtypen(optionsFahrzeuge));
        datentabellePdfZaehldaten.setActiveTabsFahrzeugklassen(this.calcActiveTabsFahrzeugklassen(optionsFahrzeuge));
        datentabellePdfZaehldaten.setActiveTabsAnteile(this.calcActiveTabsAnteile(optionsFahrzeuge));

        datentabellePdfZaehldaten.setZaehldatenList(this.datentabellePdfMessstelleMapper.ladeMesswerteDTOList2beanList(zaehlwerteDtos));
        return datentabellePdfZaehldaten;
    }

    private int calcActiveTabsFahrzeugtypen(final FahrzeugOptionsDTO optionsDTO) {
        int activeTabsFahrzeugtypen = 0;
        if (optionsDTO.isPersonenkraftwagen()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isLastkraftwagen()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isLastzuege()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isLieferwagen()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isBusse()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isKraftraeder()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isRadverkehr()) {
            activeTabsFahrzeugtypen++;
        }
        if (optionsDTO.isFussverkehr()) {
            activeTabsFahrzeugtypen++;
        }
        return activeTabsFahrzeugtypen;
    }

    private int calcActiveTabsFahrzeugklassen(final FahrzeugOptionsDTO optionsDTO) {
        int activeTabsFahrzeugklasse = 0;
        if (optionsDTO.isKraftfahrzeugverkehr()) {
            activeTabsFahrzeugklasse++;
        }
        if (optionsDTO.isSchwerverkehr()) {
            activeTabsFahrzeugklasse++;
        }
        if (optionsDTO.isGueterverkehr()) {
            activeTabsFahrzeugklasse++;
        }
        return activeTabsFahrzeugklasse;
    }

    private int calcActiveTabsAnteile(final FahrzeugOptionsDTO optionsDTO) {
        int activeTabsAnteile = 0;
        if (optionsDTO.isSchwerverkehrsanteilProzent()) {
            activeTabsAnteile++;
        }
        if (optionsDTO.isGueterverkehrsanteilProzent()) {
            activeTabsAnteile++;
        }
        return activeTabsAnteile;
    }

}
