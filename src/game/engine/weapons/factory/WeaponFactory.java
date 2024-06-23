package game.engine.weapons.factory;

import java.io.IOException;
import java.util.HashMap;

import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InsufficientResourcesException;
import game.engine.weapons.WeaponRegistry;

public class WeaponFactory 
{
	private final HashMap<Integer, WeaponRegistry> weaponShop;

	public WeaponFactory() throws IOException
	{
		super();
		weaponShop = DataLoader.readWeaponRegistry();
	}

	public HashMap<Integer, WeaponRegistry> getWeaponShop()
	{
		return weaponShop;
	}
	
	public FactoryResponse buyWeapon(int resources, int weaponCode) throws InsufficientResourcesException {
		WeaponRegistry weaponRegistry;
		FactoryResponse factoryResponse;
		int remainingResources=resources;
		
		   if (weaponShop.containsKey(weaponCode)) {
	         weaponRegistry = weaponShop.get(weaponCode);

	        
	        if (resources >= weaponRegistry.getPrice()) {
	             remainingResources = resources - weaponRegistry.getPrice();
	        } 

	        else if(resources <weaponRegistry.getPrice()){
	        
	            throw new InsufficientResourcesException("Insufficient resources to buy the weapon",resources);
	    }
	}
		   else {
			   throw new IllegalArgumentException("Invalid weapon code");
		   
		   }
		   factoryResponse=new FactoryResponse(weaponRegistry.buildWeapon(),remainingResources);
		return factoryResponse;

	}
	
	public void addWeaponToShop(int code, int price) {
		WeaponRegistry weaponregistry=new WeaponRegistry(code,price);
		weaponShop.put(code,weaponregistry);
	}
	
	public void addWeaponToShop(int code, int price, int damage, String name) {
		WeaponRegistry weaponregistry=new WeaponRegistry(code,price,damage,name);
		weaponShop.put(code,weaponregistry);
	}
	public void addWeaponToShop(int code, int price, int damage, String name, int minRange,int maxRange) {
		WeaponRegistry weaponregistry=new WeaponRegistry(code,price,damage,name,minRange,maxRange);
		weaponShop.put(code,weaponregistry);
	}
}
