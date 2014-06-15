package net.stormdev.SQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.plugin.Plugin;

public class SQLManager {
	private Connection c;
	private MySQL sqlConnection;
	private Plugin plugin;
	
	public SQLManager(MySQL sqlConnection, Plugin plugin){
	        this.plugin = plugin;
		c = sqlConnection.getConnection();
		try {
			c.setAutoCommit(true);
		} catch (Exception e) {
			plugin.getLogger().info("Error connecting to SQL database!");
		}
	}
	public void closeConnection(){
		try {
			c.close();
		} catch (Exception e) {
			plugin.getLogger().info("Error connecting to SQL database!");
		}
	}
	private boolean checkConnectionStatus(){
		try {
			if(!c.isValid(1000)){
				c.close();
				sqlConnection.connect(); //Reconnect
				try {
					c.setAutoCommit(true);
				} catch (Exception e) {
					plugin.getLogger().info("Error connecting to SQL database!");
				}
				if(!c.isValid(1000)){
					return false;
				}
				plugin.getLogger().info("Successfully re-established connection with the SQL server!");
			}
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	public void checkConnection(){
		if(!checkConnectionStatus()){
			plugin.getLogger().info("Lost connection to the SQL database! Is it offline?");
		}
	}
	public synchronized Object searchTable(String tableName, String keyName, String keyValue, String valueName) throws SQLException{
		checkConnection();
		Statement statement = c.createStatement();
		ResultSet res = statement.executeQuery("SELECT * FROM "+tableName+" WHERE "+keyName+" = '" + keyValue + "';");
		res.next();
		Object found;
		try {
			found = null;
			if(res.getString(keyName) == null) {
				found = null;
			} else {
				found = res.getObject(valueName);
			}
		} catch (Exception e) {
			found = null;
		}
		res.close();
		statement.close();
		return found;
	}
	public synchronized void exec(String statement) throws SQLException{
		checkConnection();
		PreparedStatement placeStatement = c.prepareStatement(statement);
		placeStatement.executeUpdate();
		placeStatement.close();
	}
	public synchronized Boolean setInTable(String tableName, String keyName, String keyValue, String valueName, Object value) throws SQLException{
		checkConnection();
		String replace = "INSERT INTO "+tableName+" (`"+keyName+"`, `"+valueName+"`) VALUES (?, ?)"
				+ " ON DUPLICATE KEY UPDATE "+valueName+" = ?;";
		PreparedStatement placeStatement = c.prepareStatement(replace);
		placeStatement.setString(1, keyValue);
		placeStatement.setString(2, value+"");
		placeStatement.setString(3, value+"");
		placeStatement.executeUpdate();
		placeStatement.close();
		
		/*
		String replace = "REPLACE INTO "+tableName+" (`"+keyName+"`, `"+valueName+"`) VALUES (?, ?);";
		PreparedStatement placeStatement = c.prepareStatement(replace);
		placeStatement.setString(1, keyValue);
		placeStatement.setString(2, value+"");
		placeStatement.executeUpdate();
		placeStatement.close();
		*/
		return true;
	}
	public synchronized void createTable(String tableName, String[] columns, String[] types){
		checkConnection();
		String query = null;
		try {
			query = "CREATE TABLE IF NOT EXISTS "+tableName+"(";
			Boolean first = true;
			for(int i=0;i<columns.length&&i<types.length;i++){
				if(!first){
					query = query + ", ";
				}
				else{			
					first = false;
				}
				String col = columns[i];
				String type = types[i];
				query = query + col + " " + type;
			}
			query = query + ");";
			//Query is assembles
			Statement statement = c.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (SQLException e) {
			plugin.getLogger().info("Error in query:");
			plugin.getLogger().info("Query: "+query);
			plugin.getLogger().info("Error: "+e.getMessage());
		}
	}
	
	public synchronized void deleteFromTable(String tableName, String keyName, String keyValue) throws SQLException{
		checkConnection();
		String query = "DELETE FROM "+tableName+" WHERE ?=?;";
		PreparedStatement placeStatement = c.prepareStatement(query);
		placeStatement.setString(1, keyName);
		placeStatement.setString(2, keyValue);
		placeStatement.executeUpdate();
		placeStatement.close();
	}
	
	public synchronized List<Map<Object, Object>> getRows(String tableName, String keyName, String keyValue, String... columns) throws SQLException{
		checkConnection();
		String query = "SELECT * FROM "+tableName+" WHERE ?=?;";
		PreparedStatement placeStatement = c.prepareStatement(query);
		placeStatement.setString(1, keyName);
		placeStatement.setString(2, keyValue);
		ResultSet res = placeStatement.executeQuery();
		List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
		while(res.next()){
			try {
				Map<Object, Object> obs = new HashMap<Object, Object>();
				for(String col:columns){
					try {
						Object o = res.getObject(col);
						obs.put(col, o);
					} catch (Exception e) {
						continue;
					}
				}
				list.add(obs);
			} catch (Exception e) {
				continue;
			}
		}
		res.close();
		placeStatement.close();
		return list;
	}
	
	public synchronized List<Object> getColumn(String tableName, String column) throws SQLException{
		checkConnection();
		Statement statement = c.createStatement();
		ResultSet res = statement.executeQuery("SELECT * FROM "+tableName+";");
		List<Object> list = new ArrayList<Object>();
		while(res.next()){
			try {
				Object o = res.getObject(column);
				list.add(o);
			} catch (Exception e) {
				continue; //Error reading object
			}
		}
		res.close();
		statement.close();
		return list;
	}
}