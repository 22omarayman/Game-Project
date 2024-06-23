package game.engine.weapons;

import java.util.PriorityQueue;

import game.engine.titans.Titan;

public class WallTrap extends Weapon {
	public static final int WEAPON_CODE = 4;

	public WallTrap(int baseDamage) {
		super(baseDamage);
	}

	@Override
    public int turnAttack(PriorityQueue<Titan> laneTitans) {
        // Attacks the closest titan only if it reached the base/wall
        if (!laneTitans.isEmpty()) {
            Titan closestTitan = laneTitans.peek();
            if (closestTitan.getDistance() == 0) {
                int resourcesGained = closestTitan.takeDamage(getDamage());
                if (closestTitan.isDefeated()) {
                    laneTitans.poll(); // Remove the defeated titan
                }
                return resourcesGained;
            }
        }
        return 0;
    }

}
