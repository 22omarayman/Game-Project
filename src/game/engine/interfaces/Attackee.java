package game.engine.interfaces;

//Interface containing the methods available to all objects that gets attacked within the game.

public interface Attackee {
	int getCurrentHealth();

	void setCurrentHealth(int health);

	int getResourcesValue();

	default boolean isDefeated() {
		 return this.getCurrentHealth() <= 0;//True if the attackee is defeated, otherwise false.
	}

	default int takeDamage(int damage) {
		
		this.setCurrentHealth(this.getCurrentHealth() - damage);
		
		if (this.isDefeated())
			return this.getResourcesValue();
		return 0;
	}
}
