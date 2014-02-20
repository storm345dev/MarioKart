package net.stormdev.RPManager;

import java.io.IOException;

import lib.org.jsoup.Connection;
import lib.org.jsoup.Jsoup;
import lib.org.jsoup.nodes.Document;
import lib.org.jsoup.nodes.Element;
import lib.org.jsoup.select.Elements;
import net.stormdev.mario.mariokart.MarioKart;

public class RPManager {
	
	private static final String fallbackURL = "http://www.curseforge.com/media/files/774/770/MarioKart-latest.zip"; //Current one at time of writing
	
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
					//Ignore, and count as another try...
				}
				tries--;
			}
			if(url != null){
				return url;
			}
		}
		else if(configInput.equalsIgnoreCase("NONE") || configInput.length() < 1){
			return "";
		}
		return configInput;
	}
	
	private static String getURLFromCurseForgePage() throws IOException{
		Connection con = Jsoup.connect("http://minecraft.curseforge.com/texture-packs/mario-kart-resource-pack/");
		con.followRedirects(true); //In case curse move the URL
		
		Document doc;
		try {
			doc = con.get();
		} catch (Exception e1) {
			MarioKart.logger.info("Unable to determine URL of MarioKart RP! Using built in default version...");
			return fallbackURL;
		}
		
		Element downloadSpan = null;
		for(Element e:doc.getAllElements()){
			if(e.className().equalsIgnoreCase("user-action user-action-download")){
				downloadSpan = e;
			}
		}
		
		if(downloadSpan == null){
			MarioKart.logger.info("Unable to determine URL of MarioKart RP! Using built in default version...");
			return fallbackURL;
		}
		Element downloadButton = null;
		for(Element e:downloadSpan.getAllElements()){
			if(e.tagName().equalsIgnoreCase("a")){
				//It's a link
				downloadButton = e;
			}
		}
		if(downloadButton == null){
			MarioKart.logger.info("Unable to determine URL of MarioKart RP! Using built in default version...");
			return fallbackURL;
		}
		String pageURL = downloadButton.absUrl("href"); //Absolute link to download page...
		con = Jsoup.connect(pageURL);
		con.followRedirects(true);
		
		doc = con.get();
		Elements contentBox = doc.getElementsByClass("content-box-inner");
		Element link = contentBox.select("a").first();
		if(link == null){
			MarioKart.logger.info("Unable to determine URL of MarioKart RP! Using built in default version...");
			return fallbackURL;
		}
		
		String downloadURL = link.absUrl("href");
		if(downloadURL == null || downloadURL.length() < 1){
			MarioKart.logger.info("Unable to determine URL of MarioKart RP! Using built in default version...");
			return fallbackURL;
		}
		
		return downloadURL;
	}
}
