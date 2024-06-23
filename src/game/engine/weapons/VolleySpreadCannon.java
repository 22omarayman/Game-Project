package game.engine.weapons;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import game.engine.titans.Titan;

public class VolleySpreadCannon extends Weapon {
	public static final int WEAPON_CODE = 3;

	private final int minRange;
	private final int maxRange;

	public VolleySpreadCannon(int baseDamage, int minRange, int maxRange) {
		super(baseDamage);
		this.minRange = minRange;
		this.maxRange = maxRange;
	}

	public int getMinRange() {
		return minRange;
	}

	public int getMaxRange() {
		return maxRange;
	}
	@Override
	public int turnAttack(PriorityQueue<Titan> laneTitans) {
	    int totalResourcesGained = 0;
	    List<Titan> survivingTitans = new ArrayList<>();

	    // Iterate through the lane's Titans
	    while (!laneTitans.isEmpty()) {
	        Titan titan = laneTitans.poll();
	        int distance = titan.getDistance();
	        
	        // Check if the Titan is within the specified attack range
	        if (distance >= minRange && distance <= maxRange) {
	            // Attack the Titan and calculate the damage
	            int damage = this.attack(titan);
	            
	            // Reduce the Titan's health by the amount of damage dealt
	            titan.takeDamage(damage);
	            
	            // Check if the Titan was defeated
	            if (titan.getCurrentHealth() <= 0) {
	                // Add resources gained from the defeated Titan
	                totalResourcesGained += titan.getResourcesValue();
	            } else {
	                // If the Titan is not defeated, add it to the list of surviving Titans
	                survivingTitans.add(titan);
	            }
	        } else {
	            // If the Titan is out of range, add it to the list of surviving Titans
	            survivingTitans.add(titan);
	        }
	    }
	    
	    // Reinsert surviving Titans back into the lane's priority queue
	    laneTitans.addAll(survivingTitans);

	    return totalResourcesGained;
	}


}
