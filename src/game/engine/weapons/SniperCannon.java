package game.engine.weapons;

import java.util.PriorityQueue;

import game.engine.titans.Titan;

public class SniperCannon extends Weapon {
	public static final int WEAPON_CODE = 2;

	public SniperCannon(int baseDamage) {
		super(baseDamage);
	}
	//Attacks the closest titan if it exists
	@Override
	public int turnAttack(PriorityQueue<Titan> laneTitans) {

		if (laneTitans.isEmpty()) {
			return 0;
		}

		Titan targetTitan = laneTitans.poll();

		if (this.attack(targetTitan) != 0) {
			return targetTitan.getResourcesValue();
		} else {
			laneTitans.add(targetTitan);
		}

		return 0;
	}

}
