package Robocode_Sistema;
import robocode.*;
//import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Samu_do_Bruno - a robot by (your name here)
 */
public class Samu_do_Bruno extends Robot
{
	/**
	 * run: Samu_do_Bruno's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		setColors(Color.red,Color.blue,Color.green); // Corpo X Arma Y Radar Z
		
		while(true) {
			//Verificação do modo que o robô assumirá
			if(getOthers() == 1){
			// Comportamento para quando houver apenas um robo vivo
			}
			else{
			
			}
			ahead(100);
			turnGunRight(360);
			back(100);
			turnGunRight(360);
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		fire(1);
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);
	}
	public void onHitWall(HitWallEvent e) {
		// Bater em uma parede
		turnRight(90);
		back(20);
	}	
}
