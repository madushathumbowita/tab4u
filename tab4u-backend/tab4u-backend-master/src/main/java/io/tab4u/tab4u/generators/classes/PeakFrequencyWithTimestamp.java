/**
   Madusha Thumbowita
   20191207/w1790818
 */

package io.tab4u.tab4u.generators.classes;

public class PeakFrequencyWithTimestamp {
    private final double frequency;
    private final long timestamp;

    public PeakFrequencyWithTimestamp(double frequency, long timestamp) {
        this.frequency = frequency;
        this.timestamp = timestamp;
    }

    public double getFrequency() {
        return frequency;
    }

    public long getTimestamp() {
        return timestamp;
    }
}