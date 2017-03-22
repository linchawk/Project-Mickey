package tech.behaviouring.pm.core.database;

import java.sql.*;

import tech.behaviouring.pm.util.EventLog;
import tech.behaviouring.pm.util.Hash;

/*
 * Created by Mohan on 20/11/2015
 */

public class BaseTables {
	private final String tag = "BaseTables";
	private Connection connection;

	public BaseTables() {
		connection = DBConnection.getConnection();
		if (connection == null) {
			System.out.println(tag + ", " + "Could not connect to database. See exception details in log.");
		}
	}

	/*
	 * Create all base tables
	 */

	public boolean createAllBaseTables() {
		return (createTableGymPlans() && createTableMemberDetails() && createTableMemberFingerprints()
				&& createTablePaymentDetails() && createTableDailyAttendance() && createTableGeneralInfo());
	}

	/*
	 * Delete all base tables
	 */

	public boolean deleteAllBaseTables() {
		return (deleteTableGymPlans() && deleteTableMemberDetails() && deleteTableMemberFingerprints()
				&& deleteTablePaymentDetails() && deleteTableDailyAttendance() && deleteTableGeneralInfo());
	}

	/*
	 * Create table gym_plans in the database project_mickey
	 */

	public boolean createTableGymPlans() {
		String query = "CREATE TABLE IF NOT EXISTS `gym_plans` ( "
				+ "`plan_id` int(10) unsigned NOT NULL AUTO_INCREMENT,`plan_name` text NOT NULL,"
				+ "`plan_fee_1month` int(10) unsigned NOT NULL,`plan_fee_3month` int(10) unsigned NOT NULL,"
				+ "`plan_fee_6month` int(10) unsigned NOT NULL,`plan_fee_12month` int(10) unsigned NOT NULL,"
				+ "PRIMARY KEY (`plan_id`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;";
		return createTableByQuery("gym_plans", query);
	}

	/*
	 * Delete table gym_plans in the database project_mickey
	 */

	public boolean deleteTableGymPlans() {
		return deleteTableByName("gym_plans");
	}

	/*
	 * Create table member_details in the database project_mickey
	 */

	public boolean createTableMemberDetails() {
		String query = "CREATE TABLE IF NOT EXISTS `member_details` ( "
				+ "`mem_id` int(10) unsigned NOT NULL AUTO_INCREMENT,`mem_name` text NOT NULL,"
				+ "`mem_pic` text NOT NULL,`mem_dob` date NOT NULL,`mem_bloodgroup` text,"
				+ "`mem_height_cm` int(10) unsigned DEFAULT NULL,`mem_weight_kg` int(10) unsigned DEFAULT NULL,"
				+ "`mem_phone` text NOT NULL,`mem_email` text,`mem_addr` text NOT NULL,"
				+ "`mem_joining_date` date NOT NULL,`mem_plan_id` int(10) unsigned NOT NULL,"
				+ "`mem_fee_paid_for_nmonth` int(10) unsigned NOT NULL,`mem_next_renewal` date NOT NULL,"
				+ "`mem_isverified` tinyint(4) NOT NULL,`mem_last_seen_date` date NOT NULL,`mem_n_remainderssent` tinyint(4),PRIMARY KEY (`mem_id`)"
				+ " ) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;";
		return createTableByQuery("member_details", query);
	}

	/*
	 * Delete table member_details in the database project_mickey
	 */

	public boolean deleteTableMemberDetails() {
		return deleteTableByName("member_details");
	}

	/*
	 * Create table member_fingerprints in the database project_mickey
	 */

	public boolean createTableMemberFingerprints() {
		String query = "CREATE TABLE IF NOT EXISTS `member_fingerprints` ( "
				+ "`mem_id` int(10) unsigned NOT NULL,`template_thumb` mediumblob NOT NULL,"
				+ "`template_index` mediumblob NOT NULL,`mem_isactive` tinyint(4) NOT NULL,PRIMARY KEY (`mem_id`)"
				+ " ) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;";
		return createTableByQuery("member_fingerprints", query);
	}

	/*
	 * Delete table member_fingerprints in the database project_mickey
	 */

	public boolean deleteTableMemberFingerprints() {
		return deleteTableByName("member_fingerprints");
	}

	/*
	 * Create table payment_details in the database project_mickey
	 */

	public boolean createTablePaymentDetails() {
		String query = "CREATE TABLE IF NOT EXISTS `payment_details` ("
				+ "`payment_id` int(10) unsigned NOT NULL AUTO_INCREMENT,`mem_id` int(10) unsigned NOT NULL,"
				+ "`payment_amount` int(10) unsigned NOT NULL,`payment_date` date NOT NULL,"
				+ "PRIMARY KEY (`payment_id`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;";
		return createTableByQuery("payment_details", query);
	}

	/*
	 * Delete table payment_details in the database project_mickey
	 */

	public boolean deleteTablePaymentDetails() {
		return deleteTableByName("payment_details");
	}

	/*
	 * Create table daily_attendance in the database project_mickey
	 */

	public boolean createTableDailyAttendance() {
		String query = "CREATE TABLE IF NOT EXISTS `daily_attendance` (`att_date` date NOT NULL,"
				+ "`att_morning_comers` text NOT NULL,`att_morning_comers_count` int(10) unsigned NOT NULL,"
				+ "`att_evening_comers` text NOT NULL,`att_evening_comers_count` int(10) unsigned NOT NULL,"
				+ "PRIMARY KEY (`att_date`) ) ENGINE=MyISAM DEFAULT CHARSET=latin1;";
		return createTableByQuery("daily_attendance", query);
	}

	/*
	 * Delete table daily_attendance in the database project_mickey
	 */

	public boolean deleteTableDailyAttendance() {
		return deleteTableByName("daily_attendance");
	}

	/*
	 * Create table general_info in the database project_mickey
	 */

	public boolean createTableGeneralInfo() {
		String query = "CREATE TABLE IF NOT EXISTS `general_info` (`key` tinytext NOT NULL,"
				+ "`val` text NOT NULL) ENGINE=MyISAM DEFAULT CHARSET=latin1;";
		// First create general_info table and then fill it will default values
		return (createTableByQuery("general_info", query) && fillTableGeneralInfo());
	}

	/*
	 * Delete table general_info in the database project_mickey
	 */

	public boolean deleteTableGeneralInfo() {
		return deleteTableByName("general_info");
	}

	/*
	 * Fill the table general_info with default values
	 */

	private boolean fillTableGeneralInfo() {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing insert statements for general_info");
				return false;
			}
			PreparedStatement statement = connection
					.prepareStatement("INSERT INTO general_info (`key`, `val`) VALUES (?, ?)");

			// Add gym_name to the insert batch
			statement.setString(1, "gym_name");
			statement.setString(2, "Mickey");
			statement.addBatch();

			// Add gym_address to the insert batch
			statement.setString(1, "gym_addr");
			statement.setString(2, "Tolichowki");
			statement.addBatch();

			// Add admin_user to the insert batch
			statement.setString(1, "admin_usr");
			statement.setString(2, "admin");
			statement.addBatch();

			// Add admin_pwd hash to the insert batch
			String defaultPwd = "Tolichowki@123";
			Hash hash = new Hash(defaultPwd);
			statement.setString(1, "admin_pwd");
			statement.setString(2, hash.getMD5Hash());
			statement.addBatch();

			// Add admin_pwd hash to the insert batch
			statement.setString(1, "admission_fee");
			statement.setString(2, "200");
			statement.addBatch();

			// Whether initial setup is complete
			statement.setString(1, "general_setup_complete");
			statement.setString(2, "false");
			statement.addBatch();

			// Whether gym plans setup is complete
			statement.setString(1, "plans_setup_complete");
			statement.setString(2, "false");
			statement.addBatch();

			// GSM modem details
			statement.setString(1, "modem_id");
			statement.setString(2, "com.modem1");
			statement.addBatch();

			statement.setString(1, "modem_com_port");
			statement.setString(2, "COM4");
			statement.addBatch();

			statement.setString(1, "modem_manufacturer");
			statement.setString(2, "Huawei");
			statement.addBatch();

			statement.setString(1, "modem_sms_center");
			statement.setString(2, "");
			statement.addBatch();

			statement.setString(1, "modem_setup_complete");
			statement.setString(2, "false");
			statement.addBatch();

			// Welcome SMS
			statement.setString(1, "welcome_sms");
			statement.setString(2,
					"Dear %mem_name%, Thanks for joining %gym_name%. Train hard. Eat healthy. Sleep well. See a fitter you very soon.");
			statement.addBatch();

			// Payment reminder SMS
			statement.setString(1, "payment_remainder_sms");
			statement.setString(2,
					"Dear %mem_name%, Your payment of Rs. %pay_amount% for %plan_name% is due on %due_date%. Please pay soon. Train hard. Eat healthy. Sleep well. Thanks. -%gym_name%");
			statement.addBatch();

			// Payment acknowledgement SMS
			statement.setString(1, "payment_ack_sms");
			statement.setString(2,
					"Dear %mem_name%, Your payment of Rs. %pay_amount% for %plan_name% has been received on %payment_date%. Train hard. Eat healthy. Sleep well. Thanks. -%gym_name%");
			statement.addBatch();

			int[] results = statement.executeBatch();
			for (int count : results) {
				if (count < 0) {// If one of the counts in the array is -ve,
								// then
								// the corresponding insert was not success
					EventLog.e(tag, "Some insert statement general_info batch insert has failed");
					return false;
				}
			}
			// If we reached here all counts in results array are +ve and all
			// inserts were success
			return true;

		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Create a table by taking create statement
	 */

	private boolean createTableByQuery(String tableName, String query) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing create table for " + tableName);
				return false;
			}
			Statement statement = connection.createStatement();
			int numRows = statement.executeUpdate(query);
			if (numRows == 0) { // If numRows is 0, the table has been created
								// successfully
				return true;
			} else {
				EventLog.e(tag, "Table " + tableName + " not created successfully");
				return false;
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Delete a table by taking the table name
	 */

	private boolean deleteTableByName(String tableName) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing delete table for " + tableName);
				return false;
			}
			String query = "DROP TABLE IF EXISTS " + tableName;
			Statement statement = connection.createStatement();
			int numRows = statement.executeUpdate(query);
			if (numRows == 0) { // If numRows is 0, the table has been deleted
								// successfully
				return true;
			} else {
				EventLog.e(tag, "Table " + tableName + " not deleted successfully");
				return false;
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}
}
