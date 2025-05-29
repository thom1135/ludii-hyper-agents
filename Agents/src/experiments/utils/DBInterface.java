package experiments.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInterface {

	final String dbURL = System.getenv("DB_URL");
	final String dbUser = System.getenv("DB_USER");
	final String dbPass = System.getenv("DB_PASS");

	Connection connection;
	Statement statement;
	ResultSet resultSet;

	DBInterface() {
		createConnection();
	}

	public ResultSet retrieveRecords(String query) {

		try {
			if (statement == null) {
				createConnection();
			}
			if (statement != null) {
				resultSet = statement.executeQuery(query);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return resultSet;
	}

	public boolean insertRecords(String query) {

		boolean success = false;

		try {
			if (statement == null) {
				createConnection();
			}
			if (statement != null) {
				success = statement.executeUpdate(query) == 1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return success;
	}

	private void createConnection() {

		try {
			this.connection = DriverManager.getConnection(dbURL, dbUser, dbPass);

			if (connection != null) {
				this.statement = connection.createStatement();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		try {
			if (resultSet != null && !resultSet.isClosed())
				resultSet.close();
			if (statement != null && !statement.isClosed())
				statement.close();
			if (connection != null && !connection.isClosed())
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
