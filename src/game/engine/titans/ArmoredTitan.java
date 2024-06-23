package game.engine.titans;

import game.engine.interfaces.Attackee;

public class ArmoredTitan extends Titan implements Attackee {
	public static final int TITAN_CODE = 3;

	public ArmoredTitan(int baseHealth, int baseDamage, int heightInMeters, int distanceFromBase, int speed,
			int resourcesValue, int dangerLevel) {
		super(baseHealth, baseDamage, heightInMeters, distanceFromBase, speed, resourcesValue, dangerLevel);
	}

	public int takeDamage(int damage) {

		return  super.takeDamage((int) (damage*0.25));
	}

}
