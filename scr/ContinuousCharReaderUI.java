package scr;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

/*
 * permette la lettura dei tasti premuti e rilasciati da tastiera durante la fase di creazione del
 * dataset e la creazione di valori dell’action da compiere di conseguenza ai tasti
 * premuti/rilasciati.
 */
public class ContinuousCharReaderUI extends JFrame {
    private JTextField inputField;

    // Booleani per indicare quale tasto è stato cliccato
    private boolean wPressed = false; // per accelerare
    private boolean aPressed = false; // sterzata a sinistra
    private boolean sPressed = false; // sterzata a destra
    private boolean dPressed = false; // per frenare

    // costanti di utilità
    public static final double DELTA_ACCEL = 0.065;
    public static final double DELTA_STEER = 0.04;
    public static final double DELTA_BRAKE = 0.1;

    // Valori di accelerazione, sterzata e freno da passare al driver
    private double accel = 0.0;
    private double steer = 0.0;
    private double brake = 0.0;

    public ContinuousCharReaderUI() {
        // Set up the frame
        setTitle("Continuous Character Reader");
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Initialize the text field for input
        inputField = new JTextField(20);
        add(inputField);

        // Add key listener to the text field
        inputField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("You pressed: " + e.getKeyChar());

                // ripulisce il text field
                inputField.setText("");

                // gestisco le keys premute
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W, KeyEvent.VK_UP -> wPressed = true;
                    case KeyEvent.VK_A, KeyEvent.VK_LEFT -> aPressed = true;
                    case KeyEvent.VK_S, KeyEvent.VK_DOWN -> sPressed = true;
                    case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> dPressed = true;
                    case KeyEvent.VK_Q -> System.exit(0);
                    default -> {
                        wPressed = false;
                        aPressed = false;
                        sPressed = false;
                        dPressed = false;
                    }
                }

                changeAction();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                System.out.println("You released: " + e.getKeyChar());

                // ripulisce il text field
                inputField.setText("");

                // gestisco le keys rilasciate
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W, KeyEvent.VK_UP -> wPressed = false;
                    case KeyEvent.VK_A, KeyEvent.VK_LEFT -> aPressed = false;
                    case KeyEvent.VK_S, KeyEvent.VK_DOWN -> sPressed = false;
                    case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> dPressed = false;
                    default -> {
                        wPressed = false;
                        aPressed = false;
                        sPressed = false;
                        dPressed = false;
                    }
                }

                changeAction();
            }
        });

        // Make the frame visible
        setVisible(true);
    }

    // calcola il comportamento da seguire quando viene premuto/rilasciato un tasto della tastiera
    private void changeAction() {

        if (wPressed) {
            // se viene premuto w imposto il freno a 0 e considero i casi in cui siano
            // premuti anche i tasi per sterzare (a, d)
            brake = 0.0;
            if (aPressed) { // wa
                // se premo a allora sterza verso sinistra gradualmente, massimo 1.0
                steer += DELTA_STEER;
                steer = steer > 0.5 ? 0.5 : steer;
                // deve anche diminuire l'accelerazione
                accel -= Math.abs(steer);
                accel = accel < 0.37 ? 0.37 : accel;
            } else if (dPressed) { // wd
                // se premo d allora sterza verso destra gradualmente, massimo -1.0
                steer -= DELTA_STEER;
                steer = steer < -0.5 ? -0.5 : steer;
                // deve anche diminuire l'accelerazione
                accel -= Math.abs(steer);
                accel = accel < 0.37 ? 0.37 : accel;
            } else { // solo w oppure e
                // se premo w allora accelera gradualmente, tetto massimo 1.0
                accel += DELTA_ACCEL;
                accel = accel > 1.0 ? 1.0 : accel;
                steer = 0.0;
            }
        } else if (sPressed) {
            // se premo s frena, massimo 1.0
            brake += DELTA_BRAKE;
            brake = brake > 1.0 ? 1.0 : brake;
            accel = 0.0;
            steer = 0.0;
        } else if (aPressed) {
            // se premo a allora sterza verso sinistra gradualmente, massimo 1.0
            steer += DELTA_STEER;
            steer = steer > 0.5 ? 0.5 : steer;
            accel = 0.0;
            brake = 0.0;
        } else if (dPressed) {
            // se premo d allora sterza verso destra gradualmente, massimo -1.0
            steer -= DELTA_STEER;
            steer = steer < -0.5 ? -0.5 : steer;
            accel = 0.0;
            brake = 0.0;
        } else {
            // non viene premuto nulla
            accel = 0.0;
            steer = 0.0;
            brake = 0.0;
        }
    }

    public double getAccel() {
        return accel;
    }

    public double getSteer() {
        return steer;
    }

    public double getBrake() {
        return brake;
    }

    public boolean wPressed() {
        return wPressed;
    }

    public boolean aPressed() {
        return aPressed;
    }

    public boolean sPressed() {
        return sPressed;
    }

    public boolean dPressed() {
        return dPressed;
    }

}
