package net.stormdev.RPManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import net.stormdev.mario.mariokart.MarioKart;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
		
		String URL = "http://minecraft.curseforge.com/texture-packs/mario-kart-resource-pack";
		
		Connection con = Jsoup.connect(URL);
		con.followRedirects(true); //In case curse move the URL
		con.userAgent("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
		//con.header("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
		
		Document doc;
		try {
			doc = con.get();
		} catch (Exception e1) {
			MarioKart.logger.info("Unable to determine URL of MarioKart RP! Using built in default version...");
			return fallbackURL;
		}
		
		System.out.println("Determining URL...");
		
		Element downloadSpan = null;
		for(Element e:doc.getAllElements()){
			if(e.className().equalsIgnoreCase("cf-recentfiles-credits-wrapper")){
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
		//String pageURL = downloadButton.absUrl("href"); //Absolute link to download page...
		/*
		con = Jsoup.connect(pageURL);
		con.followRedirects(true);
		
		doc = con.get();
		Elements contentBox = doc.getElementsByClass("content-box-inner");
		Element link = contentBox.select("a").first();
		if(link == null){
			MarioKart.logger.info("Unable to determine URL of MarioKart RP! Using built in default version...");
			return fallbackURL;
		}
		*/
		
		String downloadCon = downloadButton.absUrl("href"); //Absolute link to curse server! :)
		
		//Follow the server's redirect until we reach the .zip file
		URLConnection c = new URL( downloadCon ).openConnection();
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
}
