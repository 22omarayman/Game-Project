package game.engine.weapons;

import java.util.ArrayList;

import java.util.PriorityQueue;

import game.engine.interfaces.Attackee;
import game.engine.titans.Titan;

public class PiercingCannon extends Weapon implements Attackee {
	public static final int WEAPON_CODE = 1;

	public PiercingCannon(int baseDamage) {
		super(baseDamage);
	}

	@Override
	public int turnAttack(PriorityQueue<Titan> laneTitans) {
		ArrayList<Titan> temp = new ArrayList<Titan>();
		int attackRes = 0;
		for (int i = 0; i < 5 && !laneTitans.isEmpty(); i++) {

			Titan T = laneTitans.poll();
			attackRes += T.takeDamage(getDamage());

			if (!T.isDefeated())
				temp.add(T);

		}
		laneTitans.addAll(temp);
		return attackRes;
	}

	@Override
	public int getCurrentHealth() {
		return this.getCurrentHealth();
	}

	@Override
	public void setCurrentHealth(int health) {
		this.setCurrentHealth(health);
	}

	@Override
	public int getResourcesValue() {
		return this.getResourcesValue();
	}
}
