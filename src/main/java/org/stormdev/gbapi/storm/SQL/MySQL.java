package org.stormdev.gbapi.storm.SQL;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.plugin.Plugin;

import com.mysql.jdbc.Connection;

/**
 * 
 * Represents a connection to an SQL database
 *
 */
public class MySQL {
	private String url = "jdbc:mysql://localhost/siteName";
	private String user = "user";
	private String password = "pass";
	private Connection con = null;
	private Plugin plugin;
	
	/**
	 * Use for SQL connections
	 * 
	 * @param plugin Your plugin
	 * @param url The JDBC url for the database
	 * @param user The username
	 * @param password The password
	 */
	public MySQL(Plugin plugin, String url, String user, String password){
		this.plugin = plugin;
		this.url = url;
		this.user = user;
		this.password = password;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = (Connection) DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			plugin.getLogger().info("JDBC SQL driver not found! Please install the"
					+ " version required for your OS to run the plugin!");
		} catch (SQLException e) {
			plugin.getLogger().info("Error connecting to SQL database, please check"
					+ " your credentials and try again!");
		}
	}
	
	/**
	 * Attempt to connect to the database
	 */
	public void connect(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = (Connection) DriverManager.getConnection(url, user, password);
			con.setAutoReconnect(true);
			con.setAutoReconnectForConnectionPools(true);
			con.setAutoReconnectForPools(true);
		} catch (ClassNotFoundException e) {
			plugin.getLogger().info("JDBC SQL driver not found! Please install the"
					+ " version required for your OS to run the plugin!");
		} catch (SQLException e) {
			plugin.getLogger().info("Error connecting to SQL database, please check"
					+ " your credentials and try again!");
		}
	}
	
	/**
	 * Check if connected to the database
	 * 
	 * @return true if connected
	 */
	public boolean isConnected(){
		try {
			return con != null && con.isValid(3000);
		} catch (SQLException e) {
			return false;
		}
	}
	
	/**
	 * Get the sql connection
	 * @return The sql connection
	 */
	public Connection getConnection(){
		return con;
	}
}
