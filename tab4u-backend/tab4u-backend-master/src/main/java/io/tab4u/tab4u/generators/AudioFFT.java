/**
 Madusha Thumbowita
 20191207/w1790818
 */

package io.tab4u.tab4u.generators;

import org.jtransforms.fft.DoubleFFT_1D;

import io.tab4u.tab4u.generators.classes.PeakFrequencyWithTimestamp;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
 
 public class AudioFFT {
    
    // Standard frequencies of guitar fretboard notes
     private static final double[] GUITAR_FRETBOARD_FREQUENCIES = {
         82.41, 87.31, 92.50, 98.00, 103.83, 110.00, 116.54, 123.47, 130.81, 138.59, 146.83, 155.56, 164.81,
         174.61, 185.00, 196.00, 207.65, 220.00, 233.08, 246.94, 261.63, 277.18, 293.66, 311.13, 329.63, 349.23,
         369.99, 392.00, 415.30, 440.00, 466.16, 493.88, 523.25, 554.37, 587.33, 622.25, 659.26
     };
 
     /**
     * Analyzes the provided audio input stream to detect peak frequencies.
     * AudioInputStream The input stream containing audio data.
     * A list of PeakFrequencyWithTimestamp objects representing detected peaks.
     */
     public static List<PeakFrequencyWithTimestamp> analyzeAudioFile(AudioInputStream audioInputStream) {
         int maxDurationSeconds = 20;
         int frameSize = 4096;
         // Change the overlap to 1 for better accuracy
         int overlap = frameSize / 1;

         List<PeakFrequencyWithTimestamp> peaksWithTimestamp = new ArrayList<>();
 
         try {
            // Adjust audio format stereo to mono if necessary
             AudioFormat format = audioInputStream.getFormat();
             if (format.getChannels() > 1) {
                 format = new AudioFormat(format.getSampleRate(), 16, 1, true, false);
                 audioInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);
             }
 
             AudioFormat audioFormat = audioInputStream.getFormat();
             int sampleRate = (int) audioFormat.getSampleRate();
             long maxFrames = (long) maxDurationSeconds * sampleRate;
             long frameLength = Math.min(audioInputStream.getFrameLength(), maxFrames);
             byte[] audioData = new byte[(int) frameLength * audioFormat.getFrameSize()];
             int bytesRead = audioInputStream.read(audioData);
 
             double[] samples = new double[bytesRead / 2];
             for (int i = 0, j = 0; i < bytesRead; i += 2, j++) {
                 samples[j] = ((audioData[i + 1] & 0xFF) << 8 | (audioData[i] & 0xFF)) / 32768.0;
             }
 
             // Apply spectral subtraction to reduce noise
             samples = applySpectralSubtraction(samples, sampleRate);
             long currentTimestamp = 0;
             // Process audio data in frames with FFT
             for (int start = 0; start < samples.length - frameSize; start += overlap) {
                 double[] frame = Arrays.copyOfRange(samples, start, start + frameSize);
                 applyWindow(frame);
 
                 double[] fftData = new double[frameSize * 2];
                 System.arraycopy(frame, 0, fftData, 0, frame.length);
                 
                 // Perform FFT to obtain magnitude spectrum
                 DoubleFFT_1D fft = new DoubleFFT_1D(frameSize);
                 fft.realForward(fftData);
 
                 double[] magnitudeSpectrum = calculateMagnitudeSpectrum(fftData);
                 double noiseThreshold = 800;
 
                 // Detect peaks in magnitude spectrum
                 for (int i = 1; i < magnitudeSpectrum.length - 1; i++) {
                     if (magnitudeSpectrum[i] >= noiseThreshold && magnitudeSpectrum[i] > magnitudeSpectrum[i - 1] && magnitudeSpectrum[i] > magnitudeSpectrum[i + 1]) {
                         peaksWithTimestamp.add(new PeakFrequencyWithTimestamp((double) i * sampleRate / frameSize, currentTimestamp));
                     }
                 }
 
                 currentTimestamp += overlap;
             }
 
             // Sort detected peaks by timestamp
             peaksWithTimestamp.sort(Comparator.comparingLong(PeakFrequencyWithTimestamp::getTimestamp));

             // Remove outliers from detected peaks
             peaksWithTimestamp = removeOutliers(peaksWithTimestamp);
             
             // Close the audio input stream
             audioInputStream.close();
         } catch (IOException e) {
             System.out.println(e.getLocalizedMessage());
         }
 
         return peaksWithTimestamp;
     }
 
     // Applies a window function to samples to reduce spectral leakage
     private static void applyWindow(double[] samples) {
         for (int i = 0; i < samples.length; i++) {
             samples[i] *= 0.74 - 0.16 * Math.cos(2 * Math.PI * i / (samples.length - 1));
         }
     }

     // Calculates magnitude spectrum from FFT data
     private static double[] calculateMagnitudeSpectrum(double[] fftData) {
         double[] magnitudeSpectrum = new double[fftData.length / 2];
         for (int i = 0; i < magnitudeSpectrum.length; i++) {
             magnitudeSpectrum[i] = Math.sqrt(fftData[2 * i] * fftData[2 * i] + fftData[2 * i + 1] * fftData[2 * i + 1]);
         }
         return magnitudeSpectrum;
     }
 
     // Applies spectral subtraction to samples to reduce noise
     private static double[] applySpectralSubtraction(double[] samples, int sampleRate) {
         int frameSize = 4096;
         int overlap = frameSize / 2;
         double noiseEstimateDuration = 0.1;
         int noiseFrames = (int) (sampleRate * noiseEstimateDuration / (frameSize - overlap));
 
         List<double[]> noiseFramesList = new ArrayList<>();
         for (int start = 0; start < noiseFrames * (frameSize - overlap); start += frameSize - overlap) {
             double[] frame = Arrays.copyOfRange(samples, start, start + frameSize);
             applyWindow(frame);
             noiseFramesList.add(frame);
         }
 
         double[] avgNoiseSpectrum = new double[frameSize];
         for (double[] noiseFrame : noiseFramesList) {
             double[] fftData = new double[frameSize * 2];
             System.arraycopy(noiseFrame, 0, fftData, 0, noiseFrame.length);
 
             DoubleFFT_1D fft = new DoubleFFT_1D(frameSize);
             fft.realForward(fftData);
 
             double[] magnitudeSpectrum = calculateMagnitudeSpectrum(fftData);
             for (int i = 0; i < avgNoiseSpectrum.length; i++) {
                 avgNoiseSpectrum[i] += magnitudeSpectrum[i];
             }
         }
         for (int i = 0; i < avgNoiseSpectrum.length; i++) {
             avgNoiseSpectrum[i] /= noiseFramesList.size();
         }
 
         double[] outputSamples = new double[samples.length];
         int outputIndex = 0;
         for (int start = 0; start < samples.length - frameSize; start += frameSize - overlap) {
             double[] frame = Arrays.copyOfRange(samples, start, start + frameSize);
             applyWindow(frame);
 
             double[] fftData = new double[frameSize * 2];
             System.arraycopy(frame, 0, fftData, 0, frame.length);
 
             DoubleFFT_1D fft = new DoubleFFT_1D(frameSize);
             fft.realForward(fftData);
 
             double[] magnitudeSpectrum = calculateMagnitudeSpectrum(fftData);
             for (int i = 0; i < magnitudeSpectrum.length; i++) {
                 magnitudeSpectrum[i] = Math.max(magnitudeSpectrum[i] - avgNoiseSpectrum[i], 0);
             }
 
             for (int i = 0; i < fftData.length / 2; i++) {
                 double phase = Math.atan2(fftData[2 * i + 1], fftData[2 * i]);
                 fftData[2 * i] = magnitudeSpectrum[i] * Math.cos(phase);
                 fftData[2 * i + 1] = magnitudeSpectrum[i] * Math.sin(phase);
             }
 
             fft.realInverse(fftData, true);
 
             for (int i = 0; i < frameSize; i++) {
                 outputSamples[outputIndex + i] += fftData[i] / ((double) frameSize / overlap);
             }
             outputIndex += frameSize - overlap;
         }
 
         return outputSamples;
     }
 
     // Finds the closest frequency from the guitar fretboard frequencies
     private static double findClosestFrequency(double frequency) {
         double minDifference = Double.MAX_VALUE;
         double closestFrequency = frequency;
         for (double fretboardFrequency : GUITAR_FRETBOARD_FREQUENCIES) {
             double diff = Math.abs(frequency - fretboardFrequency);
             if (diff < minDifference) {
                 minDifference = diff;
                 closestFrequency = fretboardFrequency;
             }
         }
         return closestFrequency;
     }

     // Removes outliers from detected peaks based on predefined frequency range
     private static List<PeakFrequencyWithTimestamp> removeOutliers(List<PeakFrequencyWithTimestamp> peaks) {
         List<PeakFrequencyWithTimestamp> filteredPeaks = new ArrayList<>();
         double minFrequency = GUITAR_FRETBOARD_FREQUENCIES[0];
         double maxFrequency = GUITAR_FRETBOARD_FREQUENCIES[GUITAR_FRETBOARD_FREQUENCIES.length - 1];
 
         for (PeakFrequencyWithTimestamp peak : peaks) {
             if (peak.getFrequency() >= minFrequency && peak.getFrequency() <= maxFrequency) {
                 filteredPeaks.add(peak);
             }
         }
         return filteredPeaks;
     }

     // Retrieves detected frequencies from the list of peaks
     public static List<Double> getDetectedFrequencies(List<PeakFrequencyWithTimestamp> peaksWithTimestamp) {
        List<Double> detectedFrequencies = new ArrayList<>();
        for (PeakFrequencyWithTimestamp peak : peaksWithTimestamp) {
            double closestFrequency = findClosestFrequency(peak.getFrequency());
            if (!detectedFrequencies.contains(closestFrequency)) {
                detectedFrequencies.add(closestFrequency);
            }
        }
        return detectedFrequencies;
    }
 }
 