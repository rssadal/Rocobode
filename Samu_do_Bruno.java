package upa;
import robocode.*;
import robocode.util.*;
import java.awt.geom.*;
import java.awt.*;

public class SAMU1V1 extends AdvancedRobot {
	int motionDirection = 1;//direcao do movimento (frente tras)
	double edgeMovement; // movimento necessario para alcançar a borda

	public void run() {
        setColors(Color.white,Color.red,Color.red); // Corpo X Arma Y Radar Z
        setBulletColor(Color.red);//cor do projetil
		edgeMovement = Math.max(getBattleFieldWidth(), getBattleFieldHeight()); //get no tamanho do campo de batalha
		if(getOthers() > 2){
			turnLeft(getHeading() % 90); // vira para a esquerda para ir para a parede
			ahead(edgeMovement); //de fato, corre até a parede
			turnGunRight(90);// vira a arma para deixar ela sempre apontada para dentro do campo de batalha
			turnRight(90);//vira o robo junto com a arma (nesse ponto o robo esta a uma diferença de 90 graus da direcao arma)
		}
		
        while (true) {
            if(getOthers() < 3)//caso so existam dois inimigos no campo, o modo walls será desabilitado 
                break;
			ahead(edgeMovement);//depois de espiar, esta autorizado correr para a proxima borda do campo
			turnRight(90);//ao chegar na proxima borda, preciso virar 90 graus para continuar no sentido horário percorrendo as paredes
		}
        //modo tracker ativado
		setAdjustRadarForRobotTurn(true);//mantem o radar estavel quando o robo virar para qualquer direcao		
		setAdjustGunForRobotTurn(true);  //mantem a arma estavel quando o robo virar para qualquer direcao	
		turnRadarRightRadians(Double.POSITIVE_INFINITY);//matem o radar virando para a direita
	}

	public void onScannedRobot(ScannedRobotEvent e) {
        if(getOthers() < 3){//quando o modo tracker esta ativado
			inCorner();
            double angleObject = e.getBearingRadians() + getHeadingRadians();//angulo absoluto entre meu robo e o inimigo
            double enemyVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - angleObject);//determina velocidade do inimigo
            double turnCannon = 0.0;//o quanto eu devo virar minha arma
            setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//trava o radar em algum inimigo scaneado
            aimBot(turnCannon, angleObject, enemyVel, motionDirection, e);//funcao de mira inteligente
			shoot(e);//atiro
        }else{//quando o modo walls esta ativado
			double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
			double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
			double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
			double enemyHeading = e.getHeadingRadians();
			double enemyVelocity = e.getVelocity();
            intelAim(enemyX, enemyY, enemyHeading, enemyVelocity, absoluteBearing);//funcao de mira inteligente
			shoot(e);
			setAhead((e.getDistance() - 140) * motionDirection);    
		}
	}

	public void onHitWall(HitWallEvent e){//quando o modo tracker esta ativado
        if(getOthers() < 3)
		    motionDirection=-motionDirection;//direcao oposta caso eu colida com a parede    
	}

    public void onHitRobot(HitRobotEvent e) {//quando o modo walls esta ativado
        if(getOthers() > 2){
            if (e.getBearing() > -90 && e.getBearing() < 90) //se o inimigo esta na nossa frente, va para tras um pouco
                back(100);          
            else // se o inimigo esta atras de nos, va para frente um pouco
                ahead(100);
        }
	}

    public void shoot(ScannedRobotEvent e) {//funcao que determina a forma correta de atirar
		if(e.getDistance() < 800){//caso a distancia do inimigo scaneado seja menor que 800 eu posso atirar
			double firePower = decideFirePower(e);
			fire(firePower);
		}
	}
	
	public double decideFirePower(ScannedRobotEvent e){//funcao que determina a potencia do tiro de acordo com a distancia e energia
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

	public void inCorner(){//funcao que determina oq fazer caso eu fique nos cornes no modo tracker
		if((getX() == 0 && getY() == getBattleFieldHeight()) || 
			   (getX() == getBattleFieldWidth() && getY() == 0) ||
			   (getX() == getBattleFieldWidth() && getY() == getBattleFieldHeight() ||
			   (getX() == 0 && getY() == 0))){
					turnLeft(getHeading() % 45);
					setAhead(getBattleFieldWidth() / 2);
				}
	}

	public void aimBot(double turnCannon, double angleObject, double enemyVel, int motionDirection, ScannedRobotEvent e){//funcao de mira inteligente do traker
		if(Math.random()>.9)
                setMaxVelocity((12 * Math.random()) + 12);//mudança de velocidade randomica
            if (e.getDistance() > 150) {//caso a distancia seja maior que 150
                turnCannon = robocode.util.Utils.normalRelativeAngle(angleObject - getGunHeadingRadians() + enemyVel / 22);//o quanto eu devo virar minha arma
                setTurnGunRightRadians(turnCannon);//vira a minha arma
                setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(angleObject - getHeadingRadians() + enemyVel / getVelocity()));//anda para a posicao futura prevista do inimigo
                setAhead((e.getDistance() - 140) * motionDirection);//corro para frente
                
            }else{//caso eu nao esteja perto o suficiente
                turnCannon = robocode.util.Utils.normalRelativeAngle(angleObject - getGunHeadingRadians() + enemyVel / 15);//o quanto eu devo virar minha arma
                setTurnGunRightRadians(turnCannon);//vira a minha arma
                setTurnLeft(-90 - e.getBearing()); //viro perpendicular ao meu inimigo scaneado
                setAhead((e.getDistance() - 140) * motionDirection);//corro para frente
            }
	}

	public void intelAim(double enemyX, double enemyY, double enemyHeading, double enemyVelocity, double absoluteBearing){//funcao de mira inteligente do walls
		double bulletPower = Math.min(3.0,getEnergy());
			double myX = getX();
			double myY = getY();
			double deltaTime = 0;
			double alturaCampo = getBattleFieldHeight(), larguraCampo = getBattleFieldWidth();
			double previsaoX = enemyX, previsaoY = enemyY;
			while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
				Point2D.Double.distance(myX, myY, previsaoX, previsaoY)){		
				previsaoX += Math.sin(enemyHeading) * enemyVelocity;	
				previsaoY += Math.cos(enemyHeading) * enemyVelocity;
				if(	previsaoX < 18.0 || previsaoY < 18.0 || previsaoX > larguraCampo - 18.0 || previsaoY > alturaCampo - 18.0){
					previsaoX = Math.min(Math.max(18.0, previsaoX), larguraCampo - 18.0);	
					previsaoY = Math.min(Math.max(18.0, previsaoY), alturaCampo - 18.0);
					break;
				}
			}
			double theta = Utils.normalAbsoluteAngle(Math.atan2(previsaoX - getX(), previsaoY - getY()));
			setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
			setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
	}
}
