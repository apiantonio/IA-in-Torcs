package scr;

/**
 *
 * @author asaggese
 */
public class Classificatore {

    //valore di k per il K-NN. Se voglio usare NN, allora k=1 altrimenti k= (es) 5
    public static final int K = 5;

    public Classificatore() {}
    
    public static int classifica(Sample testSample) {

        String prototypes_filename = "../src/dataset.csv";
        
        // Costruisco il mio classificatore a partire dal nome del file dei prototipi
        NearestNeighbor knn = new NearestNeighbor(prototypes_filename);

        // Per classificare il campione testSample, richiama la classe Classify
        int predictedClass = knn.classify(testSample, K);

        System.out.println("Predicted class for point (" + 
                testSample.getAngleToTrackAxis() + ", " + 
                testSample.getTrackPosition() + ", " + 
                testSample.getTrackEdgeSensor11() + ", " + 
                testSample.getTrackEdgeSensor10() + ", " + 
                testSample.getTrackEdgeSensor9() + ", " + 
                testSample.getTrackEdgeSensor8() + ", " + 
                testSample.getTrackEdgeSensor7() + ", " + 
                testSample.getXSpeed() + ", " +
                testSample.getYSpeed() + ") is " +
                predictedClass
        );

        return predictedClass;
    }
    
}
