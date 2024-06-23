package game.engine.weapons;

public class WeaponRegistry {
	private final int code;
	private int price;
	private int damage;
	private String name;
	private int minRange;
	private int maxRange;

	public WeaponRegistry(int code, int price) {
		super();
		this.code = code;
		this.price = price;
	}

	public WeaponRegistry(int code, int price, int damage, String name) {
		super();
		this.code = code;
		this.price = price;
		this.damage = damage;
		this.name = name;
	}

	public WeaponRegistry(int code, int price, int damage, String name, int minRange, int maxRange) {
		super();
		this.code = code;
		this.price = price;
		this.damage = damage;
		this.name = name;
		this.minRange = minRange;
		this.maxRange = maxRange;
	}

	public int getCode() {
		return code;
	}

	public int getPrice() {
		return price;
	}

	public int getDamage() {
		return damage;
	}

	public String getName() {
		return name;
	}

	public int getMinRange() {
		return minRange;
	}

	public int getMaxRange() {
		return maxRange;
	}

	public Weapon buildWeapon() {
		Weapon weapon = null;

		switch (code) {
		case 1:
			weapon = new PiercingCannon(getDamage());
			break;
		case 2:
			weapon = new SniperCannon(getDamage());
			break;
		case 3:
			weapon = new VolleySpreadCannon(getDamage(), getMinRange(), getMaxRange());
			break;
		case 4:
			weapon = new WallTrap(getDamage());
			break;
		}
		return weapon;
	}

}
