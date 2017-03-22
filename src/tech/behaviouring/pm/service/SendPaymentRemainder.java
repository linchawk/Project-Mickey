package tech.behaviouring.pm.service;

import java.util.HashMap;
import java.util.List;

import tech.behaviouring.pm.core.applogic.objects.GymPlan;
import tech.behaviouring.pm.core.applogic.objects.MemberDetails;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.util.EventLog;

/*
 * Created by Mohan on 28/2/2016
 */

public class SendPaymentRemainder extends Thread {

	private static final String tag = "Payment Remainder";

	// Singleton instance of PaymentRemainder
	private static SendPaymentRemainder instance = null;
	// Lock object for thread safety
	private static Object lock = new Object();
	// DB object
	DBOperations db;
	// Gym name
	private String gymName;
	// Hash map of gym plans hashed by gym plan id
	HashMap<Integer, GymPlan> gymPlans;

	private SendPaymentRemainder() {
		/*
		 * Make constructor private to beat instantiation
		 */
	}

	// Create the singleton object and return it

	public static SendPaymentRemainder getInstance() {
		synchronized (lock) {
			if (instance == null)
				instance = new SendPaymentRemainder();
			return instance;
		}
	}

	public void run() {
		System.out.println("Payment remainder started");
		db = DBOperations.getInstance();
		gymName = db.getValue("gym_name");
		getGymPlans();
		sendPaymentReaminder();
	}

	// Since we will refer gymplan object for every member let's cache them
	// locally
	private void getGymPlans() {
		gymPlans = new HashMap<Integer, GymPlan>();
		for (GymPlan gymPlan : db.getAllGymPlans())
			gymPlans.put(gymPlan.getId(), gymPlan);
	}

	private void sendPaymentReaminder() {
		SmsService smsService = SmsService.getInstance();
		String paymentRemainderSms = db.getValue("payment_remainder_sms");
		// Get all members whose payment due tomorrow or day after tomorrow
		List<MemberDetails> members = db.getAllMembersByPaymentDueDate();

		for (MemberDetails member : members) {
			GymPlan memberGymPlan = gymPlans.get(member.getPlanId());
			String remainderSms = paymentRemainderSms.replace("%mem_name%", member.getName());
			remainderSms = remainderSms.replace("%pay_amount%", memberGymPlan.getFee1Month() + "");
			remainderSms = remainderSms.replace("%plan_name%", memberGymPlan.getName());
			remainderSms = remainderSms.replace("%due_date%", member.getNextRenewal() + "");
			remainderSms = remainderSms.replace("%gym_name%", gymName);
			smsService.sendSms(member.getPh(), remainderSms);

			// After sending sms update the number of payment remainders sent to
			// the member in
			// db
			member.setNRemainderSmsSent(member.getNRemainderSmsSent() + 1);
			db.updateMember(member);
			EventLog.e(tag, "Sent payment remainder sms to " + member.getName());
			try {
				// Don't send more than 3 sms per min as the GSM modem can't
				// handle more than 6 per min
				Thread.sleep(20 * 1000L);
			} catch (InterruptedException e) {
				EventLog.e(tag, e);
			}
		}
	}

}
