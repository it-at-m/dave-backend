package de.muenchen.dave.domain.dtos.laden;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum VerkehrsbeziehungQJS {
    VON_1_NACH_3_W(1,3, Himmelsrichtung.W),
    VON_1_NACH_3_O(1,3, Himmelsrichtung.O),
    VON_2_NACH_4_N(2,4, Himmelsrichtung.N),
    VON_2_NACH_4_S(2,4, Himmelsrichtung.S),
    VON_3_NACH_1_W(3,1, Himmelsrichtung.W),
    VON_3_NACH_1_O(3,1, Himmelsrichtung.O),
    VON_4_NACH_2_N(4,2, Himmelsrichtung.N),
    VON_4_NACH_2_S(4,2, Himmelsrichtung.S),
    VON_5_NACH_7_NW(5,7, Himmelsrichtung.NW),
    VON_5_NACH_7_SO(5,7, Himmelsrichtung.SO),
    VON_6_NACH_8_NO(6,8, Himmelsrichtung.NO),
    VON_6_NACH_8_SW(6,8, Himmelsrichtung.SW),
    VON_7_NACH_5_NW(7,5, Himmelsrichtung.NW),
    VON_7_NACH_5_SO(7,5, Himmelsrichtung.SO),
    VON_8_NACH_6_NO(8,6, Himmelsrichtung.NO),
    VON_8_NACH_6_SW(8,6, Himmelsrichtung.SW);

    private int von;
    private int nach;
    private Himmelsrichtung strassenseite;

//    private static final Map<Key, VerkehrsbeziehungQJS> LOOKUP = new HashMap<>();
//    static {
//        for (VerkehrsbeziehungQJS v : values()) {
//            LOOKUP.put(new Key(v.von, v.nach, v.strassenseite), v);
//        }
//    }
}
