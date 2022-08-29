/*
 * Copyright (c) 2001-2022 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package upa;


import robocode.HitRobotEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.*;


/**
 * Walls - a sample robot by Mathew Nelson, and maintained by Flemming N. Larsen
 * <p>
 * Moves around the outer edge with the gun facing in.
 *
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 */
public class SAMUdoBruno extends Robot {
	private GFGun gun = new GFGun();
	
	private static int GF_SIZE = 25;
	private static int GF_CENTER = (GF_SIZE - 1) / 2;
	private static int[] guessFactors = new int[GF_SIZE];
	public static double[] dangerFactors = new double[GF_SIZE];
	private Point2D targetLocation;
	private WaveSurfer body = new WaveSurfer();
	private ArrayList<EnemyWave> enemyWaves = new ArrayList();

	boolean peek; // Don't turn if there's a robot there
	double moveAmount; // How much to move

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

		// turnLeft to face a wall.
		// getHeading() % 90 means the remainder of
		// getHeading() divided by 90.
		turnLeft(getHeading() % 90);
		ahead(moveAmount);
		// Turn the gun to turn right 90 degrees.
		peek = true;
		turnGunRight(90);
		turnRight(90);

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
		fire(2);
		// Note that scan is called automatically when the robot is moving.
		// By calling it manually here, we make sure we generate another scan event if there's a robot on the next
		// wall, so that we do not start moving up it until it's gone.
		if (peek) {
			scan();
		}
	}
	
	private class WaveSurfer{
		private ScannedRobotEvent lastScan = null;
		
		public void onScannedRobot(ScannedRobotEvent e){
			if(lastScan != null){
				double bulletPower = lastScan.getEnergy() - e.getEnergy();
				if(0.1 <= bulletPower && bulletPower <= 3.0){
					enemyWaves.add(new EnemyWave(lastScan, bulletPower));
				}
			}
			lastScan = e;
		}
		
		public void updateWaves(){
			for(int i = 0; i < enemyWaves.size();i++){
				if(enemyWaves.get(i).test()){
					enemyWaves.remove(i);
					i--;
				}
			}
		}
		public void surf(){
			EnemyWave closestWave = getClosestWave();
			if(closestWave != null){
				goTo(closestWave.getSafestSpot());
			}
		}
		
		public EnemyWave getClosestWave(){
			double minDistance = Double.POSITIVE_INFINITY;
			EnemyWave closestWave = null;
			for(EnemyWave wave: enemyWaves){
				double distance = wave.distanceFromMe();
				if(distance < minDistance && distance > wave.bulletSpeed){
					closestWave = wave;
					minDistance = distance;
				}
			}
			return closestWave;
		}		
		
		public void goTo(Point2D spot){
			int x = (int) spot.getX() - (int) getX();
			int y = (int) spot.getY() - (int) getY();
			double turn = Math.atan2(x,y);
			setTurnRightRadians(Math.tan(turn - getHeadingRadians()));
			setAhead(Math.hypot(x,y) * Math.cos(turn));
		}

		public void updateFactors(EnemyWave wave, Point2D location){
			int index = wave.getFactorIndex(location);
			for(int i = 0; i < GF_SIZE; i++){
				dangerFactors[i] += 1.0 / (Math.pow(index - i, 2) + 1);
			}
		}
	}
}
