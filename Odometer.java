package Project;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {

	// robot position
	private double x, y, theta;
	private double distL, distR, deltaD, deltaT, dx, dy;
	private double Wl, Wr, Wb; // basic parameters of the robot (wheel radii and
								// wheel base)
	private int nowTachoL, nowTachoR, lastTachoL, lastTachoR;
	

	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 20;

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer() {

		// Initialize position, orientation, and variables for calculation.
		x = 0.0;
		y = 0.0;
		theta = Math.PI / 2;
		distL = 0;
		distR = 0;
		deltaD = 0;
		deltaT = 0;
		dx = 0;
		dy = 0;
		lastTachoL = 0;
		lastTachoR = 0;

		// Set robot's data to corresponding basic parameters of the robot.
		Wl = Robot.left_radius;
		Wr = Robot.right_radius;
		Wb = Robot.wheel_base;

		// Assign lock.
		lock = new Object();

	}

	// run method (required for Thread)
	public void run() {

		long updateStart, updateEnd;

		while (true) {

			updateStart = System.currentTimeMillis();

			// Get the current tacho values of the motors.
			nowTachoL = Robot.leftMotor.getTachoCount();
			nowTachoR = Robot.rightMotor.getTachoCount();

			// Calculate the distance traveled by each wheel.
			distL = Math.PI * Wl * (nowTachoL - lastTachoL) / 180;
			distR = Math.PI * Wr * (nowTachoR - lastTachoR) / 180;

			// Replace the last tacho values by current tacho values (for the
			// next turn).
			lastTachoL = nowTachoL;
			lastTachoR = nowTachoR;

			deltaD = (distL + distR) / 2;
			deltaT = (distR - distL) / Wb;

			synchronized (lock) {

				theta = theta + deltaT; // Update current orientation in radians
				// wrap around
				theta = theta - Math.floor(theta / (2 * Math.PI)) * 2 * Math.PI;

				// Calculate the displacement in each axis (trigonometry).
				dx = deltaD * Math.cos(theta);
				dy = deltaD * Math.sin(theta);

				// Update current position in each axis.
				x = x + dx;
				y = y + dy;

			}

			// This ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();

			if (updateEnd - updateStart < ODOMETER_PERIOD) {

				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {

					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread

				}

			}

		}

	}

	// accessors
	public void getPosition(double[] position) {

		// Ensure that the values don't change while the odometer is running
		synchronized (lock) {
			position[0] = x;
			position[1] = y;
			position[2] = Math.toDegrees(theta);
		}

	}

	public double getX() {

		double result;

		synchronized (lock) {
			result = x;
		}

		return result;

	}

	public double getY() {

		double result;

		synchronized (lock) {
			result = y;
		}

		return result;

	}

	public double getTheta() {

		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;

	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {

		// Ensure that the values don't change while the odometer is running
		synchronized (lock) {

			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];

		}

	}

	public void setX(double x) {

		synchronized (lock) {
			this.x = x;
		}

	}

	public void setY(double y) {

		synchronized (lock) {
			this.y = y;
		}

	}

	public void setTheta(double theta) {

		synchronized (lock) {
			this.theta = theta;
		}

	}

}
