package scr;

/**
 *
 * @author asaggese
 */
public class Classificatore {

    public Classificatore() {}
    
    public static int classifica(Sample testSample) {

        //valore di k per il K-NN. Se voglio usare NN, allora k=1 altrimenti k= (es) 5
        int k = 5;
        String prototypes_filename = "../src/dataset.csv";
        
        // Costruisco il mio classificatore a partire dal nome del file dei prototipi
        NearestNeighbor knn = new NearestNeighbor(prototypes_filename);

        // Per classificare il campione testSample, richiama la classe Classify
        int predictedClass = knn.classify(testSample, k);

        System.out.println("Predicted class for point (" + 
                testSample.getAngleToTrackAxis() + ", " + 
                testSample.getTrackPosition() + ", " + 
                testSample.getTrackEdgeSensor14() + ", " + 
                testSample.getTrackEdgeSensor13() + ", " + 
                testSample.getTrackEdgeSensor12() + ", " + 
                testSample.getTrackEdgeSensor11() + ", " + 
                testSample.getTrackEdgeSensor10() + ", " + 
                testSample.getTrackEdgeSensor9() + ", " + 
                testSample.getTrackEdgeSensor8() + ", " + 
                testSample.getTrackEdgeSensor7() + ", " + 
                testSample.getTrackEdgeSensor6() + ", " + 
                testSample.getTrackEdgeSensor5() + ", " + 
                testSample.getTrackEdgeSensor4() + ", " + 
                testSample.getRpm() + ", " + 
                testSample.getXSpeed() + ", " +
                testSample.getYSpeed() + ") is " +
                predictedClass
        );

        return predictedClass;
    }
    
}
