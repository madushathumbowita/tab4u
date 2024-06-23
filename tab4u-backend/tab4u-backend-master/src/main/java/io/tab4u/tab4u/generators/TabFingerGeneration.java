/**
   Madusha Thumbowita
   20191207/w1790818
 */

package io.tab4u.tab4u.generators;

import io.tab4u.tab4u.generators.classes.Fret;
import io.tab4u.tab4u.generators.classes.FretTab;
import io.tab4u.tab4u.generators.classes.PeakFrequencyWithTimestamp;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TabFingerGeneration {

    private static final List<LinkedList<Fret>> tabLists = new ArrayList<>();
    private static List<Fret> sameNoteList;
    private static double[][] fretBoard;
    private static List<FretTab> fretTabs;
    private static int numOfFrets;
    private static String[] openStringArr;
    private static int numOfStrings;
    private static final int FINGER_COUNT = 4; //Four fingers to play

    public JSONArray generateTablature(AudioInputStream audioInputStream) {
        // Initialize guitar parameters
        numOfFrets = 24;
        openStringArr = new String[]{"e", "B", "G", "D", "A", "E"};
        numOfStrings = openStringArr.length;

        // Open string frequencies
        String[] tempArr = {
            "e-329.63", "B-246.94", "G-196.00", "D-146.83", "A-110.00", "E-82.41"
        };

        //sdfgsadfg
        // Initialize the fretBoard
        initializeFretBoard(tempArr);

        // Get frequencies from AudioFFT
        List<PeakFrequencyWithTimestamp> peaksWithTimestamp = AudioFFT.analyzeAudioFile(audioInputStream);
        List<Double> detectedFrequencies = AudioFFT.getDetectedFrequencies(peaksWithTimestamp);

        // Process frequencies into fretTabs
        fretTabs = new ArrayList<>();
        for (double freq : detectedFrequencies) {
            fretTabs.add(new FretTab(freq));
        }
        normalizeFretFrequencies();
        Find(fretTabs, fretBoard);

        // Convert fretTabs to JSON and print
        JSONArray jsonArray = new JSONArray();
        for (FretTab tab : fretTabs) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fretFrequency", tab.getFretFrequency());
            jsonObject.put("fret", tab.getFret());
            jsonObject.put("stringNo", tab.getString());
            jsonObject.put("finger", tab.getFinger());
            jsonArray.put(jsonObject); 
            log.debug("The json object " + jsonObject);
        }
        return jsonArray;
    }

    public static void Find(List<FretTab> fretTabs, double[][] fretBoard) {
        sameNoteList = new ArrayList<>();
        double[] tempFreqArr = new double[fretTabs.size()]; // Temporary created array holding frequencies to use for the Find
        for (int i = 0; i < fretTabs.size(); i++) {
            tempFreqArr[i] = fretTabs.get(i).getFretFrequency();
        }
        // Getting different positions of start notes
        Outer:
        for (int x = 0; x < fretBoard.length; x++) {
            for (int y = 0; y < fretBoard[x].length; y++) {
                if (tempFreqArr[0] == fretBoard[x][y]) {
                    LinkedList<Fret> tabList = new LinkedList<>(); // Create new linked lists for all start note frets
                    tabList.add(new Fret(x, y));
                    tabLists.add(tabList); 
                    continue Outer;
                }
            }
        }
        for (int i = 1; i < tempFreqArr.length; i++) { // Iterates on the transcribed frequencies
            for (int x = 0; x < fretBoard.length; x++) {
                for (int y = 0; y < fretBoard[x].length; y++) {
                    if (tempFreqArr[i] == fretBoard[x][y]) {
                        sameNoteList.add(new Fret(x, y));
                    }
                }
            }
            leastGeometricLength();
            sameNoteList.clear(); // To accommodate new frets
        }
        finalTabs(fretTabs); // Sets the final tablature considering low geometric cost
    }

    // Initialize the fretBoard array
    public static void initializeFretBoard(String[] tempArr) {
        fretBoard = new double[openStringArr.length][numOfFrets + 1]; // +1 due to open string
        Outer:
        for (int i = 0; i < numOfStrings; i++) {
            for (String s : tempArr) {
                String tempLine = s.split("-")[0]; // Checks the String name
                if (tempLine.equals(openStringArr[i])) {
                    fretBoard[i][0] = Double.parseDouble(s.split("-")[1]); // Sets the open string frequency
                    calculateFrequencies(fretBoard, i, numOfFrets);
                    continue Outer;
                }
            }
        }
    }

    // Calculating the frequencies of rest of the frets in the string
    private static void calculateFrequencies(double[][] temp, int stringNumber, int numOfFrets) {
        for (int x = 1; x <= numOfFrets; x++) { // x is defined as 1 because 0 is already the open string frequency
            temp[stringNumber][x] = Math.round(temp[stringNumber][0] * (Math.pow(2, (x / 12.0))));
        }
    }

    // Adjusting the frequencies in the fretTabs list to match the closest fret frequency on the guitar's fretboard.
    private static void normalizeFretFrequencies() {
        double tempFreq = 0.0;
        Outer:
        for (FretTab fr : fretTabs) {
            for (int i = 0; i < numOfStrings; i++) {
                tempFreq = fr.getFretFrequency();
                if (tempFreq > fretBoard[i][0]) { // So that frequencies of one string are only iterated.
                    for (int j = 1; j < numOfFrets; j++) {
                        if ((tempFreq > (fretBoard[i][j - 1] + fretBoard[i][j]) / 2) &&
                                (tempFreq < (fretBoard[i][j + 1] + fretBoard[i][j]) / 2)) {
                            fr.setFretFrequency(fretBoard[i][j]);
                            continue Outer;
                        }
                    }
                }
            }
            // The for loop will only come here if the mapping frequency is the 0th fret of the lowest string.
            if ((tempFreq > (fretBoard[numOfStrings - 1][0]) &&
                    (tempFreq < (fretBoard[numOfStrings - 1][1] + fretBoard[numOfStrings - 1][0]) / 2))) {
                fr.setFretFrequency(fretBoard[numOfStrings - 1][0]);
            }
        }
    }

    // For each of the existing tab lists, this method calculates the geometric cost for each of the newly found frets.
    // Then it sets the fret with the lowest geometric cost to the respective tab list.
    private static void leastGeometricLength() {
        ListIterator<Fret> linkedTabs;
        Fret fret;
        for (LinkedList<Fret> l : tabLists) { // Iterate through the different linked lists for different start frets
            List<Fret> possibleNextFrets = new ArrayList<>();
            for (Fret fr : sameNoteList) { // Iterate through the new identified frets
                linkedTabs = l.listIterator(0);
                double totalGeoCost = 0.0;
                while (linkedTabs.hasNext()) {
                    fret = linkedTabs.next();
                    totalGeoCost += calculateHypotenuse(Math.abs(fret.getStringNo() - fr.getStringNo()), Math.abs(fret.getFret() - fr.getFret()));
                }
                fr.setGeoCost(totalGeoCost);
                possibleNextFrets.add(new Fret(fr.getStringNo(), fr.getFret(), totalGeoCost));
            }
            possibleNextFrets = possibleNextFrets.stream().filter(fr -> fr.getFret() <= 12).collect(Collectors.toList());
            // Sort the possible next frets by geometric cost and by fret position to prefer lower frets
            possibleNextFrets.sort((a, b) -> {
                int result = Double.compare(a.getGeoCost(), b.getGeoCost());
                if (result != 0) {
                    return result;
                } else {
                    return Integer.compare(a.getFret(), b.getFret());
                }
            });
            // Add the fret with the lowest geoCost to the tab list
            Fret nextFret = possibleNextFrets.get(0);
            l.addLast(nextFret);
        }
    }

    // Calculate hypotenuse
    private static double calculateHypotenuse(double a, double b) {
        return Math.hypot(a, b);
    }

    // Set the final tablature with the lowest geometric cost
    private static void finalTabs(List<FretTab> fretTabs) {
        double tempGeoCost;
        LinkedList<Fret> tempList = tabLists.get(0);
        double geoCost = tabLists.get(0).getLast().getGeoCost();
        for (int x = 1; x < tabLists.size(); x++) {
            tempGeoCost = tabLists.get(x).getLast().getGeoCost();
            if (tempGeoCost < geoCost) {
                geoCost = tempGeoCost;
                tempList = tabLists.get(x);
            }
        }
        for (int x = 0; x < fretTabs.size(); x++) {
            fretTabs.get(x).setFret(tempList.get(x).getFret());
            fretTabs.get(x).setString(tempList.get(x).getStringNo());
        }    
        // After creating the final tablature, generating the finger positions
        generateFingerPositions(fretTabs);
    }

    // Calculate the cost of reaching another fret
    private static double fingerReachCost(int currFinger, int nextFinger, int numOfFrets) {
        int fingDiff = Math.abs(nextFinger - currFinger); // Could be going to a lesser fret
        if (numOfFrets > (fingDiff + 1)) return -1; // This is unreachable
        if (fingDiff == numOfFrets) return 0; // The finger is already on the fret position. No cost is applied
        if (numOfFrets == (fingDiff + 1)) return 1; // Maximum reach of one fret after initial
        return 0.5 * (fingDiff - numOfFrets); // 0.5 cost for each fret which comes near to the current finger
    }

    // Method to calculate the cost of hand repositioning
    private static int calculateHandRepositionCost(int numberOfFrets) {
        return numberOfFrets * 2;
    }

    // Method to find the next finger for the next fret
    private static int searchNextFinger(int currString, int nextString, int currFret, int nextFret, int currFinger) {
        if ((currString == nextString) && (currFret == nextFret)) return currFinger;
        int tempCurrFinger = currFinger;
        int numOfFrets = Math.abs(nextFret - currFret);
        double cost, tempCost;
        int nextFinger = 0;
        // Ensure next finger increments appropriately if there is at least one fret space
        if (currString == nextString && numOfFrets > 0) {
            tempCurrFinger += numOfFrets;
            if (tempCurrFinger > 4) tempCurrFinger = 4;  // Ensure we do not exceed 4th finger
            return tempCurrFinger;
        }
        // If the transition is towards a lower string, then the finger must be a higher finger, and vice versa
        if (((nextString >= currString) && (nextFret > currFret)) || ((nextString < currString) && (nextFret >= currFret))) {
            // This is the only logical option
            if ((currFret == nextFret) && (currFinger == 4)) return currFinger;
            // Initial assignment with one step higher finger
            cost = fingerReachCost(tempCurrFinger, tempCurrFinger + 1, numOfFrets);
            tempCurrFinger++;
            nextFinger = tempCurrFinger;
            while (tempCurrFinger < 4) {
                tempCost = fingerReachCost(tempCurrFinger, tempCurrFinger + 1, numOfFrets);
                // Include hand reposition cost if the finger reach cost is -1 (unreachable)
                if (tempCost == -1) {
                    tempCost = calculateHandRepositionCost(numOfFrets);
                }
                tempCurrFinger++;
                // Compare the costs of finger reach
                if (tempCost < cost) {
                    cost = tempCost;
                    nextFinger = tempCurrFinger;
                }
            }
            return nextFinger;
        } else {
            // This is the only logical option
            if ((currFret == nextFret) && (currFinger == 1)) return currFinger;

            // Initial assignment with one step lower finger
            cost = fingerReachCost(tempCurrFinger, tempCurrFinger - 1, numOfFrets);
            tempCurrFinger--;
            nextFinger = tempCurrFinger;
            while (tempCurrFinger > 1) {
                tempCost = fingerReachCost(tempCurrFinger, tempCurrFinger - 1, numOfFrets);

                // Include hand reposition cost if the finger reach cost is -1 (unreachable)
                if (tempCost == -1) {
                    tempCost = calculateHandRepositionCost(numOfFrets);
                }
                tempCurrFinger--;
                // Compare the costs of finger reach
                if (tempCost < cost) {
                    cost = tempCost;
                    nextFinger = tempCurrFinger;
                }
            }
            return nextFinger;
        }
    }

    // Method to generate finger positions for the tablature
    private static void generateFingerPositions(List<FretTab> fretTabs) {
        // Start with the index finger
        int currentFinger = 1;
        // Loop through the fretTabs list to determine finger positions
        for (int i = 0; i < fretTabs.size(); i++) {
            FretTab tab = fretTabs.get(i);
            // If it's the first note or a new string, assign index finger (finger 1)
            if (i == 0 || tab.getString() != fretTabs.get(i - 1).getString()) {
                currentFinger = 1;
            }
            // Set the finger for the current note
            tab.setFinger(currentFinger);
            // Find the next finger for the next note
            if (i < fretTabs.size() - 1) {
                FretTab nextTab = fretTabs.get(i + 1);
                currentFinger = searchNextFinger(tab.getString(), nextTab.getString(), tab.getFret(), nextTab.getFret(), currentFinger);
            }
            // Ensuring finger is inside the valid range (1 to FINGER_COUNT)
            tab.setFinger(Math.max(1, Math.min(FINGER_COUNT, tab.getFinger())));
        }
    }
}