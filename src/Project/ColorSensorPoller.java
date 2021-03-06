package Project;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

/**
 * ColorSensorPoller Class
 * This class polls the ColorSensor sensor for colorIDs.
 * 
 * @author DPM TEAM18
 * @version 2.0, 25 Nov 2015
 * 
 *
 */

public class ColorSensorPoller extends Thread{

	private SampleProvider colorValue;
	private float[] colorData;
	public float colorID;
	private Object lock = new Object();
	
	
	/**
	 * Creates an Object of type ColorSensorPoller
	 * The constructor populates a SampleProvider and an array with ColorID data
	 * @param colorsensor is an EV3ColorSensor
	 */
	public ColorSensorPoller(EV3ColorSensor colorsensor) {
		this.colorValue=colorsensor.getMode("ColorID");
		this.colorData = new float[colorValue.sampleSize()];
	}
	
	
	/**
	 * called when the thread is started 
	 * It acquires data from the sensor.
	 */
	public void run(){
		while(true){
			colorValue.fetchSample(colorData,0);
			synchronized(lock){
				this.colorID=(colorData[0]);
			}
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			} // Poor man's timed sampling
		}
	}
	
	/**
	 * returns the ColorID 
	 * @return float
	 */
	
	public float getColor(){
		float result= this.colorID;
		synchronized(lock){
			return result;
		}
	}
	

}
