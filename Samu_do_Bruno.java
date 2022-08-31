package upa;

import robocode.*;
import java.awt.*;

public class SAMU1V1 extends AdvancedRobot {
	int moveDirection=1;//which way to move
	boolean peek; // Don't turn if there's a robot there
	double moveAmount; // How much to move
	/**
	 * run:  Tracker's main run function
	 */
	public void run() {
        setColors(Color.white,Color.red,Color.red); // Corpo X Arma Y Radar Z
        // Initialize moveAmount to the maximum possible for this battlefield.
		moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		// Initialize peek to false
		peek = false;
        // turnLeft to face a wall.
		// getHeading() % 90 means the remainder of
		// getHeading() divided by 90.
		if(getOthers() > 2){
			turnLeft(getHeading() % 90);
			ahead(moveAmount);
			// Turn the gun to turn right 90 degrees.
			peek = true;
			turnGunRight(90);
			turnRight(90);
		}
		
        while (true) {
			// Look before we turn when ahead() completes.
			peek = true;
			// Move up the wall
			ahead(moveAmount);
			// Don't look now
			peek = false;
			// Turn to the next wall
			turnRight(90);
            if(getOthers() < 3)
                break;
		}

		setAdjustRadarForRobotTurn(true);//keep the radar still while we turn
		setScanColor(Color.white);
		setBulletColor(Color.blue);
		setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
		turnRadarRightRadians(Double.POSITIVE_INFINITY);//keep turning radar right
	}

	public void onScannedRobot(ScannedRobotEvent e) {
        if(getOthers() < 3){
            double absBearing=e.getBearingRadians()+getHeadingRadians();//enemies absolute bearing
            double latVel=e.getVelocity() * Math.sin(e.getHeadingRadians() -absBearing);//enemies later velocity
            double gunTurnAmt;//amount to turn our gun
            setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
            if(Math.random()>.9){
                setMaxVelocity((12*Math.random())+12);//randomly change speed
            }
            if (e.getDistance() > 150) {//if distance is greater than 150
                gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/22);//amount to turn our gun, lead just a little bit
                setTurnGunRightRadians(gunTurnAmt); //turn our gun
                setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+latVel/getVelocity()));//drive towards the enemies predicted future location
                setAhead((e.getDistance() - 140)*moveDirection);//move forward
                shoot(e);
            }
            else{//if we are close enough...
                gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/15);//amount to turn our gun, lead just a little bit
                setTurnGunRightRadians(gunTurnAmt);//turn our gun
                setTurnLeft(-90-e.getBearing()); //turn perpendicular to the enemy
                setAhead((e.getDistance() - 140)*moveDirection);//move forward
                shoot(e);
            }	
        }else{
            shoot(e);
            if (peek) {
                scan();
            }
        }
	}

	public void onHitWall(HitWallEvent e){
        if(getOthers() < 3){
		    moveDirection=-moveDirection;//reverse direction upon hitting a wall
        }
	}

    public void onHitRobot(HitRobotEvent e) {
        if(getOthers() > 2){
            // If he's in front of us, set back up a bit.
            if (e.getBearing() > -90 && e.getBearing() < 90) {
                back(100);
            } // else he's in back of us, so set ahead a bit.
            else {
                ahead(100);
            }
        }
	}
    
    public void shoot(ScannedRobotEvent e) {
		if(e.getDistance() < 800){
			double firePower = decideFirePower(e);
			fire(firePower);
		}
	}
	
	public double decideFirePower(ScannedRobotEvent e){
		double firePower = getOthers() == 1 ? 3.0 : 2.0;
		
		if(e.getDistance() > 400){
			firePower = 1.0;
		}else if(e.getDistance() < 200) {
			firePower = 3.0;
		}
		if(getEnergy() < 1){
			firePower = 0.1;
		}else if(getEnergy() < 10){
			firePower = 1.0;
		}
		return Math.max(e.getEnergy() / 4, firePower);
	}
}
