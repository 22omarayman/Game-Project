package game.engine.titans;

import game.engine.interfaces.Mobil;

public class ColossalTitan extends Titan implements Mobil {
	public static final int TITAN_CODE = 4;
	

	public ColossalTitan(int baseHealth, int baseDamage, int heightInMeters, int distanceFromBase, int speed,
			int resourcesValue, int dangerLevel) {
		super(baseHealth, baseDamage, heightInMeters, distanceFromBase, speed, resourcesValue, dangerLevel);
	}
	//Speed (distance moved) increases by 1 after every movement action
	public boolean move() {
		
		this.setDistance(Math.max(0, this.getDistance() - this.getSpeed()));//Math.max got the logic from the test
		this.setSpeed(this.getSpeed() + 1);
		return hasReachedTarget();
	}

}
