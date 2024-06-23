/**
   Madusha Thumbowita
   20191207/w1790818
 */

package io.tab4u.tab4u.generators.classes;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class FretTab {
    private double fretFrequency;
    private int fret;
    private int stringNo;
    private int finger;

    public FretTab(double fretFrequency) {
        this.fretFrequency = fretFrequency;
    }
    public int getString() {
        return stringNo;
    }
    public void setString(int stringNo) {
        this.stringNo = stringNo;
    }

}
