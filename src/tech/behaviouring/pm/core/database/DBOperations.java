package tech.behaviouring.pm.core.database;

import java.sql.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import tech.behaviouring.pm.core.applogic.objects.DailyAttendance;
import tech.behaviouring.pm.core.applogic.objects.Fingerprint;
import tech.behaviouring.pm.core.applogic.objects.FingerprintWrapper;
import tech.behaviouring.pm.core.applogic.objects.GymPlan;
import tech.behaviouring.pm.core.applogic.objects.MemberDetails;
import tech.behaviouring.pm.core.applogic.objects.PaymentDetails;
import tech.behaviouring.pm.core.applogic.objects.SingleFingerprint;
import tech.behaviouring.pm.hardware.fingerprintreader.FpCaptureListener.Finger;
import tech.behaviouring.pm.util.Calculate;
import tech.behaviouring.pm.util.Convert;
import tech.behaviouring.pm.util.EventLog;
import tech.behaviouring.pm.util.DataStructures.TimeOfDay;

/*
 * Created by Mohan on 24/11/2015
 * This class provides API for all database operations
 */

public class DBOperations {

	/*
	 * Maintain a single instance through out the app
	 */

	private static Object lock = new Object(); // Monitor for synchronization

	private Connection connection;
	private final String tag = "DBOperations";
	private static DBOperations DBOperationsInstance = null;

	/*
	 * Constructor is private to beat instantiation from outside the class
	 */

	private DBOperations() {
		/*
		 * dum dum dum
		 */
	}

	private void init() {
		connection = DBConnection.getConnection();
	}

	public static DBOperations getInstance() {
		synchronized (lock) { // Thread safety
			if (DBOperationsInstance == null) {
				DBOperationsInstance = new DBOperations();
				DBOperationsInstance.init();
			}
			return DBOperationsInstance;
		}
	}

	/*
	 * Gym plan management APIs
	 */

	/*
	 * Create a new gym plan
	 */

	public boolean createGymPlan(GymPlan plan) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing insert for gym_plan");
				return false;
			}
			String query = "INSERT INTO `gym_plans` "
					+ "(`plan_name`, `plan_fee_1month`, `plan_fee_3month`, `plan_fee_6month`, `plan_fee_12month`) "
					+ "VALUES (?, ?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, plan.getName());
			statement.setInt(2, plan.getFee1Month());
			statement.setInt(3, plan.getFee3Month());
			statement.setInt(4, plan.getFee6Month());
			statement.setInt(5, plan.getFee12Month());
			int numRows = statement.executeUpdate();
			if (numRows > 0) { // If numRows is greater than 0, the new plan was
								// created successfully in the table
				return true;
			} else {
				EventLog.e(tag, "New Gym Plan " + plan.getName() + " not created successfully");
				return false;
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Get a gym plan by plan id
	 */

	public GymPlan getGymPlanById(int planId) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select for gym_plan");
				return null;
			}
			String query = "SELECT * FROM `gym_plans` WHERE `plan_id` = " + planId;
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);
			GymPlan gymPlan = new GymPlan();
			while (rs.next()) {
				gymPlan.setId(rs.getInt("plan_id"));
				gymPlan.setName(rs.getString("plan_name"));
				gymPlan.setFee1Month(rs.getInt("plan_fee_1month"));
				gymPlan.setFee3Month(rs.getInt("plan_fee_3month"));
				gymPlan.setFee6Month(rs.getInt("plan_fee_6month"));
				gymPlan.setFee12Month(rs.getInt("plan_fee_12month"));
			}
			return gymPlan;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Get all gym plans
	 */

	public List<GymPlan> getAllGymPlans() {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select all for gym_plan");
				return null;
			}
			String query = "SELECT * FROM `gym_plans`";
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);
			List<GymPlan> gymPlans = new ArrayList<GymPlan>();

			while (rs.next()) {
				GymPlan gymPlan = new GymPlan();
				gymPlan.setId(rs.getInt("plan_id"));
				gymPlan.setName(rs.getString("plan_name"));
				gymPlan.setFee1Month(rs.getInt("plan_fee_1month"));
				gymPlan.setFee3Month(rs.getInt("plan_fee_3month"));
				gymPlan.setFee6Month(rs.getInt("plan_fee_6month"));
				gymPlan.setFee12Month(rs.getInt("plan_fee_12month"));
				gymPlans.add(gymPlan);
			}
			return gymPlans;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Update gym plan
	 */

	public boolean updateGymPlan(GymPlan plan) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing update for gym_plan");
				return false;
			}
			String query = "UPDATE `gym_plans` SET `plan_name` = ?,`plan_fee_1month` = ?,"
					+ "`plan_fee_3month` = ?,`plan_fee_6month` = ?,`plan_fee_12month` = ? WHERE `plan_id` = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, plan.getName());
			statement.setInt(2, plan.getFee1Month());
			statement.setInt(3, plan.getFee3Month());
			statement.setInt(4, plan.getFee6Month());
			statement.setInt(5, plan.getFee12Month());
			statement.setInt(6, plan.getId());
			System.out.println(statement.toString());
			int numRows = statement.executeUpdate();
			if (numRows >= 0) { // If numRows is greater than 0, the new plan
								// was
								// updated successfully in the table
				return true;
			} else {
				EventLog.e(tag, "Gym Plan " + plan.getName() + " not updated successfully");
				return false;
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Delete gym plan by plan_id
	 */

	public boolean deleteGymPlan(int planId) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing delete for gym_plan");
				return false;
			}
			String query = "DELETE FROM `gym_plans` WHERE `plan_id` = " + planId;
			Statement statement = connection.createStatement();
			int numRows = statement.executeUpdate(query);
			if (numRows > 0) { // If numRows is greater than 0, the plan was
								// deleted successfully from the table
				return true;
			} else {
				EventLog.e(tag, "Gym Plan " + planId + " not deleted successfully");
				return false;
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Membership management APIs
	 */

	/*
	 * Create a new member
	 */

	public boolean createMember(MemberDetails memberDetails) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing insert for member_details");
				return false;
			}
			// Begin transaction. Make sure rows are inserted in both
			// member_details and member_fingerprints tables
			connection.setAutoCommit(false);
			String query = "INSERT INTO `member_details` (`mem_name`, `mem_pic`, `mem_dob`, `mem_bloodgroup`, `mem_height_cm`, `mem_weight_kg`,"
					+ " `mem_phone`, `mem_email`, `mem_addr`, `mem_joining_date`, `mem_plan_id`, `mem_fee_paid_for_nmonth`, `mem_next_renewal`,"
					+ " `mem_isverified`, `mem_last_seen_date`, `mem_n_remainderssent`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, memberDetails.getName());
			statement.setString(2, memberDetails.getPicLocation());
			statement.setDate(3, Convert.javaDateToSqlDate(memberDetails.getDob()));
			statement.setString(4, memberDetails.getBloodGroup());
			statement.setInt(5, memberDetails.getHeightCm());
			statement.setInt(6, memberDetails.getWeightKg());
			statement.setString(7, memberDetails.getPh());
			statement.setString(8, memberDetails.getEmail());
			statement.setString(9, memberDetails.getAddr());
			statement.setDate(10, Convert.javaDateToSqlDate(memberDetails.getDateJoined()));
			statement.setInt(11, memberDetails.getPlanId());
			statement.setInt(12, memberDetails.getFeePaidForNMonth());
			statement.setDate(13, Convert.javaDateToSqlDate(memberDetails.getNextRenewal()));
			statement.setInt(14, memberDetails.getIsVerified());
			statement.setDate(15, Convert.javaDateToSqlDate(memberDetails.getLastSeenOn()));
			statement.setInt(16, memberDetails.getNRemainderSmsSent());
			int numRows = statement.executeUpdate();
			if (numRows < 0) { // If memId is less than 0, insert wasn't
								// successful
				connection.rollback();
				EventLog.e(tag, "New member " + memberDetails.getName() + " not created successfully");
				return false;
			} else {

				// Get the member id and insert the fingerprints into
				// member_fingerprints
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next()) {
					int memberId = resultSet.getInt(1);
					if (memberId < 0) {// Just be sure
						connection.rollback();
						EventLog.e(tag, "New member " + memberDetails.getName()
								+ " not created successfully. Member Id returned was " + memberId);
						return false;
					}
					memberDetails.setId(memberId);
					if (createFingerprint(memberDetails.getFingerprint())) {
						connection.commit(); // Commit both inserts
						// After creating the member make the first payment also
						PaymentDetails firstPayment = new PaymentDetails();
						firstPayment.setMemId(memberId);
						firstPayment.setPayAmount(memberDetails.getFirstPaymentAmount());
						firstPayment.setPayDate(new Date()); // Payment date is
																// today
						return createPayment(firstPayment);
					} else {
						connection.rollback();
						return false;
					}
				} else {
					connection.rollback();
					EventLog.e(tag, "New member " + memberDetails.getName()
							+ " not created successfully. Empty resultset for generated keys");
					return false;
				}
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Get total members count
	 */

	public int getMembersCount() {
		String query = "SELECT COUNT(*) AS count FROM `member_details`";
		return getCountByQuery(query);
	}

	/*
	 * Get a member detail by member id
	 */

	public MemberDetails getMemberById(int memId) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select for member_details");
				return null;
			}
			String query = "SELECT * FROM `member_details` WHERE `mem_id` = " + memId;
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);
			MemberDetails memberDetails = new MemberDetails();
			while (rs.next()) {
				memberDetails.setId(rs.getInt("mem_id"));
				memberDetails.setName(rs.getString("mem_name"));
				memberDetails.setPicLocation(rs.getString("mem_pic"));
				memberDetails.setDob(rs.getDate("mem_dob"));
				memberDetails.setBloodGroup(rs.getString("mem_bloodgroup"));
				memberDetails.setHeightCm(rs.getInt("mem_height_cm"));
				memberDetails.setWeightKg(rs.getInt("mem_weight_kg"));
				memberDetails.setPh(rs.getString("mem_phone"));
				memberDetails.setEmail(rs.getString("mem_email"));
				memberDetails.setAddr(rs.getString("mem_addr"));
				memberDetails.setDateJoined(rs.getDate("mem_joining_date"));
				memberDetails.setPlanId(rs.getInt("mem_plan_id"));
				memberDetails.setFeePaidForNMonth(rs.getInt("mem_fee_paid_for_nmonth"));
				memberDetails.setNextRenewal(rs.getDate("mem_next_renewal"));
				memberDetails.setIsVerified(rs.getInt("mem_isverified"));
				memberDetails.setLastSeenOn(rs.getDate("mem_last_seen_date"));
				memberDetails.setNRemainderSmsSent(rs.getInt("mem_n_remainderssent"));

			}
			return memberDetails;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Get all members
	 */

	public List<MemberDetails> getAllMembers(int offset, int limit) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select all for member_details");
				return null;
			}
			String query = "SELECT * FROM `member_details` ORDER BY `mem_name` ASC LIMIT " + offset + "," + limit;
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);
			List<MemberDetails> memberDetailsList = new ArrayList<MemberDetails>();
			while (rs.next()) {
				MemberDetails memberDetails = new MemberDetails();
				memberDetails.setId(rs.getInt("mem_id"));
				memberDetails.setName(rs.getString("mem_name"));
				memberDetails.setPicLocation(rs.getString("mem_pic"));
				memberDetails.setDob(rs.getDate("mem_dob"));
				memberDetails.setBloodGroup(rs.getString("mem_bloodgroup"));
				memberDetails.setHeightCm(rs.getInt("mem_height_cm"));
				memberDetails.setWeightKg(rs.getInt("mem_weight_kg"));
				memberDetails.setPh(rs.getString("mem_phone"));
				memberDetails.setEmail(rs.getString("mem_email"));
				memberDetails.setAddr(rs.getString("mem_addr"));
				memberDetails.setDateJoined(rs.getDate("mem_joining_date"));
				memberDetails.setPlanId(rs.getInt("mem_plan_id"));
				memberDetails.setFeePaidForNMonth(rs.getInt("mem_fee_paid_for_nmonth"));
				memberDetails.setNextRenewal(rs.getDate("mem_next_renewal"));
				memberDetails.setIsVerified(rs.getInt("mem_isverified"));
				memberDetails.setLastSeenOn(rs.getDate("mem_last_seen_date"));
				memberDetails.setNRemainderSmsSent(rs.getInt("mem_n_remainderssent"));
				memberDetailsList.add(memberDetails);

			}
			return memberDetailsList;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Get all members whose payment is due tomorrow or day after tomorrow
	 */

	public List<MemberDetails> getAllMembersByPaymentDueDate() {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select all for member_details");
				return null;
			}
			Date dateTomorrow = Calculate.addDaysToDate1(new Date(), 1);
			Date dateDayAfterTomorrow = Calculate.addDaysToDate1(dateTomorrow, 1);
			String query = "SELECT * FROM `member_details` WHERE (`mem_next_renewal` = ? OR `mem_next_renewal` =?) AND `mem_n_remainderssent` <= 2";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setDate(1, Convert.javaDateToSqlDate(dateTomorrow));
			statement.setDate(2, Convert.javaDateToSqlDate(dateDayAfterTomorrow));
			ResultSet rs = statement.executeQuery();
			List<MemberDetails> memberDetailsList = new ArrayList<MemberDetails>();
			while (rs.next()) {
				MemberDetails memberDetails = new MemberDetails();
				memberDetails.setId(rs.getInt("mem_id"));
				memberDetails.setName(rs.getString("mem_name"));
				memberDetails.setPicLocation(rs.getString("mem_pic"));
				memberDetails.setDob(rs.getDate("mem_dob"));
				memberDetails.setBloodGroup(rs.getString("mem_bloodgroup"));
				memberDetails.setHeightCm(rs.getInt("mem_height_cm"));
				memberDetails.setWeightKg(rs.getInt("mem_weight_kg"));
				memberDetails.setPh(rs.getString("mem_phone"));
				memberDetails.setEmail(rs.getString("mem_email"));
				memberDetails.setAddr(rs.getString("mem_addr"));
				memberDetails.setDateJoined(rs.getDate("mem_joining_date"));
				memberDetails.setPlanId(rs.getInt("mem_plan_id"));
				memberDetails.setFeePaidForNMonth(rs.getInt("mem_fee_paid_for_nmonth"));
				memberDetails.setNextRenewal(rs.getDate("mem_next_renewal"));
				memberDetails.setIsVerified(rs.getInt("mem_isverified"));
				memberDetails.setLastSeenOn(rs.getDate("mem_last_seen_date"));
				memberDetails.setNRemainderSmsSent(rs.getInt("mem_n_remainderssent"));
				memberDetailsList.add(memberDetails);

			}
			return memberDetailsList;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Update member details
	 */

	public boolean updateMember(MemberDetails memberDetails) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing update for member_details");
				return false;
			}
			String query = "UPDATE `member_details` SET `mem_name` = ?, `mem_pic` = ?, `mem_dob` = ?, `mem_bloodgroup` = ?, `mem_height_cm` = ?, `mem_weight_kg` = ?,"
					+ " `mem_phone` = ?, `mem_email` = ?, `mem_addr` = ?, `mem_joining_date` = ?, `mem_plan_id` = ?, `mem_fee_paid_for_nmonth` = ?, `mem_next_renewal` = ?,"
					+ " `mem_isverified` = ?, `mem_last_seen_date` = ?, `mem_n_remainderssent` = ? WHERE `mem_id` = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, memberDetails.getName());
			statement.setString(2, memberDetails.getPicLocation());
			statement.setDate(3, Convert.javaDateToSqlDate(memberDetails.getDob()));
			statement.setString(4, memberDetails.getBloodGroup());
			statement.setInt(5, memberDetails.getHeightCm());
			statement.setInt(6, memberDetails.getWeightKg());
			statement.setString(7, memberDetails.getPh());
			statement.setString(8, memberDetails.getEmail());
			statement.setString(9, memberDetails.getAddr());
			statement.setDate(10, Convert.javaDateToSqlDate(memberDetails.getDateJoined()));
			statement.setInt(11, memberDetails.getPlanId());
			statement.setInt(12, memberDetails.getFeePaidForNMonth());
			statement.setDate(13, Convert.javaDateToSqlDate(memberDetails.getNextRenewal()));
			statement.setInt(14, memberDetails.getIsVerified());
			statement.setDate(15, Convert.javaDateToSqlDate(memberDetails.getLastSeenOn()));
			statement.setInt(16, memberDetails.getNRemainderSmsSent());
			statement.setInt(17, memberDetails.getId());
			int numRows = statement.executeUpdate();
			if (numRows >= 0) { // If numRows is greater than or equals 0, the
								// member was
								// updated successfully in the table
				return true;
			} else {
				EventLog.e(tag, "Member " + memberDetails.getName() + " not updated successfully");
				return false;
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Delete member by member_id
	 */

	public boolean deleteMember(int memId) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing delete for member_details");
				return false;
			}
			// Delete the associated fingerprint first
			if (deleteFingerprint(memId))
				return false;
			String query = "DELETE FROM `member_details` WHERE `mem_id` = " + memId;
			Statement statement = connection.createStatement();
			int numRows = statement.executeUpdate(query);
			if (numRows > 0) { // If numRows is greater than 0, the member was
								// deleted successfully from the table
				return true;
			} else {
				EventLog.e(tag, "Member " + memId + " not deleted successfully");
				return false;
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Insert Fingerprints of a member
	 */

	private boolean createFingerprint(Fingerprint fingerprint) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing insert for member_fingerprints");
				return false;
			}
			String query = "INSERT INTO `member_fingerprints` (`mem_id`, `template_thumb`, `template_index`, `mem_isactive`) VALUES (?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, fingerprint.getMemberId());
			statement.setBytes(2, fingerprint.getFingerprintTemplate(Finger.Thumb));
			statement.setBytes(3, fingerprint.getFingerprintTemplate(Finger.Index));
			// Initially set isactive to true. Later if the member is not seen
			// for 30 days it will be set to false by StartupActions.class
			statement.setInt(4, 1);
			// System.out.println(statement.toString());
			int numRows = statement.executeUpdate();
			if (numRows < 0) { // If numRows is less than 0, insert wasn't
								// successful
				EventLog.e(tag,
						"Fingerprint for New member " + fingerprint.getMemberId() + " not created successfully");
				return false;
			}
			return true;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Delete Fingerprint by member_id
	 */

	private boolean deleteFingerprint(int memId) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing delete for member_fingerprints");
				return false;
			}
			String query = "DELETE FROM `member_fingerprints` WHERE `mem_id` = " + memId;
			Statement statement = connection.createStatement();
			int numRows = statement.executeUpdate(query);
			if (numRows > 0) { // If numRows is greater than 0, the fingerprint
								// was
								// deleted successfully from the table
				return true;
			} else {
				EventLog.e(tag, "Fingerprint for " + memId + " not deleted successfully");
				return false;
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Get Fingerprints of all members
	 */

	public FingerprintWrapper getAllFingerprints() {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select all for member_fingerprints");
				return null;
			}
			String query = "SELECT * FROM `member_fingerprints` WHERE `mem_isactive` = 1";
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);
			List<SingleFingerprint> thumbFpList = new ArrayList<SingleFingerprint>();
			List<SingleFingerprint> indexFpList = new ArrayList<SingleFingerprint>();
			while (rs.next()) {
				SingleFingerprint thumbFp = new SingleFingerprint();
				thumbFp.setMemberId(rs.getInt("mem_id"));
				thumbFp.setFpTemplate(rs.getBytes("template_thumb"));
				thumbFpList.add(thumbFp);

				SingleFingerprint indexFp = new SingleFingerprint();
				indexFp.setMemberId(rs.getInt("mem_id"));
				indexFp.setFpTemplate(rs.getBytes("template_index"));
				indexFpList.add(indexFp);
			}
			FingerprintWrapper fpWrapper = new FingerprintWrapper();
			fpWrapper.setFpTemplates(Finger.Thumb, thumbFpList);
			fpWrapper.setFpTemplates(Finger.Index, indexFpList);
			return fpWrapper;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Identify all members who were not seen for last 30 days and deactivate
	 * their fingerprints by setting mem_isactive = 0
	 */

	public void ignoreInactiveMembersFingerprints(Date lastSeenDate) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing ignore inactivemembers for member_fingerprints");
				return;
			}
			String query = "UPDATE `member_fingerprints` SET `mem_isactive` = 0 WHERE `mem_id` IN (SELECT `mem_id` FROM `member_details` WHERE `mem_last_seen_date` < ?)";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setDate(1, Convert.javaDateToSqlDate(lastSeenDate));
			int numRows = statement.executeUpdate();
			EventLog.e(tag, "De-activated fingerprint templates of " + numRows + " inactive members");
		} catch (Exception e) {
			EventLog.e(tag, e);
			return;
		}
	}

	/*
	 * Payment management APIs
	 */

	/*
	 * Create new payment
	 */

	public boolean createPayment(PaymentDetails paymentDetails) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing insert for payment_details");
				return false;
			}
			String query = "INSERT INTO `payment_details` (`mem_id`, `payment_amount`, `payment_date`) "
					+ "VALUES (?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, paymentDetails.getMemId());
			statement.setInt(2, paymentDetails.getPayAmount());
			statement.setDate(3, Convert.javaDateToSqlDate(paymentDetails.getPayDate()));
			int numRows = statement.executeUpdate();
			if (numRows > 0) { // If numRows is greater than 0, the payment
								// record
								// was
								// created successfully in the table
				return true;
			} else {
				EventLog.e(tag, "Payment for member " + paymentDetails.getMemId() + " not created successfully");
				return false;
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Get payment made by payment_id
	 */

	public PaymentDetails getPaymentDetailsByPaymentId(int payId) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select for payment_details");
				return null;
			}
			String query = "SELECT * from `payment_details` WHERE `payment_id` = " + payId;
			Statement statement = connection.createStatement();
			PaymentDetails paymentDetails = new PaymentDetails();
			ResultSet rs = statement.executeQuery(query);

			while (rs.next()) {
				paymentDetails.setPayId(rs.getInt("payment_id"));
				paymentDetails.setMemId(rs.getInt("mem_id"));
				paymentDetails.setPayAmount(rs.getInt("payment_amount"));
				paymentDetails.setPayDate(rs.getDate("payment_date"));
			}
			return paymentDetails;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Get list of payment made by mem_id
	 */

	public List<PaymentDetails> getPaymentDetailsByMemberId(int memId) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select for payment_details");
				return null;
			}
			String query = "SELECT * from `payment_details` WHERE `mem_id` = " + memId;
			Statement statement = connection.createStatement();
			List<PaymentDetails> paymentDetailsList = new ArrayList<PaymentDetails>();
			ResultSet rs = statement.executeQuery(query);

			while (rs.next()) {
				PaymentDetails paymentDetails = new PaymentDetails();
				paymentDetails.setPayId(rs.getInt("payment_id"));
				paymentDetails.setMemId(rs.getInt("mem_id"));
				paymentDetails.setPayAmount(rs.getInt("payment_amount"));
				paymentDetails.setPayDate(rs.getDate("payment_date"));
				paymentDetailsList.add(paymentDetails);
			}
			return paymentDetailsList;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Get list of payments made in given date range
	 */

	public List<PaymentDetails> getPaymentDetailsInDateRange(Date dateStart, Date dateEnd, int offset, int limit) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select for payment_details");
				return null;
			}
			String query = "SELECT * from `payment_details` WHERE `payment_date` >= ? AND `payment_date` <= ? ORDER BY `payment_id` DESC LIMIT "
					+ offset + "," + limit;
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setDate(1, Convert.javaDateToSqlDate(dateStart));
			statement.setDate(2, Convert.javaDateToSqlDate(dateEnd));
			List<PaymentDetails> paymentDetailsList = new ArrayList<PaymentDetails>();
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				PaymentDetails paymentDetails = new PaymentDetails();
				paymentDetails.setPayId(rs.getInt("payment_id"));
				paymentDetails.setMemId(rs.getInt("mem_id"));
				paymentDetails.setPayAmount(rs.getInt("payment_amount"));
				paymentDetails.setPayDate(rs.getDate("payment_date"));
				paymentDetailsList.add(paymentDetails);
			}
			return paymentDetailsList;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Get count of payments made in given date range
	 */

	public int getPaymentCountInDateRange(Date dateStart, Date dateEnd) {
		if (connection == null) {
			EventLog.e(tag, "Connection object null. Not executing count for payment_details");
			return -1;
		}
		String query = "SELECT COUNT(*) AS count from `payment_details` WHERE `payment_date` >= ? AND `payment_date` <= ?";
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			statement.setDate(1, Convert.javaDateToSqlDate(dateStart));
			statement.setDate(2, Convert.javaDateToSqlDate(dateEnd));
			return getCountByQuery(statement);
		} catch (SQLException e) {
			EventLog.e(tag, e);
			return -1;
		}
	}

	/*
	 * Get list of payments made after given date
	 */

	public List<PaymentDetails> getPaymentDetailsAfterDate(Date date, int offset, int limit) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select for payment_details");
				return null;
			}
			String query = "SELECT * from `payment_details` WHERE `payment_date` >= ? ORDER BY `payment_id` DESC LIMIT "
					+ offset + "," + limit;
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setDate(1, Convert.javaDateToSqlDate(date));
			List<PaymentDetails> paymentDetailsList = new ArrayList<PaymentDetails>();
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				PaymentDetails paymentDetails = new PaymentDetails();
				paymentDetails.setPayId(rs.getInt("payment_id"));
				paymentDetails.setMemId(rs.getInt("mem_id"));
				paymentDetails.setPayAmount(rs.getInt("payment_amount"));
				paymentDetails.setPayDate(rs.getDate("payment_date"));
				paymentDetailsList.add(paymentDetails);
			}
			return paymentDetailsList;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Get total amount paid after a given date
	 */

	public int getTotalPaymentAfterDate(Date date) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select for payment_details");
				return 0;
			}
			String query = "SELECT SUM(`payment_amount`) as total_amount from `payment_details` WHERE `payment_date` >= ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setDate(1, Convert.javaDateToSqlDate(date));
			ResultSet rs = statement.executeQuery();
			int totalAmount = 0;
			while (rs.next()) {
				totalAmount = rs.getInt("total_amount");
			}
			return totalAmount;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return 0;
		}
	}

	/*
	 * Create new attendance record
	 */

	private boolean createAttendanceForDay(DailyAttendance dailyAttendance) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing insert for daily_attendance");
				return false;
			}
			String query = "INSERT INTO `daily_attendance` (`att_date`, `att_morning_comers`, `att_morning_comers_count`, "
					+ "`att_evening_comers`, `att_evening_comers_count`) " + "VALUES (?, ?, ?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setDate(1, Convert.javaDateToSqlDate(dailyAttendance.getDate()));
			statement.setString(2, dailyAttendance.getAttendees(TimeOfDay.Morning));
			statement.setInt(3, dailyAttendance.getNAttendees(TimeOfDay.Morning));
			statement.setString(4, dailyAttendance.getAttendees(TimeOfDay.Evening));
			statement.setInt(5, dailyAttendance.getNAttendees(TimeOfDay.Evening));
			int numRows = statement.executeUpdate();
			if (numRows > 0) { // If numRows is greater than 0, the attendance
								// record
								// was
								// created successfully in the table
				return true;
			} else {
				EventLog.e(tag,
						"Attendance record for date " + dailyAttendance.getDate() + " not created successfully");
				return false;
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Update attendance record by Date
	 */

	public boolean updateAttendanceForDay(DailyAttendance dailyAttendance) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing update for daily_attendance");
				return false;
			}
			String query = "UPDATE `daily_attendance` SET `att_morning_comers` = ?, `att_morning_comers_count` = ?,"
					+ "`att_evening_comers` = ?, `att_evening_comers_count` = ? WHERE `att_date` = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, dailyAttendance.getAttendees(TimeOfDay.Morning));
			statement.setInt(2, dailyAttendance.getNAttendees(TimeOfDay.Morning));
			statement.setString(3, dailyAttendance.getAttendees(TimeOfDay.Evening));
			statement.setInt(4, dailyAttendance.getNAttendees(TimeOfDay.Evening));
			statement.setDate(5, Convert.javaDateToSqlDate(dailyAttendance.getDate()));
			int numRows = statement.executeUpdate();
			if (numRows >= 0) { // If numRows is greater than 0, the attendance
								// record
								// was
								// updated successfully in the table
				return true;
			} else {
				EventLog.e(tag,
						"Attendance record for date " + dailyAttendance.getDate() + " not created successfully");
				return false;
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

	/*
	 * Get attendance record for a date
	 */

	public DailyAttendance getAttendanceForDate(Date date) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select for daily_attendance");
				return null;
			}
			String query = "SELECT * from `daily_attendance` WHERE `att_date` = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setDate(1, Convert.javaDateToSqlDate(date));
			DailyAttendance dailyAttendance = new DailyAttendance();
			ResultSet rs = statement.executeQuery();

			boolean isResultSetEmpty = true;

			while (rs.next()) {
				dailyAttendance.setDate(rs.getDate("att_date"));
				dailyAttendance.setAttendees(rs.getString("att_morning_comers"), TimeOfDay.Morning);
				dailyAttendance.setNAttendees(rs.getInt("att_morning_comers_count"), TimeOfDay.Morning);
				dailyAttendance.setAttendees(rs.getString("att_evening_comers"), TimeOfDay.Evening);
				dailyAttendance.setNAttendees(rs.getInt("att_evening_comers_count"), TimeOfDay.Evening);
				isResultSetEmpty = false;
			}

			if (isResultSetEmpty) {
				// We have to create today's attendance row first and set all
				// counts to zero
				createAttendanceForDay(dailyAttendance);
			}

			return dailyAttendance;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Get attendance records after on or after a date
	 */

	public List<DailyAttendance> getAttendanceAfterDate(Date date) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select for daily_attendance");
				return null;
			}
			String query = "SELECT * from `daily_attendance` WHERE `att_date` >= ? ORDER BY `att_date` ASC";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setDate(1, Convert.javaDateToSqlDate(date));
			List<DailyAttendance> dailyAttendanceList = new ArrayList<DailyAttendance>();
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				DailyAttendance dailyAttendance = new DailyAttendance();
				dailyAttendance.setDate(rs.getDate("att_date"));
				dailyAttendance.setAttendees(rs.getString("att_morning_comers"), TimeOfDay.Morning);
				dailyAttendance.setNAttendees(rs.getInt("att_morning_comers_count"), TimeOfDay.Morning);
				dailyAttendance.setAttendees(rs.getString("att_evening_comers"), TimeOfDay.Evening);
				dailyAttendance.setNAttendees(rs.getInt("att_evening_comers_count"), TimeOfDay.Evening);
				dailyAttendanceList.add(dailyAttendance);
			}
			return dailyAttendanceList;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Other DB APIs
	 */

	/*
	 * Get count by query
	 */

	private int getCountByQuery(String query) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select count for query: " + query);
				return -1;
			}
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);
			int totalRecords = -1;
			while (rs.next()) {
				totalRecords = rs.getInt("count");
			}
			return totalRecords;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return -1;
		}
	}

	private int getCountByQuery(PreparedStatement statement) {
		try {
			ResultSet rs = statement.executeQuery();
			int totalRecords = -1;
			while (rs.next()) {
				totalRecords = rs.getInt("count");
			}
			return totalRecords;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return -1;
		}
	}

	/*
	 * Get value by name from general_info table
	 */

	public String getValue(String name) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not executing select for general_info");
				return null;
			}
			String query = "SELECT val from `general_info` WHERE `key` = ?;";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, name);
			ResultSet rs = statement.executeQuery();
			String value = null;
			while (rs.next()) {
				value = rs.getString("val");
			}
			return value;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return null;
		}
	}

	/*
	 * Update value by name in general_info table
	 */

	public boolean setValue(String name, String val) {
		try {
			if (connection == null) {
				EventLog.e(tag, "Connection object null. Not update for general_info");
				return false;
			}
			String query = "UPDATE `general_info` SET `val` = ? WHERE `key` = ?;";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, val);
			statement.setString(2, name);
			int nRowsAffected = statement.executeUpdate();
			if (nRowsAffected >= 0)
				return true;
			else
				return false;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}
	}

}
