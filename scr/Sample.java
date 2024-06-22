package scr;

/**
 * Rappresenta i punti nello spazio degli stati, ogni punto è costituito dalle features scelte e
 * dalla classe associata Inoltre fornisce un metodo per calcolare la distanza euclidea tra due
 * punti.
 */
public class Sample {

    double[] features; // array delle features
    int cls; // classe del sample

    /**
     * Chiamo questo costruttore quando ho la classe di appartenenza e sto costruendo il dataset. In
     * alternativa, quando voglio classificare un nuovo campione, uso l'altro costruttore.
     * 
     * @param features array di valori delle features del sample
     * @param cls classe del sample (ground truth)
     */
    public Sample(double[] features, int cls) {
        this.features = features;
        this.cls = cls;
    }

    /**
     * Costruttore per inizializzare l'esempio con un determinato set di funzionalità senza uns
     * label di classe. Viene utilizzato quando si classifica un nuovo campione.
     * 
     * @param features array di valori delle features del sample
     */
    public Sample(double[] features) {
        this.features = features;
        this.cls = -1; // Default class value
    }

    /**
     * Questo costruttore prende la stringa dal file csv e costruisce il Sample
     **/
    public Sample(String line) {
        String[] parts = line.split(",");
        int n = parts.length;
        features = new double[n - 1];
        for (int i = 0; i < n - 1; i++) {
            this.features[i] = Double.parseDouble(parts[i].trim());
        }
        this.cls = Integer.parseInt(parts[n - 1].trim());
    }

    /**
     * Calcola la distanza euclidea da un altro sample
     * 
     * @param other sample di cui calcolare la distanza
     * @return la distanza dal sample passato
     */
    public double distance(Sample other) {
        double sum = 0;
        for (int i = 0; i < features.length; i++) {
            sum += Math.pow(this.features[i] - other.features[i], 2);
        }
        return Math.sqrt(sum);
    }

    // getters

    public double[] getFeatures() {
        return features;
    }

    public int getCls() {
        return cls;
    }

    public double getAngleToTrackAxis() {
        return features[0];
    }

    public double getTrackPosition() {
        return features[1];
    }

    public double getTrackEdgeSensor11() {
        return features[2];
    }

    public double getTrackEdgeSensor10() {
        return features[3];
    }

    public double getTrackEdgeSensor9() {
        return features[4];
    }

    public double getTrackEdgeSensor8() {
        return features[5];
    }

    public double getTrackEdgeSensor7() {
        return features[6];
    }

    public double getXSpeed() {
        return features[7];
    }

    public double getYSpeed() {
        return features[8];
    }
}
