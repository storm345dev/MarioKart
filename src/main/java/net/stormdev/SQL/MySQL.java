package net.stormdev.SQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.plugin.Plugin;

public class MySQL {
	private String url = "jdbc:mysql://localhost/siteName";
	private String user = "user";
	private String password = "pass";
	private Connection con = null;
	private Plugin plugin;
	
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
	
	public void connect(){
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
	
	public boolean isConnected(){
		try {
			return con != null && con.isValid(3000);
		} catch (SQLException e) {
			return false;
		}
	}
	
	public Connection getConnection(){
		return con;
	}
}
