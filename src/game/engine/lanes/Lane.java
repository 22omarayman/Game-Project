package game.engine.lanes;

import java.util.ArrayList;

import java.util.PriorityQueue;

import game.engine.base.Wall;
import game.engine.titans.Titan;
import game.engine.weapons.Weapon;

public class Lane implements Comparable<Lane> {
	private final Wall laneWall;
	private int dangerLevel;
	private final PriorityQueue<Titan> titans;
	private final ArrayList<Weapon> weapons;

	public Lane(Wall laneWall) {
		super();
		this.laneWall = laneWall;
		this.dangerLevel = 0;
		this.titans = new PriorityQueue<>();
		this.weapons = new ArrayList<>();
	}

	public Wall getLaneWall() {
		return this.laneWall;
	}

	public int getDangerLevel() {
		return this.dangerLevel;
	}

	public void setDangerLevel(int dangerLevel) {
		this.dangerLevel = dangerLevel;
	}

	public PriorityQueue<Titan> getTitans() {
		return this.titans;
	}

	public ArrayList<Weapon> getWeapons() {
		return this.weapons;
	}

	@Override
	public int compareTo(Lane o) {
		return this.dangerLevel - o.dangerLevel;
	}

	public void addTitan(Titan titan) {
		titans.add(titan);
	}

	public void addWeapon(Weapon weapon) {
		weapons.add(weapon);
	}

	public void moveLaneTitans() {
		PriorityQueue<Titan> temp = new PriorityQueue<>();
		while (!titans.isEmpty()) {
			Titan T = titans.poll();

			T.move();
			temp.add(T);
		}
		titans.clear();
		titans.addAll(temp);
	}

	// Perform attacks of titans that have reached the base or wall
	public int performLaneTitansAttacks() {
		int resources = 0;
		for (Titan T : titans) {
			if (T.getDistance() == 0)
				resources += T.attack(laneWall);
		}
		return resources;
	}

	// Perform attacks of all weapons against titans in the lane
	public int performLaneWeaponsAttacks() {
		int resourcesGathered = 0;
		for (Weapon weapon : weapons) {
			resourcesGathered += weapon.turnAttack(titans);
		}
		return resourcesGathered;
	}

	public boolean isLaneLost() {
		return laneWall.getCurrentHealth() <= 0;
	}

	public void updateLaneDangerLevel() {
		int totalDangerLevel = 0;
		for (Titan titan : titans) {
			totalDangerLevel += titan.getDangerLevel();
		}
		setDangerLevel(totalDangerLevel);
	}

}
