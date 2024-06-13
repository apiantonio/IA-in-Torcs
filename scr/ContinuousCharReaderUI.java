package scr;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class ContinuousCharReaderUI extends JFrame {
    private JTextField inputField;
    private SimpleDriver driver; // driver da passare

    // Booleani per indicare quale tasto è stato cliccato 
    private boolean wPressed = false;
    private boolean aPressed = false;
    private boolean sPressed = false;
    private boolean dPressed = false;

    // Valori di accelerazione, sterzata e freno da passare al driver
    private double accel = 0.0;
    private double steer = 0.0;
    private double brake = 0.0;

    public ContinuousCharReaderUI(SimpleDriver driver) { // aggiunto un SimpleDriver come argomento
        this.driver = driver;

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
            public void keyTyped(KeyEvent e) {
                char ch = e.getKeyChar();
                System.out.println("You pressed: " + ch);

                // Clear the text field
                inputField.setText("");

                // Gestiscto le keys premute
                switch (ch) {
                    case 'w', 'W' -> wPressed = true;
                    case 's', 'S' -> sPressed = true;
                    case 'a', 'A' -> aPressed = true;
                    case 'd', 'D' -> dPressed = true;
                    case 'q', 'Q' -> System.exit(0);
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
                char ch = e.getKeyChar();
                System.out.println("You released: " + ch);

                // Clear the text field
                inputField.setText("");

                // Gestiscto le keys rilasciate
                switch (ch) {
                    case 'w', 'W' -> wPressed = false;
                    case 's', 'S' -> sPressed = false;
                    case 'a', 'A' -> aPressed = false;
                    case 'd', 'D' -> dPressed = false;
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
            // in simpledriver dovrà calcolare brake
        if (wPressed) {
            // se premo w allora accelera gradualmente, tetto massimo 1.0 
            accel += 0.3;
            accel = accel > 1.0 ? 1.0 : accel;
        } else if (sPressed) {
            // se premo s frena, massimo 1.0
            brake += 0.2;
            brake = brake > 1.0 ? 1.0 : brake;
        } else { 
            // se non sto premendo né w né s decelera e resetta il freno
            accel -= 0.3; // ?È utile? forse si può mettere a 0
            accel = accel < 0.0 ? 0.0 : accel;
            brake = 0.0;
        } 
        
        // la sterzata va da -1.0 (tutto a dx) a +1.0 (tutto a sx)
        if (aPressed) {
            // se premo a allora sterza verso sinistra gradualmente, massimo 1.0
            steer += 0.3;
            steer = steer > 1.0 ? 1.0 : steer;
        } else if (dPressed) {
            // se premo d allora sterza verso destra gradualemnte, massimo -1.0
            steer -= 0.3;
            steer = steer < -1.0 ? -1.0 : steer;
        } else {
            // se non sto sterzando allora porta la sterzata a 0
            steer = 0.0;
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
}
