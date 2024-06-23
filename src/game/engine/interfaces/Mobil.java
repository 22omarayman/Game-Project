package game.engine.interfaces;

//Interface containing the methods available to all objects that has mobility (i.e can move)within the game.

public interface Mobil {
	int getDistance();

	void setDistance(int distance);

	int getSpeed();

	void setSpeed(int speed);

	default boolean hasReachedTarget() {
		return this.getDistance() <= 0; // True if the target is reached, otherwise false.

	}

	default boolean move() {

		this.setDistance(getDistance() - getSpeed());
		
		return this.hasReachedTarget();// True if the target was reached, otherwise false.
	}

}
