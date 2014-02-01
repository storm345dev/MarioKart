package net.stormdev.mario.lesslag;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;

import net.stormdev.mario.mariokart.main;
import net.stormdev.mario.races.Race;

public class DynamicLagReducer implements Runnable {
	public static int TICK_COUNT = 0;
	public static long[] TICKS = new long[600];
	public static long LAST_TICK = 0L;
	private long finalTime = 0L;
	private static boolean running = false;

	public static double getTPS() {
		return getTPS(100);
	}

	public static double getAvailableMemory(){
		if(!main.dynamicLagReduce){
			return 1000;
		}
		return Runtime.getRuntime().freeMemory() * 0.00097560975 * 0.00097560975; //In MB
	}
	
	public static double getMaxMemory(){
		return Runtime.getRuntime().maxMemory() * 0.00097560975 * 0.00097560975; //In MB
	}
	
	public static double getMemoryUse(){
		if(!main.dynamicLagReduce){
			return 10;
		}
		return getMaxMemory()-getAvailableMemory();
	}
	
	public static boolean overloadPrevention(){
		if(!main.dynamicLagReduce){
			return false;
		}
		long freeMemory = (long) (Runtime.getRuntime().freeMemory() * 0.00097560975 * 0.00097560975); //In MB
		if(freeMemory < 150){
			main.logger.info("[INFO] Current system available memory: "+freeMemory);
			System.gc();
			freeMemory = (long) (Runtime.getRuntime().freeMemory() * 0.00097560975 * 0.00097560975); //In MB
			if(freeMemory < 150){ //If, after gc, the memory is still running out
				if(!main.plugin.raceScheduler.isLockedDown()){
					main.logger.info("[INFO] Current system available memory (Post cleanup) : "+freeMemory);
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
				main.logger.info("[INFO] Current system available memory (Reopening queues): "+freeMemory);
				main.plugin.raceScheduler.unlockDown();
			}
		}
		return false;
	}
	
	public static int getResourceScore(){
		if(!main.dynamicLagReduce){
			return 1000;
		}
		double tps = getTPS(100);
		double mem = getAvailableMemory();
		if(tps>19 && mem>500){
			return 100;
		}
		else if(mem < 50){
			return 10;
		}
		int i = 100;
		i -= 100-(tps*5);
		if(mem < 300){
			i -=20;
		}
		return i;
	}
	
	public static int getResourceScore(double requestedMemory){
		if(!main.dynamicLagReduce){
			return 100;
		}
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
		try {
			if (TICK_COUNT < ticks || !running) {
				return 20.0D;
			}
			int target = (TICK_COUNT - 1 - ticks) % TICKS.length;
			long elapsed = System.currentTimeMillis() - TICKS[target];
			return ticks / (elapsed / 1000.0D);
		} catch (Exception e) {
			//Has been restarted
			return 20;
		}
	}

	public static long getElapsed(int tickID) {
		long time = TICKS[(tickID % TICKS.length)];
		return System.currentTimeMillis() - time;
	}

	@Override
	public void run() {
		running = true;
		if(finalTime < 1){
			finalTime = System.currentTimeMillis() + 1200000; //20 mins later...
		}
		long current = System.currentTimeMillis();
		if(current > finalTime){
			//Restart
			restart();
			return;
		}
		TICKS[(TICK_COUNT % TICKS.length)] = current;
		TICK_COUNT += 1;
		return;
	}
	
	public void restart(){
		TICK_COUNT = 0;
		TICKS = new long[600];
		LAST_TICK = 0L;
		running = false;
		main.plugin.lagReducer.cancel();
		main.plugin.lagReducer = Bukkit.getScheduler().runTaskTimer(main.plugin,
				new DynamicLagReducer(), 100L, 1L);
		return;
	}
}
