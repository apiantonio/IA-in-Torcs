package scr;

public class Sample {

    public static final int N_FEATURES = 16; // numero di features scelte (pari alla dimensione)
    private final double[] features; // array delle features
    private final int cls; // classe del sample

    /*
    Chiamo questo costruttore quando ho la classe di appartenenza e sto costruendo il dataset. 
    In alternativa, quando voglio classificare un nuovo campione, uso l'altro costruttore.
    */
    public Sample(double angleToTrackAxis, double trackPosition, double trackEdgeSensor14, double trackEdgeSensor13,
                    double trackEdgeSensor12, double trackEdgeSensor11, double trackEdgeSensor10, 
                    double trackEdgeSensors9, double trackEdgeSensors8,  double trackEdgeSensors7, 
                    double trackEdgeSensors6,  double trackEdgeSensors5,  double trackEdgeSensors4, double rpm,
                    double xSpeed, double ySpeed, int cls) {

        this.features = new double[]{angleToTrackAxis, trackPosition, trackEdgeSensor14, trackEdgeSensor13, 
                    trackEdgeSensor12, trackEdgeSensor11, trackEdgeSensor10, trackEdgeSensors9, trackEdgeSensors8,
                    trackEdgeSensors7, trackEdgeSensors6, trackEdgeSensors5, trackEdgeSensors4, rpm, xSpeed, ySpeed};
        this.cls = cls;
    }

    public Sample(double angleToTrackAxis, double trackPosition, double trackEdgeSensor14, double trackEdgeSensor13,
                    double trackEdgeSensor12, double trackEdgeSensor11, double trackEdgeSensor10, 
                    double trackEdgeSensors9, double trackEdgeSensors8,  double trackEdgeSensors7, 
                    double trackEdgeSensors6,  double trackEdgeSensors5,  double trackEdgeSensors4, 
                    double rpm, double xSpeed, double ySpeed) {
                        
        this.features = new double[]{angleToTrackAxis, trackPosition, trackEdgeSensor14, trackEdgeSensor13, 
                    trackEdgeSensor12, trackEdgeSensor11, trackEdgeSensor10, trackEdgeSensors9, trackEdgeSensors8,
                    trackEdgeSensors7, trackEdgeSensors6, trackEdgeSensors5, trackEdgeSensors4, rpm, xSpeed, ySpeed};
        this.cls = -1;
    }

    /*
    Questo costruttore prende la stringa dal file csv e costruisce il Sample
    */
    public Sample(String line) {
        String[] parts = line.split(",");
        this.features = new double[N_FEATURES];
        for (int i = 0; i < N_FEATURES; i++) {
            this.features[i] = Double.parseDouble(parts[i].trim());
        }
        this.cls = Integer.parseInt(parts[N_FEATURES].trim());
    }

    // calcola la distanza euclidea tra due samples
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

    public double getTrackEdgeSensor14() {
        return features[2];
    }

    public double getTrackEdgeSensor13() {
        return features[3];
    }

    public double getTrackEdgeSensor12() {
        return features[4];
    }

    public double getTrackEdgeSensor11() {
        return features[5];
    }

    public double getTrackEdgeSensor10() {
        return features[6];
    }

    public double getTrackEdgeSensor9() {
        return features[7];
    }

    public double getTrackEdgeSensor8() {
        return features[8];
    }

    public double getTrackEdgeSensor7() {
        return features[9];
    }

    public double getTrackEdgeSensor6() {
        return features[10];
    }

    public double getTrackEdgeSensor5() {
        return features[11];
    }

    public double getTrackEdgeSensor4() {
        return features[12];
    }

    public double getRpm() {
        return features[13];
    }

    public double getXSpeed() {
        return features[14];
    }
    
    public double getYSpeed() {
        return features[15];
    }
}
