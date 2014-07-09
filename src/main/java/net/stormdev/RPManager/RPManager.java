package net.stormdev.RPManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import net.stormdev.mario.mariokart.MarioKart;

public class RPManager {
	
	private static final String fallbackURL = "http://www.curseforge.com/media/files/774/770/MarioKart-latest.zip"; //Current one at time of writing
	//http://minecraft.curseforge.com/texture-packs/74301-mario-kart-resource-pack/files/774770/download
	/*
	 * Get's the latest RP url from CurseForge... (I wish they extended their API to include RPs...)
	 * 
	 */
	public static String getRPUrl(String configInput){
		if(configInput.equalsIgnoreCase("DEFAULT_CURSEFORGE_PACK")){
			String url = null;
			int tries = 5;
			while(url == null && tries > 0){
				try {
					url = getURLFromCurseForgePage();
				} catch (IOException e) {
					e.printStackTrace();
					//Ignore, and count as another try...
				}
				tries--;
			}
			if(url != null){
				return url;
			}
			else {
				return fallbackURL;
			}
		}
		else if(configInput.equalsIgnoreCase("NONE") || configInput.length() < 1){
			return "";
		}
		return configInput;
	}
	
	private static String getURLFromCurseForgePage() throws IOException{
		MarioKart.logger.info("Attempting to resolve resource pack URL... (This may take a while)");
		
		//Follow the server's redirect until we reach the .zip file
		URLConnection c = new URL("http://minecraft.curseforge.com/texture-packs/74301-mario-kart-resource-pack/files/latest").openConnection();
		c.connect();
		InputStream is = c.getInputStream();
		String rURL = c.getURL().toExternalForm();
		is.close();
		
		String downloadURL = rURL; //We now know the .zip file url!
		if(downloadURL == null || downloadURL.length() < 1){
			MarioKart.logger.info("Unable to determine URL of MarioKart RP! Using built in default version...");
			return fallbackURL;
		}
		MarioKart.logger.info("Resolved ResourcePack URL to: "+downloadURL);
		
		return downloadURL;
	}
	
	public static void main(String args[]){
		if(args.length < 1){
			System.out.println("Need an arg...");
			return;
		}
		
		String url = args[0];
		
		String rURL;
		try {
			//Follow the server's redirect until we reach the .zip file
			URLConnection c = new URL(url).openConnection();
			c.connect();
			InputStream is = c.getInputStream();
			rURL = c.getURL().toExternalForm();
			is.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("Resolved ResourcePack URL to: "+rURL);
	}
}
