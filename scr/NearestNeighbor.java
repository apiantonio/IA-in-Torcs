package scr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Classificatore KNN
public class NearestNeighbor {

    private final List<Sample> trainingData;
    private KDTree kdtree;
    private final int[] classCounts; // VERIFICA NEI COSTRUTTORI CHE QUESTO SIA CONFORME CON QUELLO CHE STAI IPOTIZZANDO!
    private final String firstLineOfTheFile; // VERIFICA NEI COSTRUTTORI CHE QUESTO SIA CONFORME CON QUELLO CHE STAI IPOTIZZANDO!
    
    public static final int NUM_CLASS = 7; // Le classi vanno da 0 a 6

    public NearestNeighbor(String filename) {
        this.trainingData = new ArrayList<>();
        this.kdtree = null;
        this.classCounts = new int[NUM_CLASS];
        this.firstLineOfTheFile = "angleToTrackAxis,trackPosition,11Sensor,rxSensor,ctrSensor,sxSensor,7Sensor,xSpeed,ySpeed,class";
        this.readPointsFromCSV(filename);
    }
    
    private void readPointsFromCSV(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(firstLineOfTheFile)) {
                    continue; // Skip header
                }
                // Aggiungo il campione richiamando il costruttore che prende come input la stringa letta
                trainingData.add(new Sample(line));
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.kdtree = new KDTree(trainingData); // Inizializza il KDTree utilizzando i punti letti
    }

    public List<Sample> findKNearestNeighbors(Sample testPoint, int k) {
        return kdtree.kNearestNeighbors(testPoint, k);
    }

    public int classify(Sample testPoint, int k) {
        List<Sample> kNearestNeighbors = findKNearestNeighbors(testPoint, k);

        // Count the occurrences of each class in the k nearest neighbors
        for (Sample neighbor : kNearestNeighbors) {
            classCounts[neighbor.getCls()]++;
        }

        // Find the class with the maximum count
        int maxCount = -1;
        int predictedClass = -1;
        for (int i = 0; i < classCounts.length; i++) {
            if (classCounts[i] > maxCount) {
                maxCount = classCounts[i];
                predictedClass = i;
            }
        }

        return predictedClass;
    }

    public List<Sample> getTrainingData() {
        return trainingData;
    }

}


