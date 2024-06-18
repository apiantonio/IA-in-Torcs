/*
La classe utilizza i dati acquisiti dai sensori della
telemetria e restituisce le azioni da intraprendere
per la guida dell’auto, implementando i metodi
presenti in Controller.

    ATTENZIONE!!!
    Nell’ agente è obbligatorio implementare i
    seguenti metodi:
    • public Action control(SensorModel sensors)
    • public void shutdown()
    • public void reset()
*/


package scr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class SimpleDriver extends Controller {

    /* Costanti di cambio marcia */
    final int[] gearUp = {5000, 6000, 6000, 6500, 7000, 0};
    final int[] gearDown = {0, 2500, 3000, 3000, 3500, 3500};

    /* Constanti */
    final int stuckTime = 25;
    final float stuckAngle = (float) 0.523598775; // PI/6

    /* Costanti di accelerazione e di frenata */
    final float maxSpeedDist = 70;
    final float maxSpeed = 360; // originale 150
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

    private ContinuousCharReaderUI ccr; // char reader automatico da cui leggiamo il tasto premuto
    private long lastTimeWroteCSV = 0; // variabile per salvare quando è stata scritta l'ultima riga del CSV
    private long lastTimeDidAction = 0; // per l'ultima azione predetta da compiere
    private int deltaMillis = 300;
    private Action actionTodo = new Action(); // azione da intrapredere a seconda della classe predetta

    /* Nel costruttore chiamo il char reader */
    public SimpleDriver() {
        // Thread esterno che lancia il char reader per l'interazione da tastiera
    //   SwingUtilities.invokeLater(() -> { ccr = new ContinuousCharReaderUI(); });
    }

    // il metodo viene chiamato quando il client vuole inoltrare una richiesta di riavvio della
    // gara. Questa funzione potrebbe essere usata per chiudere i file aperti, salvare su disco,
    // salvare eventualmente lo stato della corsa ecc., laddove necessario.
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

    private float getSteer(SensorModel sensors) {
        /**
         * L'angolo di sterzata viene calcolato correggendo l'angolo effettivo della vettura rispetto all'asse della pista
         * [sensors.getAngle()] e regolando la posizione della vettura rispetto al centro della pista [sensors.getTrackPos()*0,5].
         */
        float targetAngle = (float) (sensors.getAngleToTrackAxis() - sensors.getTrackPosition() * 0.5);
        // ad alta velocità ridurre il comando di sterzata per evitare di perdere il controllo
        if (sensors.getSpeed() > steerSensitivityOffset) {
            return (float) (targetAngle / (steerLock * (sensors.getSpeed() - steerSensitivityOffset) * wheelSensitivityCoeff));
        } else {
            return (targetAngle) / steerLock;
        }
    }

    private float getAccel(SensorModel sensors) {
        // controlla se l'auto è fuori dalla carreggiata
        if (sensors.getTrackPosition() > -1 && sensors.getTrackPosition() < 1) {
            /*Capisco cosa sta succedendo davanti al veicolo, guardando poco a destra e poco a sinistra…
            */
            // lettura del sensore a +5 gradi rispetto all'asse dell'automobile
            float rxSensor = (float) sensors.getTrackEdgeSensors()[10];
            // lettura del sensore parallelo all'asse della vettura
            float sensorsensor = (float) sensors.getTrackEdgeSensors()[9];
            // lettura del sensore a -5 gradi rispetto all'asse dell'automobile
            float sxSensor = (float) sensors.getTrackEdgeSensors()[8];

            float targetSpeed;

            // Se la pista è rettilinea e abbastanza lontana da una curva, quindi va alla massima velocità
            if (sensorsensor > maxSpeedDist || (sensorsensor >= rxSensor && sensorsensor >= sxSensor)) {
                targetSpeed = maxSpeed;
            } else { // c'è una curva
                /*
                 * se la curva è molto pronunciata, modero la velocità di conseguenza.
                 */
                // In prossimità di una curva a destra
                if (rxSensor > sxSensor) {

                    // Calcolo dell'"angolo" di sterzata
                    float h = sensorsensor * sin5;
                    float b = rxSensor - sensorsensor * cos5;
                    float sinAngle = b * b / (h * h + b * b);

                    // Set della velocità in base alla curva
                    targetSpeed = maxSpeed * (sensorsensor * sinAngle / maxSpeedDist);
                } // In prossimità di una curva a sinistra
                else {
                    // Calcolo dell'"angolo" di sterzata
                    float h = sensorsensor * sin5;
                    float b = sxSensor - sensorsensor * cos5;
                    float sinAngle = b * b / (h * h + b * b);

                    // eSet della velocità in base alla curva
                    targetSpeed = maxSpeed * (sensorsensor * sinAngle / maxSpeedDist);
                }
            }

            /**
             * Il comando di accelerazione/frenata viene scalato in modo esponenziale rispetto alla differenza tra velocità target
             * e quella attuale
             */
            return (float) (2 / (1 + Math.exp(sensors.getSpeed() - targetSpeed)) - 1);
        }
        else // Quando si esce dalla carreggiata restituisce un comando di accelerazione moderata
        {
            return (float) 0.3;
        }
    }

    // metodo control da usare nella fase operativa
    @Override
    public Action control(SensorModel sensors) {
        
        long currentTime = System.currentTimeMillis();
        // esegue una nuova azione ogni deltaMillis
        if (currentTime - lastTimeDidAction < deltaMillis) {
            return actionTodo;
        } else {
            // prendo i dati dei sensori e li normalizzo per classificarli
            double[] trackEdgeSensors = sensors.getTrackEdgeSensors(); // array dei sensori

            double angleToTrackAxis = normalize(sensors.getAngleToTrackAxis(), -Math.PI, Math.PI);
            double trackPosition = normalize(sensors.getTrackPosition(), -100, 100);
            double trackEdgeSensor11 = normalize(trackEdgeSensors[11], -200, 200);
            double trackEdgeSensor10 = normalize(trackEdgeSensors[10], -200, 200); // rx -5
            double trackEdgeSensor9 = normalize(trackEdgeSensors[9], -200, 200); // ctr 0
            double trackEdgeSensor8 = normalize(trackEdgeSensors[8], -200, 200); // sx +5
            double trackEdgeSensor7 = normalize(trackEdgeSensors[7], -200, 200);
            double xSpeed = normalize(sensors.getSpeed(), -maxSpeed, maxSpeed);
            double ySpeed = normalize(sensors.getLateralSpeed(), -maxSpeed, maxSpeed);

            int predictedClass;

            // creo sample coi dati, lo classifico e a seconda della classe predetta eseguo l'azione corrispondente
            Sample testSample = new Sample(
                angleToTrackAxis,
                trackPosition,
                trackEdgeSensor11,
                trackEdgeSensor10,
                trackEdgeSensor9,
                trackEdgeSensor8,
                trackEdgeSensor7,
                xSpeed,
                ySpeed
            );

            // predici classe del sample
            predictedClass = Classificatore.classifica(testSample);

            // i valori dell'ultima azione eseguita
            double accel = actionTodo.accelerate;
            double steer = actionTodo.steering;
            double brake = actionTodo.brake;

            clutch = clutching(sensors, clutch); // calcola la frizione
            int gear = getGear(sensors); // calcola la marcia

            // valori dell'azione a seconda della classe predetta
            switch (predictedClass) {
                // nessun tasto premuto
                case 0 -> {
                    return new Action(); // azione nulla
                }
                // premuto w
                case 1 -> {
                    accel += ContinuousCharReaderUI.DELTA_ACCEL;
                    accel = accel > 1.0 ? 1.0 : accel;
                    steer = 0.0;
                    brake = 0.0;
                }
                // premo a
                case 2 -> {
                    accel = 0.0;
                    steer += ContinuousCharReaderUI.DELTA_STEER;
                    steer = steer > 1.0 ? 1.0 : steer;
                    brake = 0.0;
                }
                // premo s
                case 3 -> {
                    accel = 0.0;
                    steer = 0.0;
                    brake += ContinuousCharReaderUI.DELTA_BRAKE;
                    brake = brake > 1.0 ? 1.0 : brake;
                    // se si vuole frenare allora applico l'ABS al freno
                    brake = filterABS(sensors, brake);
                }
                // premo d
                case 4 -> {
                    accel = 0.0;
                    steer -= ContinuousCharReaderUI.DELTA_STEER;
                    steer = steer < -1.0 ? -1.0 : steer;
                    //deve anche diminuire l'accelerazione
                    brake = 0.0;
                }
                // premo w e a
                case 5 -> {
                    accel -= Math.abs(steer);
                    accel = accel < 0.37 ? 0.37 : accel;
                    steer += ContinuousCharReaderUI.DELTA_STEER;
                    steer = steer > 1.0 ? 1.0 : steer;
                    brake = 0.0;
                }
                // premo w e d
                case 6 -> {
                    accel -= Math.abs(steer);
                    accel = accel < 0.37 ? 0.37 : accel;
                    steer -= ContinuousCharReaderUI.DELTA_STEER;
                    steer = steer < -1.0 ? -1.0 : steer;
                    brake = 0.0;
                }
                // premo e e a
                case 7 -> {
                    gear = -1;
                    accel -= Math.abs(steer);
                    accel = accel < 0.37 ? 0.37 : accel;
                    steer += ContinuousCharReaderUI.DELTA_STEER;
                    steer = steer > 1.0 ? 1.0 : steer;
                    brake = 0.0;
                }
                // premo e e d
                case 8 -> {
                    gear = -1;
                    accel -= Math.abs(steer);
                    accel = accel < 0.37 ? 0.37 : accel;
                    steer -= ContinuousCharReaderUI.DELTA_STEER;
                    steer = steer < -1.0 ? -1.0 : steer;
                    brake = 0.0;
                }
                // premo e
                case 9 -> {
                    gear = -1;
                    accel += ContinuousCharReaderUI.DELTA_ACCEL;
                    accel = accel > 1.0 ? 1.0 : accel;
                    steer = 0.0;
                    brake = 0.0;
                }
                default -> throw new AssertionError();
            }

            // se non si sta andando in retromarcia ma la velocità è negativa allora si vuole smettere
            // di andare in retro dunque prima freno
            if(predictedClass != 7 && predictedClass != 8 && predictedClass != 9 && sensors.getSpeed() < 0) {
                brake = 1.0;
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

    // metodo control da usare per creare il dataset
    // sensors rappresenta lo stato attuale del gioco come percepito dal driver; il metodo
    // restituisce l'azione da intraprendere
    /* @Override
    public Action control(SensorModel sensors) {
        // azione da intraprendere
        Action action = new Action();

        boolean wPressed = ccr.wPressed();
        boolean aPressed = ccr.aPressed();
        boolean sPressed = ccr.sPressed();
        boolean dPressed = ccr.dPressed();
        boolean ePressed = ccr.ePressed();

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
        } else if (ePressed) {
            if(aPressed){
                cls = 7; // ea
            } else if(dPressed) {
                cls = 8; // ed
            } else {
                cls = 9; // e
            }
        } else {
            cls = 0; // non sta premendo nulla
        }

        double accel = ccr.getAccel(); // accelerazione calcolata in base al tasto premuto
        double brake = ccr.getBrake(); // valore del freno
        int gear = getGear(sensors); // calcola la marcia

        // voglio andare in retro con il tasto apposito
        if(ePressed) {
            gear = -1;
            brake = 0.0;
        } else if (sensors.getSpeed() < 0) {
            // se non si sta andando in retromarcia ma la velocità è negativa allora si vuole smettere
            // di andare in retro dunque prima freno
            brake = 1.0;
        } else if (brake != 0) { // se brake != 0 allora si sta premendo 's' per frenare
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

        printToCSV(sensors, cls);

        return action;
    } */

    // // Controlla se l'auto è attualmente bloccata
        // /**
        //  * Se l'auto ha un angolo, rispetto alla traccia, superiore a 30° incrementa "stuck" che è una variabile che indica per
        //  * quanti cicli l'auto è in condizione di difficoltà. Quando l'angolo si riduce, "stuck" viene riportata a 0 per indicare
        //  * che l'auto è uscita dalla situaizone di difficoltà
        // **/
        // if (Math.abs(sensors.getAngleToTrackAxis()) > stuckAngle) {
        //      // update stuck counter
        //      stuck++;
        // } else {
        //      // if not stuck reset stuck counter
        //      stuck = 0;
        // }

        // // Applicare la polizza di recupero o meno in base al tempo trascorso
        // /**
        //  * Se "stuck" è superiore a 25 (stuckTime) allora procedi a entrare in situaizone di RECOVERY per far fronte alla
        //  * situazione di difficoltà
        // **/
        // if (stuck > stuckTime) { //Auto Bloccata
        //     /**
        //      * Impostare la marcia e il comando di sterzata supponendo che l'auto stia puntando in una direzione al di fuori di
        //      * pista
        //      **/

        //     // Per portare la macchina parallela all'asse TrackPos
        //     float steer = (float) (-sensors.getAngleToTrackAxis() / steerLock);
        //     int gear = -1; // Retromarcia

        //     // Se l'auto è orientata nella direzione corretta invertire la marcia e sterzare
        //     if (sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0) {
        //         gear = 1;
        //         steer = -steer;
        //     }
        //     clutch = clutching(sensors, clutch);
        //     // Costruire una variabile CarControl e restituirla
        //     Action action = new Action();
        //     action.gear = gear;
        //     action.steering = steer;
        //     action.accelerate = 1.0;
        //     action.brake = 0;
        //     action.clutch = clutch;

        //     return action;
        // }
        // else //Auto non Bloccata
        // {
        //     // Calcolo del comando di accelerazione/frenata
        //     float accel_and_brake = getAccel(sensors);

        //     // Calcolare marcia da utilizzare
        //     int gear = getGear(sensors);

        //     // Calcolo angolo di sterzata
        //     float steer = getSteer(sensors);

        //     // Normalizzare lo sterzo
        //     /* Qui stiamo prevenendo eventuali errori legati ad  una errata normalizzazione del range -1,1 nelle
        //       funzioni di aggiornamento di tali valori*/
        //     if (steer < -1) {
        //         steer = -1;
        //     }
        //     if (steer > 1) {
        //         steer = 1;
        //     }

        //     // Impostare accelerazione e frenata dal comando congiunto accelerazione/freno
        //     /*  Gestisco eventuali situazioni di frenata (accelerazione negativa).
        //         La frenata non produce il bloccaggio delle ruote, ma funziona in modo  simile a quello che accade nelle nostre
        //         auto, con il meccanismo dell’ABS */
        //     float accel, brake;
        //     if (accel_and_brake > 0) {
        //         accel = accel_and_brake;
        //         brake = 0;
        //     } else {
        //         accel = 0;

        //         // Applicare l'ABS al freno
        //         brake = filterABS(sensors, -accel_and_brake);
        //     }
        //     clutch = clutching(sensors, clutch);

        //     // Costruire una variabile CarControl e restituirla
        //     Action action = new Action();
        //     action.gear = gear;
        //     action.steering = steer;
        //     action.accelerate = accel;
        //     action.brake = brake;
        //     action.clutch = clutch;

        //     return action;
        // }
    // }

    // stampa una nuova riga nel dataset
    private void printToCSV(SensorModel sensors, int cls) {
        // Ottieni il tempo corrente
        long currentTime = System.currentTimeMillis();
        String datasetPath = "../src/dataset.csv"; // path del file CSV in cui salviamo i dati

        // Scrivo una riga sul file CSV delta millisecondi
        if (currentTime - lastTimeWroteCSV >= deltaMillis) {

            // Try-with-resources per la gestione del file
            try (PrintWriter csvWriter = new PrintWriter(new FileWriter(datasetPath, true))) {
                // Controllo se il file esiste già
                File file = new File(datasetPath);
                boolean fileExists = file.exists();

                // Se il file non esisteva o era vuoto scrivo l'intestazione
                if (!fileExists || file.length() == 0) {
                    csvWriter.println("angleToTrackAxis,trackPosition,11Sensor,rxSensor,ctrSensor,sxSensor,7Sensor,xSpeed,ySpeed,class");
                }

                double[] trackEdgeSensors = sensors.getTrackEdgeSensors();

                // Scrivi i valori normalizzati dei sensori nel file CSV
                csvWriter.printf(Locale.US,"%f,%f,%f,%f,%f,%f,%f,%f,%f,%d\n",
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

                lastTimeWroteCSV = currentTime; // aggiorno la variabile
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // esegue una normalizzazione di un valore x ad un range compreso tra -1 e 1,
    // è necessario passare i parametri max che rappresenta il valore massimo che può assumere x
    // e min che rappresenta il valore minimo che può assumere x
    private double normalize(double x,  double min, double max) {
        return 2 * ((x - min) / (max - min)) - 1;
    }

          //float                               //float
    private double filterABS(SensorModel sensors, double brake) {
        // Converte la velocità in m/s
        float speed = (float) (sensors.getSpeed() / 3.6);

        // Quando la velocità è inferiore alla velocità minima per l'abs non interviene in caso di frenata
        if (speed < absMinSpeed) {
            return brake;
        }

        // Calcola la velocità delle ruote in m/s
        float slip = 0.0f;
        for (int i = 0; i < 4; i++) {
            slip += sensors.getWheelSpinVelocity()[i] * wheelRadius[i];
        }

        // Lo slittamento è la differenza tra la velocità effettiva dell'auto e la velocità media delle ruote
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

 // float                              // float
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

                // Applicare un'uscita più forte della frizione quando la marcia è una e la corsa è appena iniziata.
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
     * 19 angoli desiderati (rispetto all'asse della vettura) per ciascuno dei 19 telemetri del sensore «Track»
     */
    // È possibile sovrascrivere con una propria implementazione la funzione
    @Override
    public float[] initAngles() {

        float[] angles = new float[19];

        /*
         * set angles as
         * {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90}
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
