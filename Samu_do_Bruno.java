package upa;

import robocode.*;
//import robocode.getBattleFieldWidth;
import robocode.util.Utils;
import java.awt.geom.*;     // for Point2D's
import java.util.ArrayList; // for collection of waves
import java.awt.Color;


public class SAMU1V1 extends AdvancedRobot {
	double moveAmount; // How much to move
	int moveDirection = 1;//+1 frente, -1 trás
	boolean peek;
	
	public void run() {
		setColors(Color.white,Color.red,Color.red); // Corpo X Arma Y Radar Z		
		while(true){
			if(getOthers() <= 2){
				setAdjustRadarForRobotTurn(true);//estabiliza radar ao virar	
				setAdjustGunForRobotTurn(true); //estabiliza arma ao virar
				turnRadarRightRadians(Double.POSITIVE_INFINITY);
			}else{
				moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
				peek = false;
				turnLeft(getHeading() % 90);
				ahead(moveAmount);
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
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
	
		double absoluteBearing = e.getBearingRadians() + getHeadingRadians();              //absolute bearing dos inimigos
		double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() -absoluteBearing);//enemies velocity
		double gunTurnAmount;//amount to turn our gun
		
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
		
		if(getOthers() <= 2){
			if(Math.random()>.9){
				setMaxVelocity((12*Math.random())+12);//mundança de velocidade randomica
			}
			if (e.getDistance() > 150) {//caso a distancia seja maior que 150
				gunTurnAmount = robocode.util.Utils.normalRelativeAngle(absoluteBearing- getGunHeadingRadians()+latVel/22);//ajuste de mira 
				setTurnGunRightRadians(gunTurnAmount); //aplicando ajuste
				setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absoluteBearing-getHeadingRadians()+latVel/getVelocity()));//corre para a posicao futura do inimigo
				setAhead((e.getDistance() - 140)*moveDirection);//pra frente
				shoot(e);
			}
			else{//se nao estiver perto o suficiente.
				gunTurnAmount = robocode.util.Utils.normalRelativeAngle(absoluteBearing- getGunHeadingRadians()+latVel/15);//amount to turn our gun, lead just a little bit
				setTurnGunRightRadians(gunTurnAmount);//turn our gun
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
		/*
	
		if(getOthers() == 1){
			moveDirection=-moveDirection;//corra para direção contraria ao colidir com a parede
		}*/
		setAhead((140)*moveDirection);//move forward
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
