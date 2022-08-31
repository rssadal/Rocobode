package upa;
import robocode.*;
import java.awt.*;

public class SAMU1V1 extends AdvancedRobot {
	int motionDirection = 1;//direcao do movimento (frente tras)
	boolean espiao; // nao virar caso tenha um robo no caminho (atire até ele sair do caminho)
	double edgeMovement; // movimento necessario para alcançar a borda

	public void run() {
        setColors(Color.white,Color.red,Color.red); // Corpo X Arma Y Radar Z
        setBulletColor(Color.red);//cor do projetil
		edgeMovement = Math.max(getBattleFieldWidth(), getBattleFieldHeight()); //get no tamanho do campo de batalha
		espiao = false;
		if(getOthers() > 2){
			turnLeft(getHeading() % 90); // vira para a esquerda para ir para a parede
			ahead(edgeMovement); //de fato, corre até a parede
			espiao = true;
			turnGunRight(90);// vira a arma para deixar ela sempre apontada para dentro do campo de batalha
			turnRight(90);//vira o robo junto com a arma (nesse ponto o robo esta a uma diferença de 90 graus da direcao arma)
		}
		
        while (true) {
			espiao = true;//apos chegar a parede, o espiao pode ser ativado para verificar se existem inimigos no caminho da parede
			ahead(edgeMovement);//depois de espiar, esta autorizado correr para a proxima borda do campo
			espiao = false;//enquanto estou correndo, nao estou espiando
			turnRight(90);//ao chegar na proxima borda, preciso virar 90 graus para continuar no sentido horário percorrendo as paredes
            if(getOthers() < 3)//caso so existam dois inimigos no campo, o modo walls será desabilitado 
                break;
		}
        //modo tracker ativado
		setAdjustRadarForRobotTurn(true);//mantem o radar estavel quando o robo virar para qualquer direcao		
		setAdjustGunForRobotTurn(true);  //mantem a arma estavel quando o robo virar para qualquer direcao	
		turnRadarRightRadians(Double.POSITIVE_INFINITY);//matem o radar virando para a direita
	}

	public void onScannedRobot(ScannedRobotEvent e) {
        if(getOthers() < 3){//quando o modo tracker esta ativado
            double angleObject = e.getBearingRadians() + getHeadingRadians();//angulo absoluto entre meu robo e o inimigo
            double enemyVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - angleObject);//determina velocidade do inimigo
            double turnCannon;//o quanto eu devo virar minha arma
            setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//trava o radar em algum inimigo scaneado
            if(Math.random()>.9)
                setMaxVelocity((12 * Math.random()) + 12);//mudança de velocidade randomica
            if (e.getDistance() > 150) {//caso a distancia seja maior que 150
                turnCannon = robocode.util.Utils.normalRelativeAngle(angleObject - getGunHeadingRadians() + enemyVel / 22);//o quanto eu devo virar minha arma
                setTurnGunRightRadians(turnCannon);//vira a minha arma
                setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(angleObject - getHeadingRadians() + enemyVel / getVelocity()));//anda para a posicao futura prevista do inimigo
                setAhead((e.getDistance() - 140) * motionDirection);//corro para frente
                shoot(e);//atiro
            }
            else{//caso eu nao esteja perto o suficiente
                turnCannon = robocode.util.Utils.normalRelativeAngle(angleObject - getGunHeadingRadians() + enemyVel / 15);//o quanto eu devo virar minha arma
                setTurnGunRightRadians(turnCannon);//vira a minha arma
                setTurnLeft(-90 - e.getBearing()); //viro perpendicular ao meu inimigo scaneado
                setAhead((e.getDistance() - 140) * motionDirection);//corro para frente
                shoot(e);//atiro
            }	
        }else{//quando o modo walls esta ativado
            shoot(e);//atiro
            if (espiao) //caso o espiao esteja habilitado eu posso scanear a area novamente
                scan();     
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
}
