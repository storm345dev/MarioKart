package net.stormdev.mario.mariokart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import lib.husky.mysql.MySQL;

public class SQLManager {
	MySQL MySQL = null;
	Connection c = null;

	public SQLManager(String hostName, String port, String dbName,
			String username, String password) {
		MarioKart.logger.info("Connecting to mySQL database...");
		try {
			MySQL = new MySQL(MarioKart.plugin, hostName, port, dbName, username,
					password);
			c = MySQL.openConnection();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			MarioKart.logger.info("Error connecting to mySQL database!");
		}
	}
	
	public boolean isValid(){
		if (MySQL == null || c == null) {
			return false;
		}
		return true;
	}

	public synchronized void closeConnection() {
		try {
			c.close();
		} catch (SQLException e) {
			MarioKart.logger.info("Error occured when closing sql connection!");
		}
		if(MySQL != null){
			MySQL.closeConnection();
		}
		return;
	}

	public Object searchTable(String tableName, String keyName,
			String keyValue, String valueName) throws SQLException {
		Statement statement = c.createStatement();
		ResultSet res = statement.executeQuery("SELECT * FROM " + tableName
				+ " WHERE " + keyName + " = '" + keyValue + "';");
		res.next();
		Object found = null;
		if (res.getString(keyName) == null) {
			found = null;
		} else {
			found = res.getObject(valueName);
		}
		res.close();
		statement.close();
		return found;
	}

	public Map<String, Object> getFromTable(String tableName, String col1,
			String col2) throws SQLException {
		Statement statement = c.createStatement();
		ResultSet res = statement.executeQuery("SELECT " + col1 + "," + col2
				+ " FROM " + tableName + ";");
		HashMap<String, Object> results = new HashMap<String, Object>();
		while (res.next()) {
			Object found = res.getObject(col2);
			String key = res.getString(col1);
			if (key != null && found != null) {
				results.put(key, found);
			}
		}
		res.close();
		statement.close();
		return results;
	}

	public Map<String, String> getStringsFromTable(String tableName,
			String col1, String col2) throws SQLException {
		Statement statement = c.createStatement();
		ResultSet res = statement.executeQuery("SELECT " + col1 + "," + col2
				+ " FROM " + tableName + ";");
		HashMap<String, String> results = new HashMap<String, String>();
		while (res.next()) {
			String found = res.getString(col2);
			String key = res.getString(col1);
			if (key != null && found != null) {
				results.put(key, found);
			}
		}
		res.close();
		statement.close();
		return results;
	}

	public Boolean deleteFromTable(String tableName, String keyName,
			String keyValue, String valueName) throws SQLException {
		String del = "DELETE FROM " + tableName + " WHERE " + tableName + "."
				+ keyName + " = ?;";
		PreparedStatement delStatement = c.prepareStatement(del);
		delStatement.setString(1, keyValue);
		delStatement.executeUpdate();
		delStatement.close();
		return true;
	}

	public boolean setInTable(String tableName, String keyName, String keyValue, String valueName, Object value) throws SQLException{
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

	public void createTable(String tableName, String[] columns, String[] types) {
		String query = null;
		try {
			query = "CREATE TABLE IF NOT EXISTS " + tableName + "(";
			Boolean first = true;
			for (int i = 0; i < columns.length && i < types.length; i++) {
				if (!first) {
					query = query + ", ";
				} else {
					first = false;
				}
				String col = columns[i];
				String type = types[i];
				query = query + col + " " + type;
			}
			query = query + ");";
			// Query is assembles
			Statement statement = c.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (SQLException e) {
			MarioKart.logger.info(MarioKart.colors.getError() + "Query: " + query);
			MarioKart.logger.info(MarioKart.colors.getError() + "Error: "
					+ e.getMessage());
		}
	}
}
