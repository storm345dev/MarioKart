package net.stormdev.mario.utils;

import java.util.HashMap;
import java.util.UUID;

import net.stormdev.mario.mariokart.Race;
import net.stormdev.mario.mariokart.main;

public class DynamicLagReducer implements Runnable {
	public static int TICK_COUNT = 0;
	public static long[] TICKS = new long[600];
	public static long LAST_TICK = 0L;

	public static double getTPS() {
		return getTPS(100);
	}

	public static double getAvailableMemory(){
		return Runtime.getRuntime().freeMemory() * 0.00097560975 * 0.00097560975; //In MB
	}
	
	public static double getMaxMemory(){
		return Runtime.getRuntime().maxMemory() * 0.00097560975 * 0.00097560975; //In MB
	}
	
	public static double getMemoryUse(){
		return getMaxMemory()-getAvailableMemory();
	}
	
	public static boolean overloadPrevention(){
		long freeMemory = (long) (Runtime.getRuntime().freeMemory() * 0.00097560975 * 0.00097560975); //In MB
		if(freeMemory < 150){
			System.gc();
			freeMemory = (long) (Runtime.getRuntime().freeMemory() * 0.00097560975 * 0.00097560975); //In MB
			if(freeMemory < 150){
				if(!main.plugin.raceScheduler.isLockedDown()){
					main.plugin.raceScheduler.lockdown(); //Lock all queues
				}
				else{
					//Re-occuring issue
					if(main.plugin.random.nextBoolean() && main.plugin.random.nextBoolean()
							&& main.plugin.random.nextBoolean()){ //Small chance races will get cancelled
						if(main.plugin.raceScheduler.getRacesRunning() > 0){
							//Terminate a race
							try {
								HashMap<UUID, Race> races = new HashMap<UUID, Race>(main.plugin.raceScheduler.getRaces());
								Object[] ids = races.keySet().toArray();
								UUID id = (UUID) ids[main.plugin.random.nextInt(ids.length)];
								Race r = races.get(id);
								r.broadcast(main.colors.getError()+"Terminating race due to depleted system resources, sorry.");
								main.plugin.raceScheduler.stopRace(r);
								main.logger.info("[WARNING] Low memory resulted in termination of race: "
										+ id);
							} catch (Exception e) {
								//Error ending race
							}
						}
					}
				}
				return true;
			}
		}
		else{
			if(main.plugin.raceScheduler.isLockedDown() &&
					freeMemory > 200){
				main.plugin.raceScheduler.unlockDown();
			}
		}
		return false;
	}
	
	public static int getResourceScore(){
		double tps = getTPS(100);
		double mem = getAvailableMemory();
		if(tps>19 && mem>500){
			return 100;
		}
		else if(mem < 200){
			return 10;
		}
		else{
			int i = 100;
			i -= 100-(tps*5);
			if(mem < 300){
				i -=20;
			}
			return i;
		}
	}
	
	public static int getResourceScore(double requestedMemory){
		double tps = getTPS(100);
		double mem = getAvailableMemory();
		if(tps>19 && mem>requestedMemory+20){
			return 100;
		}
		else if(mem < requestedMemory){
			return 10;
		}
		else{
			int i = 100;
			i -= 100-(tps*5);
			if(mem < requestedMemory){
				i -=50;
			}
			return i;
		}
	}
	
	public static double getTPS(int ticks) {
		if (TICK_COUNT < ticks) {
			return 20.0D;
		}
		int target = (TICK_COUNT - 1 - ticks) % TICKS.length;
		long elapsed = System.currentTimeMillis() - TICKS[target];
		return ticks / (elapsed / 1000.0D);
	}

	public static long getElapsed(int tickID) {
		long time = TICKS[(tickID % TICKS.length)];
		return System.currentTimeMillis() - time;
	}

	@Override
	public void run() {
		TICKS[(TICK_COUNT % TICKS.length)] = System.currentTimeMillis();
		TICK_COUNT += 1;
		return;
	}
}
