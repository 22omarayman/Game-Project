package game.engine.interfaces;

//Interface containing the methods available to all attackers within the game.

public interface Attacker {
	int getDamage(); // gets the damage value to be applied

	default int attack(Attackee target) {
		
		return target.takeDamage(getDamage());
	}

}