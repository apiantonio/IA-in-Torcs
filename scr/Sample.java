package scr;

/**
 * Questa classe va cambiata sulla base del vostro vettore di feature.
 * Per ora, considero: 
 * un vettore di feature di 3 double (x,y,z) e una classe che è un intero.
 */

public class Sample {

    // features
    private final double angleToTrackAxis;
    private final double trackPosition;
    private final double trackEdgeSensor10; // rxSensor
    private final double trackEdgeSensors9; // ctr
    private final double trackEdgeSensors8; // sx
    private final double rpm;
    private final double gear;
    private final double steering;
    private final double accelerate; 
    private final double brake;
    private final double clutch;

    // classe
    private final int cls; 

    /*
    Chiamo questo costruttore quando ho la classe di appartenenza e sto costruendo il dataset. 
    In alternativa, quando voglio classificare un nuovo campione, uso l'altro costruttore.
    */
    public Sample(double angleToTrackAxis, double trackPosition, double trackEdgeSensor10, double trackEdgeSensors9,
            double trackEdgeSensors8, double rpm, double gear, double steering, double accelerate, double brake,
            double clutch, int cls) {
        this.angleToTrackAxis = angleToTrackAxis;
        this.trackPosition = trackPosition;
        this.trackEdgeSensor10 = trackEdgeSensor10;
        this.trackEdgeSensors9 = trackEdgeSensors9;
        this.trackEdgeSensors8 = trackEdgeSensors8;
        this.rpm = rpm;
        this.gear = gear;
        this.steering = steering;
        this.accelerate = accelerate;
        this.brake = brake;
        this.clutch = clutch;
        this.cls = cls;
    }

    public Sample(double angleToTrackAxis, double trackPosition, double trackEdgeSensor10, double trackEdgeSensors9,
            double trackEdgeSensors8, double rpm, double gear, double steering, double accelerate, double brake,
            double clutch) {
        this.angleToTrackAxis = angleToTrackAxis;
        this.trackPosition = trackPosition;
        this.trackEdgeSensor10 = trackEdgeSensor10;
        this.trackEdgeSensors9 = trackEdgeSensors9;
        this.trackEdgeSensors8 = trackEdgeSensors8;
        this.rpm = rpm;
        this.gear = gear;
        this.steering = steering;
        this.accelerate = accelerate;
        this.brake = brake;
        this.clutch = clutch;
        this.cls = -1;
    }

    /*
    Questo costruttore prende la stringa dal file csv e costruisce il Sample
    */
    public Sample(String line) {
        String[] parts = line.split(",");
        this.angleToTrackAxis = Double.parseDouble(parts[0].trim());
        this.trackPosition = Double.parseDouble(parts[1].trim());
        this.trackEdgeSensor10 = Double.parseDouble(parts[2].trim());
        this.trackEdgeSensors9 = Double.parseDouble(parts[3].trim());
        this.trackEdgeSensors8 = Double.parseDouble(parts[4].trim());
        this.rpm = Double.parseDouble(parts[5].trim());
        this.gear = Double.parseDouble(parts[6].trim());
        this.steering = Double.parseDouble(parts[7].trim());
        this.accelerate = Double.parseDouble(parts[8].trim());
        this.brake = Double.parseDouble(parts[9].trim());
        this.clutch = Double.parseDouble(parts[10].trim());
        this.cls = Integer.parseInt(parts[11].trim());
    }
    
    // calcola la distanza euclidea tra due samples
    public double distance(Sample other) {
        return Math.sqrt(
            Math.pow(this.angleToTrackAxis - other.angleToTrackAxis, 2) +
            Math.pow(this.trackPosition - other.trackPosition, 2) +
            Math.pow(this.trackEdgeSensor10 - other.trackEdgeSensor10, 2) +
            Math.pow(this.trackEdgeSensors9 - other.trackEdgeSensors9, 2) +
            Math.pow(this.trackEdgeSensors8 - other.trackEdgeSensors8, 2) +
            Math.pow(this.rpm - other.rpm, 2) +
            Math.pow(this.gear - other.gear, 2) +
            Math.pow(this.steering - other.steering, 2) +
            Math.pow(this.accelerate - other.accelerate, 2) +
            Math.pow(this.brake - other.brake, 2) +
            Math.pow(this.clutch - other.clutch, 2)
        );
    }

    // getters
    public double getAngleToTrackAxis() {
        return angleToTrackAxis;
    }

    public double getTrackPosition() {
        return trackPosition;
    }

    public double getTrackEdgeSensor10() {
        return trackEdgeSensor10;
    }

    public double getTrackEdgeSensors9() {
        return trackEdgeSensors9;
    }

    public double getTrackEdgeSensors8() {
        return trackEdgeSensors8;
    }

    public double getRpm() {
        return rpm;
    }

    public double getGear() {
        return gear;
    }

    public double getSteering() {
        return steering;
    }

    public double getAccelerate() {
        return accelerate;
    }

    public double getBrake() {
        return brake;
    }

    public double getClutch() {
        return clutch;
    }

    public int getCls() {
        return cls;
    }
    
}