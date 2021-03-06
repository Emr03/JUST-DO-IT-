package Project;

import java.io.IOException;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import wifi.StartCorner;
import wifi.Transmission;
import wifi.WifiConnection;

/**
 * 
 * Robot class initializes motors sensors and threads as well as other classes included in the project
 * also contains the main method which handles transition between the robot's activities 
 * @author DPM TEAM18
 * @version 2.0, 25 Nov 2015
 */
public class Robot {

	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

	public static  EV3UltrasonicSensor usSensor_left; 
	public static final EV3UltrasonicSensor usSensor_right = new EV3UltrasonicSensor(SensorPort.S3);
	public static final EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S1);
	public static final EV3ColorSensor lightSensor = new EV3ColorSensor(SensorPort.S4); 

	public static UltrasonicPoller usPoller_left;
	public static final UltrasonicPoller usPoller_right=new UltrasonicPoller(usSensor_right.getDistanceMode());
	public static final ColorSensorPoller colorPoller = new ColorSensorPoller(colorSensor); 


	public static final Odometer odometer = new Odometer();
	public static final Navigation navigator = new Navigation();
	public static final UsLocalization localizer = new UsLocalization();
	public static final PathPlanner planner = new PathPlanner();
	//public static final OdometryCorrection odoCorrector = new OdometryCorrection(); 
	public static final SongPlayer justDoIt = new SongPlayer(); 
	
	public static final double left_radius = 2.05;
	public static final double right_radius = 2.05;
	public static final double wheel_base = 15.24;
	public static final double tile = 30.48; 
	public static final int ROTATE_SPEED = 200;           
	public static final int FORWARD_SPEED = 200;
	
	public static StartCorner corner;
	public static int MyHome_lowerLeft[] = new int[2];
	public static int MyHome_upperRight[] = new int[2];
	public static int OppHome_lowerLeft[] = new int[2];
	public static int OppHome_upperRight[] = new int[2];
	public static int Dropzone[]=new int[2];
	public static int Opp_Color=0;
	public static int Home_Color=0;
	static int start_coord[] = {0, 0};
	

	public static int flagColor;
	private static final String SERVER_IP = "192.168.10.200";
	private static final int TEAM_NUMBER = 18;

	public static void main(String[] args) {
		// get info from wifi class
		//adjust coordinates from wifi class to point to middle of the tiles
		
			WifiConnection conn = null;
			try {
				conn = new WifiConnection(SERVER_IP, TEAM_NUMBER);
			} catch (IOException e) {
				LCD.drawString("Connection failed", 0, 8);
			}
			
			Transmission t = conn.getTransmission();
			if (t == null) {
				LCD.drawString("Failed to read transmission...", 0, 5);
			} else {
				corner = t.startingCorner;
				MyHome_lowerLeft[0] = t.opponentHomeZoneBL_X;
				MyHome_lowerLeft[1] = t.opponentHomeZoneBL_Y;
				MyHome_upperRight[0]=t.opponentHomeZoneTR_X;
				MyHome_upperRight[1]=t.opponentHomeZoneTR_Y;
				OppHome_lowerLeft[0] = t.homeZoneBL_X;
				OppHome_lowerLeft[1] = t.homeZoneBL_Y;
				OppHome_upperRight[0]=t.homeZoneTR_X;
				OppHome_upperRight[1]=t.homeZoneTR_Y;
				Dropzone[0] = t.dropZone_X;
				Dropzone[1] = t.dropZone_Y;
				Home_Color = t.flagType;
				Opp_Color = t.opponentFlagType;
			}		
			
			
		
		start_coord = corner.getCoordinates();
		System.out.println("start_coord x" +  start_coord[0]);
		System.out.println("start_coord y" +  start_coord[1]);
		
		odometer.setX(start_coord[0] - tile/2);
		odometer.setY(start_coord[1] - tile/2);
		

		usPoller_right.start(); 
		odometer.start(); 
		LCDInfo lcd = new LCDInfo(odometer);
//		
		leftMotor.setAcceleration(2000);
		rightMotor.setAcceleration(2000);
		
		
		//Localize 
		localizer.doLocalization(); 
		navigator.turnTo(0);

		usSensor_left = new EV3UltrasonicSensor(SensorPort.S2); 
		usPoller_left = new UltrasonicPoller(usSensor_left);
		usPoller_left.start(); 
		justDoIt.start();
		
		//determine entry point to search area, pass entry points to flag capture
		double[] entry_point = planner.getEntryPoints(); 
		System.out.println("entry_point x " + entry_point[0]);
		System.out.println("entry_point x " + entry_point[1]);
		planner.setDestination(entry_point); 
		//navigate to entry point
		planner.travel(); 
		
		FlagCapture IwannaWin = new FlagCapture(entry_point); 
		IwannaWin.Search(); 
		
		double[] drop_coord = new double [2]; 
		drop_coord [0] = Dropzone[0]*tile + tile/2; 
		drop_coord [1] = Dropzone[1]*tile + tile/2; 
		
		planner.setDestination(drop_coord);
		planner.travel(); 
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE); 
		System.exit(0); 
	

		
	}
	
	

}
