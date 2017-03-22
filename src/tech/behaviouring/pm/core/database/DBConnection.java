package tech.behaviouring.pm.core.database;

import java.sql.DriverManager;
import java.sql.Connection;
import tech.behaviouring.pm.util.EventLog;

/*
 * Created by Mohan on 18/11/15.
 */

public class DBConnection {
	/*
	 * Let's maintain a single connection instance throughout the app
	 */
	private static Connection connection = null;
	private static Object lock = new Object(); // Monitor for this class

	public static Connection getConnection() {
		// Synchronize the Connection object creation for thread safety
		synchronized (lock) {
			try {
				/*
				 * Create connection only if its null. Singleton >
				 */
				if (connection == null) {
					Class.forName("com.mysql.jdbc.Driver");
					connection = DriverManager.getConnection(
							"jdbc:mysql://localhost/project_mickey?" + "user=mickey&password=rockybalboa2008");
				}
				return connection;
			} catch (Exception e) {
				EventLog.e("DBConnection", e);
				return null;
			}
		}
	}

	public static void closeConnection() {

		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
		} catch (Exception e) {
			EventLog.e("DBConnection", e);
		}
	}
}
