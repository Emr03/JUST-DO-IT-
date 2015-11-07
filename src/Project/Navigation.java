package Project;

/**
 * 
 * @author Shia LaBlock
 * provides methods for controlling the robot's motion
 */
public class Navigation {


	static double targetX, targetY, targetT; // waypoint coordinates and heading
	static double currentX, currentY, currentT; // current coordinates 
	static double deltaX, deltaY, deltaT;       //displacement required              


	private final int ROTATE_SPEED = 50;           
	private final int FORWARD_SPEED = 100;
	private boolean isNavigating = false;       //boolean that is set to true while the robot navigates
	          
	private double theta_tolerance = 0.087;
	private static double[] waypoint;

	
	/**
	 *  goes through the sequence of waypoints passed in the constructor
	 */
	public void run(){
		isNavigating = true;
		try {
			travelTo(waypoint[0], waypoint[1]); 	
		} catch (InterruptedException e) {
			isNavigating = false;
		 }
	}

	/**
	 * moves the robot to coordinate (x, y)
	 * @param x
	 * @param y
	 */
	public void travelTo(double x, double y) throws InterruptedException {
		currentX = Robot.odometer.getX();
		currentY = Robot.odometer.getY();
		// calculate the change required for each coordinate
		deltaX = x - currentX;
		deltaY = y - currentY;

		while ((Math.abs(deltaX) > 1 || Math.abs(deltaY) > 1)) {

			targetT = getArcTan(deltaX, deltaY);
			adjustHeading(targetT); // Do the turning.

			Robot.leftMotor.setSpeed(FORWARD_SPEED);
			Robot.rightMotor.setSpeed(FORWARD_SPEED);

			Robot.leftMotor.forward();
			Robot.rightMotor.forward();

			currentX = Robot.odometer.getX();
			currentY = Robot.odometer.getY();
			// calculate the change required for each coordinate
			deltaX = x - currentX;
			deltaY = y - currentY;
		} // end of while

		stopMotors();
		isNavigating = false;

	}// end of method travelTo

	/**
	 * returns the binary arctan by looking at deltaX and deltaY
	 * @param deltaX
	 * @param deltaY
	 * @return binary arctan 
	 */
	private double getArcTan(double deltaX, double deltaY) throws InterruptedException{
		double T;
		if ((int) deltaX == 0)
			T = ((int) deltaY >= 0) ? Math.PI / 2 : -Math.PI / 2;

		else if ((int) deltaX < 0)
			T = Math.atan(deltaY / deltaX) + Math.PI;

		else
			T = Math.atan(deltaY / deltaX);

		return T;
	}

	/**
	 * turns the robot to an absolute heading
	 * @param theta
	 * @throws InterruptedException
	 */
	public void turnTo(double theta) throws InterruptedException{
		adjustHeading(theta); 
		stopMotors(); 
	}
	
	/**
	 * adjusts heading while traveling a straight path
	 * @param theta
	 */
	private void adjustHeading(double theta) throws InterruptedException{
		currentT = Robot.odometer.getTheta(); // get current heading
		deltaT = theta - currentT;

		deltaT = getMinAngle(deltaT);

		Robot.leftMotor.setSpeed(ROTATE_SPEED);
		Robot.rightMotor.setSpeed(ROTATE_SPEED);

		while (Math.abs(deltaT) > theta_tolerance) {
			if (deltaT < 0) {
				Robot.leftMotor.forward();
				Robot.rightMotor.backward();
			} else {
				Robot.leftMotor.backward();
				Robot.rightMotor.forward();
			}

			currentT = Robot.odometer.getTheta(); // get current heading
			deltaT = theta - currentT;
			deltaT = getMinAngle(deltaT);

		} // end of while

	}
	
	/**
	 * stops motors synchronously
	 */
	public void stopMotors() {
		Robot.leftMotor.startSynchronization();
		Robot.leftMotor.stop(); 
		Robot.rightMotor.stop();
		Robot.leftMotor.endSynchronization();
	}

	/**
	 * computes the minimal angle needed for turning
	 * @param deltaT angular displacement in radians
	 * @return minimal angle in radians
	 * @throws InterruptedException
	 */
	private double getMinAngle(double deltaT) throws InterruptedException {
		// set minimal theta
		if (deltaT > 0) {
			if (Math.abs(deltaT) > Math.abs(deltaT - 2 * Math.PI))
				deltaT = deltaT - 2 * Math.PI;
		}
		if (deltaT < 0) {
			if (Math.abs(deltaT) > Math.abs(deltaT + 2 * Math.PI))
				deltaT = deltaT + 2 * Math.PI;
		}

		return deltaT;
	}

	/**
	 * indicates whether the robot is navigating
	 * @return boolean flag 
	 */
	public boolean isNavigating() {
		if (isNavigating)
			return true;
		else
			return false;
	}


}
