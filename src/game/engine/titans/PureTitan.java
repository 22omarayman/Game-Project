package game.engine.titans;

import game.engine.interfaces.Attackee;

public class PureTitan extends Titan implements Attackee {
	public static final int TITAN_CODE = 1;

	public PureTitan(int baseHealth, int baseDamage, int heightInMeters, int distanceFromBase, int speed,
			int resourcesValue, int dangerLevel) {
		super(baseHealth, baseDamage, heightInMeters, distanceFromBase, speed, resourcesValue, dangerLevel);
		
	}

}
