package scr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import static scr.Classificatore.*;
import static scr.ContinuousCharReaderUI.*;
/**
 * La classe utilizza i dati acquisiti dai sensori della telemetria e restituisce le azioni da
 * intraprendere per la guida dell’auto, implementando i metodi presenti in Controller.
 */

public class SimpleDriver extends Controller {

    /* Costanti di cambio marcia */
    final int[] gearUp = {5000, 6000, 6000, 6500, 7000, 0};
    final int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};

    /* Constanti */
    final int stuckTime = 25;
    final float stuckAngle = (float) 0.523598775; // PI/6

    /* Costanti di accelerazione e di frenata */
    final float maxSpeedDist = 70;
    final float maxSpeed = 210; // originale 150
    final float sin5 = (float) 0.08716;
    final float cos5 = (float) 0.99619;

    /* Costanti di sterzata */
    final float steerLock = (float) 0.785398;
    final float steerSensitivityOffset = (float) 80.0;
    final float wheelSensitivityCoeff = 1;

    /* Costanti del filtro ABS */
    final float wheelRadius[] = {(float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276};
    final float absSlip = (float) 2.0;
    final float absRange = (float) 3.0;
    final float absMinSpeed = (float) 3.0;

    /* Costanti da stringere */
    final float clutchMax = (float) 0.5;
    final float clutchDelta = (float) 0.05;
    final float clutchRange = (float) 0.82;
    final float clutchDeltaTime = (float) 0.02;
    final float clutchDeltaRaced = 10;
    final float clutchDec = (float) 0.01;
    final float clutchMaxModifier = (float) 1.3;
    final float clutchMaxTime = (float) 1.5;

    private int stuck = 0;

    // current clutch
    private double clutch = 0;

    // utilità
    private static final int DELTA_MILLIS = 300; // tempo tra una lettura (o scrittura) e l'altra dal CSV
    private long lastTimeWroteCSV = 0; // variabile per salvare quando è stata scritta l'ultima riga del CSV
    private long lastTimeDidAction = 0; // per l'ultima azione predetta da compiere
    private Action actionTodo = new Action(); // azione da intrapredere a seconda della classe predetta

    // char reader automatico da cui leggiamo il tasto premuto/rilasciato da tastiera
    private ContinuousCharReaderUI ccr;

    // Nel costruttore chiamo il char reader con un thread apposito
    public SimpleDriver() {
        // Thread esterno che lancia il char reader per l'interazione da tastiera
    //    SwingUtilities.invokeLater(() -> { ccr = new ContinuousCharReaderUI(); }); // commentare questa riga durante fase operativa
    }

    // il metodo viene chiamato quando il client vuole inoltrare una richiesta di riavvio della
    // gara.
    @Override
    public void reset() {
        System.out.println("Restarting the race!");
    }

    // il metodo è utilizzato al termine della gara
    @Override
    public void shutdown() {
        System.out.println("Bye bye!");
    }

    private int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        // Se la marcia è 0 (N) o -1 (R) restituisce semplicemente 1
        if (gear < 1) {
            return 1;
        }

        // Se il valore di RPM dell'auto è maggiore di quello suggerito
        // sale di marcia rispetto a quella attuale
        if (gear < 6 && rpm >= gearUp[gear - 1]) {
            return gear + 1;
        } else // Se il valore di RPM dell'auto è inferiore a quello suggerito
        // scala la marcia rispetto a quella attuale
        if (gear > 1 && rpm <= gearDown[gear - 1]) {
            return gear - 1;
        } else // Altrimenti mantenere l'attuale
        {
            return gear;
        }
    }

    /**
     * Metodo control da usare nella fase operativa, ogni delta millisecodi classifica i valori dei
     * sensori ed esegue un'azione di conseguenza
     **/
    @Override
    public Action control(SensorModel sensors) {

        double angleToTrackAxis = sensors.getAngleToTrackAxis();
        clutch = clutching(sensors, clutch);

        //
        // Se l'auto ha un angolo, rispetto alla traccia, superiore a 30° incrementa "stuck" che è
        // una variabile che indica per
        // quanti cicli l'auto è in condizione di difficoltà. Quando l'angolo si riduce, "stuck"
        // viene riportata a 0 per indicare
        // che l'auto è uscita dalla situaizone di difficoltà
        //
        if (Math.abs(angleToTrackAxis) > stuckAngle) {
            // update stuck counter
            stuck++;
        } else {
            // if not stuck reset stuck counter
            stuck = 0;
        }

        // Auto Bloccata //

        // Applicare la polizza di recupero o meno in base al tempo trascorso
        //
        // questa codice per "sbloccare" l'auto è applicato solo nel caso in cui stuck > 25
        // in altre situazioni anche se l'auto è bloccata viene applicato un comportamento coerente
        // col dataset
        // Se "stuck" è superiore a 25 (stuckTime) allora procedi a entrare in situazione di
        // RECOVERY per far fronte alla
        // situazione di difficoltà
        //
        if (stuck > stuckTime) { // Auto Bloccata
            //
            // Impostare la marcia e il comando di sterzata supponendo che l'auto stia puntando in
            // una direzione al di fuori di
            // pista
            //

            // Per portare la macchina parallela all'asse TrackPos
            double steer = (double) (-angleToTrackAxis / steerLock);
            int gear = -1; // Retromarcia

            // Se l'auto è orientata nella direzione corretta invertire la marcia e sterzare
            if (angleToTrackAxis * sensors.getTrackPosition() > 0) {
                gear = 1;
                steer = -steer;
            }
            
           double brake = 0.0;
           // quando mette la retromarcia primafrena
           if (gear == -1 && sensors.getSpeed() > 0) {
               brake = 1.0;
           } else if (gear != -1 && sensors.getSpeed() < 0) {
               // se non si sta andando in retromarcia ma la velocità è negativa allora si vuole
               // smettere di andare in retro dunque prima freno
               brake = 1.0;
           } 

            actionTodo.gear = gear;
            actionTodo.steering = steer;
            actionTodo.accelerate = 0.5;
            actionTodo.brake = brake;
            actionTodo.clutch = clutch;

            return actionTodo;
        }

        // Auto non Bloccata //

        long currentTime = System.currentTimeMillis();
        // esegue una nuova azione ogni DELTA_MILLIS
        if (currentTime - lastTimeDidAction < DELTA_MILLIS) {
            return actionTodo;
        } else {

            // prendo i dati dei sensori e li normalizzo per classificarli
            double[] trackEdgeSensors = sensors.getTrackEdgeSensors(); // array dei sensori
            // features
            angleToTrackAxis = normalize(angleToTrackAxis, -Math.PI, Math.PI);
            double trackPosition = normalize(sensors.getTrackPosition(), -100, 100);
            double trackEdgeSensor11 = normalize(trackEdgeSensors[11], -200, 200);
            double trackEdgeSensor10 = normalize(trackEdgeSensors[10], -200, 200); // rx -5
            double trackEdgeSensor9 = normalize(trackEdgeSensors[9], -200, 200); // ctr 0
            double trackEdgeSensor8 = normalize(trackEdgeSensors[8], -200, 200); // sx +5
            double trackEdgeSensor7 = normalize(trackEdgeSensors[7], -200, 200);
            double xSpeed = normalize(sensors.getSpeed(), -maxSpeed, maxSpeed);
            double ySpeed = normalize(sensors.getLateralSpeed(), -maxSpeed, maxSpeed);

            // creo sample coi dati, lo classifico e a seconda della classe predetta eseguo l'azione
            // corrispondente
            double[] normalizedFeatures = {angleToTrackAxis, trackPosition, trackEdgeSensor11, 
                trackEdgeSensor10, trackEdgeSensor9, trackEdgeSensor8, trackEdgeSensor7, xSpeed, ySpeed};
            Sample newSample = new Sample(normalizedFeatures);

            // predice classe del sample
            int predictedClass = classifica(newSample);

            // i valori dell'ultima azione eseguita
            double accel = actionTodo.accelerate;
            double steer = actionTodo.steering;
            double brake = actionTodo.brake;

            int gear = getGear(sensors); // calcola la marcia

            // valori dell'azione a seconda della classe predetta
            switch (predictedClass) {
                // nessun tasto premuto
                case 0 -> { 
                    // azione nulla
                    accel = 0.0;
                    steer = 0.0;
                    brake = 0.0; 
                }
                // premuto w
                case 1 -> {
                    accel += 1.05 * DELTA_ACCEL;
                    accel = accel > 1.0 ? 1.0 : accel;
                    steer = 0.0;
                    brake = 0.0;
                }
                // premo a
                case 2 -> {
                    accel = 0.0;
                    steer += 1.5 * DELTA_STEER;
                    steer = steer > 0.5 ? 0.5 : steer;
                    brake = 0.0;
                }
                // premo s
                case 3 -> {
                    accel = 0.0;
                    steer = 0.0;
                    brake += 1.5 * DELTA_BRAKE;
                    brake = brake > 1.0 ? 1.0 : brake;
                    // se si vuole frenare allora applico l'ABS al freno
                //  brake = filterABS(sensors, brake);
                }
                // premo d
                case 4 -> {
                    accel = 0.0;
                    steer -= 1.5 * DELTA_STEER;
                    steer = steer < -0.5 ? -0.5 : steer;
                    // deve anche diminuire l'accelerazione
                    brake = 0.0;
                }
                // premo w e a
                case 5 -> {
                    accel -= Math.abs(steer);
                    accel = accel < 0.25 ? 0.25 : accel;
                    steer += 1.5 * DELTA_STEER;
                    steer = steer > 0.5 ? 0.5 : steer;
                    brake = 0.0;
                }
                // premo w e d
                case 6 -> {
                    accel -= Math.abs(steer);
                    accel = accel < 0.25 ? 0.25 : accel;
                    steer -= 1.5 * DELTA_STEER;
                    steer = steer < -0.5 ? -0.5 : steer;
                    brake = 0.0;
                }
                default -> throw new AssertionError();
            }

            // se sei prossimo al bordo destro della carreggiata, sterza a sinistra;
            // se sei prossimo al bordo sinistro della carreggiata, sterza a destra 
            // per riposizionarti
            if (sensors.getTrackPosition() < -0.85) {
                accel -= Math.abs(steer);
                accel = accel < 0.25 ? 0.25 : accel;
                steer += 0.8 * DELTA_STEER;
                steer = steer > 0.5 ? 0.5 : steer;
            } else if (sensors.getTrackPosition() > 0.85) {
                accel -= Math.abs(steer);
                accel = accel < 0.25 ? 0.25 : accel;
                steer -= 0.8 * DELTA_STEER;
                steer = steer < -0.5 ? -0.5 : steer;
            }

            actionTodo.accelerate = accel;
            actionTodo.steering = steer;
            actionTodo.brake = brake;
            actionTodo.clutch = clutch;
            actionTodo.gear = gear;

            lastTimeDidAction = currentTime; // aggiorno la variabile

            return actionTodo;
        }
    }

    /**
     * metodo control da usare per creare il dataset sensors rappresenta lo stato attuale del gioco
     * come percepito dal driver, il metodo restituisce l'azione da intraprendere a secnoda del
     * tasto premuto sulla tastiera
     */
    /*@Override
    public Action control(SensorModel sensors) {
        // azione da intraprendere
        Action action = new Action();

        boolean wPressed = ccr.wPressed();
        boolean aPressed = ccr.aPressed();
        boolean sPressed = ccr.sPressed();
        boolean dPressed = ccr.dPressed();

        // determino la ground truth
        int cls;
        if (wPressed) {
            if (aPressed) {
                cls = 5; // wa
            } else if (dPressed) {
                cls = 6; // wd
            } else {
                cls = 1; // w
        }
        } else if (sPressed) {
            cls = 3; // s
        } else if (aPressed) {
            cls = 2; // a
        } else if (dPressed) {
            cls = 4; // d
        } else {
            cls = 0; // non sta premendo nulla
        }

        double accel = ccr.getAccel(); // accelerazione calcolata in base al tasto premuto
        double brake = ccr.getBrake(); // valore del freno
        int gear = getGear(sensors); // calcola la marcia

        if (brake != 0) { // se brake != 0 allora si sta premendo 's' per frenare
            // se si vuole frenare allora applico l'ABS
            accel = 0;
            // Applicare l'ABS al freno
            brake = filterABS(sensors, brake);
        }

        action.accelerate = accel;
        action.brake = brake;
        action.steering = ccr.getSteer(); // sterzata calcolata in base al tasto premuto
        action.gear = gear;
        action.clutch = clutching(sensors, clutch); // calcola la frizione

        // Scrivo una riga sul file CSV ogni delta millisecondi
        long currentTime = System.currentTimeMillis(); // tempo corrente
        if (currentTime - lastTimeWroteCSV >= DELTA_MILLIS) {
             // stampo nel dataset
             printToCSV(sensors, cls);
             lastTimeWroteCSV = currentTime; // aggiorno la variabile
        }

        return action;
    } */

    
    /**
     * Stampa una nuova riga nel dataset
     * 
     * @param sensors sono i sensori da cui prendere le features da scrivere
     * @param cls è la classe ground truth
     */
    private void printToCSV(SensorModel sensors, int cls) {

        String datasetPath = "../src/dataset.csv"; // path del file CSV in cui salviamo i dati

        // Try-with-resources per la gestione del file
        try (PrintWriter csvWriter = new PrintWriter(new FileWriter(datasetPath, true))) {
            // Controllo se il file esiste già
            File file = new File(datasetPath);

            // Se il file non esisteva o era vuoto scrivo l'intestazione
            if (!file.exists() || file.length() == 0) {
                csvWriter.println("angleToTrackAxis,trackPosition,11Sensor,rxSensor,ctrSensor,sxSensor,7Sensor,xSpeed,ySpeed,class");
            }

            double[] trackEdgeSensors = sensors.getTrackEdgeSensors();
            // Scrivi i valori normalizzati dei sensori nel file CSV
            csvWriter.printf(Locale.US, "%f,%f,%f,%f,%f,%f,%f,%f,%f,%d\n",
                normalize(sensors.getAngleToTrackAxis(), -Math.PI, Math.PI),
                normalize(sensors.getTrackPosition(), -100, 100),
                normalize(trackEdgeSensors[11], -200, 200), // -10
                normalize(trackEdgeSensors[10], -200, 200), // rx -5
                normalize(trackEdgeSensors[9], -200, 200), // ctr 0
                normalize(trackEdgeSensors[8], -200, 200), // sx +5
                normalize(trackEdgeSensors[7], -200, 200), // 10
                normalize(sensors.getSpeed(), -maxSpeed, maxSpeed), // velocità lungo l'asse x
                normalize(sensors.getLateralSpeed(), -maxSpeed, maxSpeed), // veloxità lungo l'asse y
                cls // classe
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

            // float                              //float
    private double filterABS(SensorModel sensors, double brake) {
        // Converte la velocità in m/s
        float speed = (float) (sensors.getSpeed() / 3.6);

        // Quando la velocità è inferiore alla velocità minima per l'abs non interviene in caso di
        // frenata
        if (speed < absMinSpeed) {
            return brake;
        }

        // Calcola la velocità delle ruote in m/s
        float slip = 0.0f;
        for (int i = 0; i < 4; i++) {
            slip += sensors.getWheelSpinVelocity()[i] * wheelRadius[i];
        }

        // Lo slittamento è la differenza tra la velocità effettiva dell'auto e la velocità media
        // delle ruote
        slip = speed - slip / 4.0f;

        // Quando lo slittamento è troppo elevato, si applica l'ABS
        if (slip > absSlip) {
            brake = brake - (slip - absSlip) / absRange;
        }

        // Controlla che il freno non sia negativo, altrimenti lo imposta a zero
        if (brake < 0) {
            return 0;
        } else {
            return brake;
        }
    }

    // float // float
    double clutching(SensorModel sensors, double clutch) {

        float maxClutch = clutchMax;

        // Controlla se la situazione attuale è l'inizio della gara
        if (sensors.getCurrentLapTime() < clutchDeltaTime && getStage() == Stage.RACE
                && sensors.getDistanceRaced() < clutchDeltaRaced) {
            clutch = maxClutch;
        }

        // Regolare il valore attuale della frizione
        if (clutch > 0) {
            double delta = clutchDelta;
            if (sensors.getGear() < 2) {

                // Applicare un'uscita più forte della frizione quando la marcia è una e la corsa è
                // appena iniziata.
                delta /= 2;
                maxClutch *= clutchMaxModifier;
                if (sensors.getCurrentLapTime() < clutchMaxTime) {
                    clutch = maxClutch;
                }
            }

            // Controllare che la frizione non sia più grande dei valori massimi
            clutch = Math.min(maxClutch, clutch);

            // Se la frizione non è al massimo valore, diminuisce abbastanza rapidamente
            if (clutch != maxClutch) {
                clutch -= delta;
                clutch = Math.max((float) 0.0, clutch);
            } // Se la frizione è al valore massimo, diminuirla molto lentamente.
            else {
                clutch -= clutchDec;
            }
        }
        return clutch;
    }

    /*
     * il metodo viene richiamato prima dell'inizio della gara e può essere utilizzato per definire
     * una configurazione personalizzata dei sensori di pista: il metodo restituisce un vettore dei
     * 19 angoli desiderati (rispetto all'asse della vettura) per ciascuno dei 19 telemetri del
     * sensore «Track»
     */
    @Override
    public float[] initAngles() {

        float[] angles = new float[19];

        /*
         * set angles as {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90}
         */
        for (int i = 0; i < 5; i++) {
            angles[i] = -90 + i * 15;
            angles[18 - i] = 90 - i * 15;
        }

        for (int i = 5; i < 9; i++) {
            angles[i] = -20 + (i - 5) * 5;
            angles[18 - i] = 20 - (i - 5) * 5;
        }
        angles[9] = 0;
        return angles;
    }

}