package net.stormdev.RPManager;

import java.io.IOException;

public class RPManager {
	
	private static final String RPURL = "http://minecraft.curseforge.com/texture-packs/74301-mario-kart-resource-pack/files/latest"; //Always downloads latest
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
	
	private static String getURLFromCurseForgePage() throws IOException {
		return RPURL;
	}
	
	/*
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
	*/
}
