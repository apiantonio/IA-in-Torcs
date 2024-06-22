package scr;

/**
 * permette di classificare un sample passato come parametro al metodo classifica() 
 * e stampa la classe predetta su un file di output.
 */
public class Classificatore {

    //valore di k per il K-NN. Se voglio usare NN allora k=1 altrimenti k = (es) 5
    public static final int K = 5; // Importante sia dispari per evitare comportamenti indesiderati

    public Classificatore() {}

    
    /**
     * Determina la classe di un punto passato in input
     * 
     * @param sample rappresenta il punto da classificare
     * @return un intero che rappresenta la classe predetta 
     */
    public static int classifica(Sample sample) {

        String prototypes_filename = "../src/dataset.csv";
        
        // Costruisco il mio classificatore a partire dal nome del file dei prototipi
        NearestNeighbor knn = new NearestNeighbor(prototypes_filename);

        // Per classificare il campione sample, richiama la classe Classify
        int predictedClass = knn.classify(sample, K);

        /* System.out.println("Predicted class for point (" + 
            sample.getAngleToTrackAxis() + ", " + 
            sample.getTrackPosition() + ", " + 
            sample.getTrackEdgeSensor11() + ", " + 
            sample.getTrackEdgeSensor10() + ", " + 
            sample.getTrackEdgeSensor9() + ", " + 
            sample.getTrackEdgeSensor8() + ", " + 
            sample.getTrackEdgeSensor7() + ", " + 
            sample.getXSpeed() + ", " +
            sample.getYSpeed() + ") is " +
            predictedClass
        );  */
        //System.out.println("Predicted class is " + predictedClass);

        /* 
        String data = String.format("Predicted class for point (%f, %f, %f, %f, %f, %f, %f, %f, %f) is %d\n", 
            normalize(sample.getAngleToTrackAxis(), -Math.PI, Math.PI),
            normalize(sample.getTrackPosition(), -100, 100),
            normalize(sample.getTrackEdgeSensor11(), -200, 200),
            normalize(sample.getTrackEdgeSensor10(), -200, 200),
            normalize(sample.getTrackEdgeSensor9(), -200, 200),
            normalize(sample.getTrackEdgeSensor8(), -200, 200),
            normalize(sample.getTrackEdgeSensor7(), -200, 200),
            normalize(sample.getXSpeed(), -210, 210),
            normalize(sample.getYSpeed(), -210, 210),
            predictedClass
        );
        
        // Scrivi i dati nel file
        try (FileWriter fileWriter = new FileWriter("../src/prediction_log.csv", true)) {
            fileWriter.write(data);
            System.out.println("Predicted class is " + predictedClass);
        } catch (IOException e) {
            e.printStackTrace();
        } 
        */
        
        return predictedClass;
    }
    
}
