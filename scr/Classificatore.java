package scr;

/**
 *
 * @author asaggese
 */
public class Classificatore {

    public Classificatore() {
    }
    
    public static void classifica(Sample testSample) {

        //valore di k per il K-NN. Se voglio usare NN, allora k=1 altrimenti k= (es) 5
        int k = 5;
        String prototypes_filename = "../src/scr/dataset.csv";
        
        // Costruisco il mio classificatore a partire dal nome del file dei prototipi
        NearestNeighbor knn = new NearestNeighbor(prototypes_filename);

        // Per classificare il campione testSample, richiama la classe Classify
        int predictedClass = knn.classify(testSample, k);

        System.out.println("Predicted class for point (" + 
                testSample.getAngleToTrackAxis() + ", " + 
                testSample.getTrackPosition() + ", " + 
                testSample.getTrackEdgeSensor10() + ", " + 
                testSample.getTrackEdgeSensors9() + ", " + 
                testSample.getTrackEdgeSensors8() + ", " + 
                testSample.getRpm() + ", " + 
                testSample.getGear() + ", " + 
                testSample.getSteering() + ", " + 
                testSample.getAccelerate() + ", " + 
                testSample.getBrake() + ", " + 
                testSample.getClutch() + ") is: " + 
                predictedClass
        );
    }
    
}
