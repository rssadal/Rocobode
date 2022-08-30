package upa;

import robocode.HitRobotEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import robocode.Rules;
import robocode.HitByBulletEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

public class SAMUdoBruno extends Robot {

	boolean peek; // Don't turn if there's a robot there
	double moveAmount; // How much to move
	double gunTurnAmt;

	/**
	 * run: Move around the walls
	 */
	public void run() {
		// Set colors
		setColors(Color.white,Color.red,Color.red); // Corpo X Arma Y Radar Z

		// Initialize moveAmount to the maximum possible for this battlefield.
		moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		// Initialize peek to false
		peek = false;
		gunTurnAmt = 10;
		// turnLeft to face a wall.
		// getHeading() % 90 means the remainder of
		// getHeading() divided by 90.
		turnLeft(getHeading() % 90);
		ahead(moveAmount);
		// Turn the gun to turn right 90 degrees.
		peek = true;
		turnRight(90);
		turnGunRight(90);
		while (true) {
			// Look before we turn when ahead() completes.
			peek = true;
			// Move up the wall
			ahead(moveAmount);
			// Don't look now
			peek = false;
			
			// Turn to the next wall
			turnRight(90);
		}
	}
	
	public void onHitByBullet(HitByBulletEvent e){
		turnRight(45);
		ahead(moveAmount);
		turnRight(45);
	}

	/**
	 * onHitRobot:  Move away a bit.
	 */
	public void onHitRobot(HitRobotEvent e) {
		// If he's in front of us, set back up a bit.
		if (e.getBearing() > -90 && e.getBearing() < 90) {
			back(100);
		} // else he's in back of us, so set ahead a bit.
		else {
			ahead(100);
		}
	}

	/**
	 * onScannedRobot:  Fire!
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		double angleToEnemy = (Math.PI * (getHeading()) / 180) + (Math.PI * (e.getBearing()) / 180);
		double turnToEnemy = Utils.normalRelativeAngle(angleToEnemy - (Math.PI * (getRadarHeading()) / 180));
		double extraTurn = Math.atan(36.0 / e.getDistance()) * (turnToEnemy >= 0 ? 1 : -1);
		turnRadarRight(Math.PI * (turnToEnemy + extraTurn) / 180);
		turnRadarLeft(Math.PI * 45 / 180);
	/*	double skew = (e.getDistance() - 300) / 5 * -Math.signum(getVelocity());
		turnRight(e.getBearing() + 90 + skew);	
		
		if (e.getDistance() > 150) {
			gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
			turnGunRight(gunTurnAmt); 
			turnRight(e.getBearing()); 
			ahead(e.getDistance() - 140);
			return;
		}
		gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
		turnGunRight(gunTurnAmt);*/
		shoot(e);
		
		if(peek){		
			scan();
		}
	}

	public void shoot(ScannedRobotEvent e) {
		double firePower = decideFirePower(e);
		double absoluteBearing = (Math.PI * (e.getBearing()) / 180) + (Math.PI * (getHeading())/180);
		//double absoluteBearing = e.getBearing() + getHeading();
		double gunTurn = absoluteBearing - (Math.PI * (getGunHeading()) / 180);
		//double gunTurn = absoluteBearing - getGunHeading();
		double future = e.getVelocity() * Math.sin((Math.PI * (e.getHeading()) / 180) - absoluteBearing) / Rules.getBulletSpeed(firePower);
		//double future = e.getVelocity() * Math.sin((e.getHeading()) - absoluteBearing) / Rules.getBulletSpeed(firePower);		
		turnGunRight(Math.PI * (Utils.normalRelativeAngle(gunTurn + future)) / 180);
		//turnGunRight(Utils.normalRelativeAngle(gunTurn + future));
		fire(firePower);
	}
	
	public double decideFirePower(ScannedRobotEvent e){
		double firePower = getOthers() == 1 ? 2.0 : 3.0;
		
		if(e.getDistance() > 400){
			firePower = 2.0;
		}else if(e.getDistance() < 200) {
			firePower = 4.0;
		}
		/*
		if(getEnergy() < 1){
			firePower = 0.1;
		}else if(getEnergy() < 10){
			firePower = 1.0;
		}*/
		return Math.max(e.getEnergy() / 4, firePower);
	}
}
