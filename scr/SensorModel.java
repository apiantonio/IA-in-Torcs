/*
Contiene i prototipi delle funzioni da 
implementare per rendere disponibili i valori 
della telemetria ricevuta (sensori)
L’implementazione delle funzioni la vedremo in 
MessageBasedSensorModel.

NB: La classe è già implementata e non richiede alcuna modifica

*/

package scr;

/**
 * Created by IntelliJ IDEA. User: Administrator Date: Mar 4, 2008 Time:
 * 12:18:47 PM
 */
public interface SensorModel {


	// Informazioni di base sull'auto e sul tracciato
 
	public double getSpeed(); // speedX (-inf, +inf) Velocità dell'auto lungo l'asse longitudinale dell'auto.

	public double getAngleToTrackAxis();

	public double[] getTrackEdgeSensors();

	public double[] getFocusSensors();// ML

	public double getTrackPosition();

	public int getGear(); // gear -1,1,2,3,4,5,6 Valore Marcia

	// informazioni di base sulle altre auto (utili solo per le gare con più auto)

	public double[] getOpponentSensors();

	public int getRacePosition(); // racePos {1,2,- - -,N} Posizione in gara rispetto alle altre auto

	// informazioni aggiuntive (utilizzare se necessario)

	public double getLateralSpeed(); // speedY (-inf, +inf) Velocità dell'auto lungo l'asse trasversale dell'auto

	public double getCurrentLapTime();

	public double getDamage();

	public double getDistanceFromStartLine();

	public double getDistanceRaced();

	public double getFuelLevel(); // fuel [0,+∞) (L) Livello attuale del carburante

	public double getLastLapTime();

	public double getRPM(); // rpm [0,+∞) (giri/min) Numero di giri al minuto del motore dell'auto

	public double[] getWheelSpinVelocity();

	public double getZSpeed();

	public double getZ();

	public String getMessage();

}
