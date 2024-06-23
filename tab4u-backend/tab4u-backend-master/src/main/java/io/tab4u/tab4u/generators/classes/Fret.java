/**
   Madusha Thumbowita
   20191207/w1790818
 */

package io.tab4u.tab4u.generators.classes;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Fret {
    private int stringNo;
    private int fret;
    private double geoCost;

    public Fret(int stringNo, int fret) {
        this.stringNo = stringNo;
        this.fret = fret;
    }
}