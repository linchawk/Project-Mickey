package tech.behaviouring.pm.service;

import org.smslib.AGateway;
import org.smslib.IOutboundMessageNotification;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;

import tech.behaviouring.pm.core.applogic.PM_Event;
import tech.behaviouring.pm.core.applogic.PM_EventListener;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.util.EventLog;

/*
 * Created by Mohan on 24/2/2016
 */

public class SmsService {

	private static final String tag = "SMS Service";
	// Instance object for Singleton class. Lets make SmsService a
	// singleton
	private static SmsService instance = null;
	// Lock object for thread safety
	private static Object lock = new Object();
	private DBOperations db;
	private boolean cadSendSms;
	// The guy who is interested in SmsService events
	private PM_EventListener listener;

	private SmsService() {
		/*
		 * Make constructor to beat instantiation
		 */
		cadSendSms = false;
	}

	// Create the singleton object and return it

	public static SmsService getInstance() {
		synchronized (lock) {
			if (instance == null)
				instance = new SmsService();
			return instance;
		}
	}

	public void start() {
		init();
	}

	private void init() {

		try {
			db = DBOperations.getInstance();
			if (db.getValue("modem_setup_complete").equals("false"))
				return;

			OutboundNotification outboundNotification = new OutboundNotification();
			System.out.println("Starting sms service");
			SerialModemGateway gateway = new SerialModemGateway(db.getValue("modem_id"), db.getValue("modem_com_port"),
					115200, db.getValue("modem_manufacturer"), "");
			gateway.setInbound(true);
			gateway.setOutbound(true);
			gateway.setSimPin("0000");
			gateway.setSmscNumber(db.getValue("modem_sms_center"));
			Service.getInstance().setOutboundMessageNotification(outboundNotification);
			Service.getInstance().addGateway(gateway);
			Service.getInstance().startService();
			System.out.println("Sms service started");
			cadSendSms = true;

			// Let the interested guy know SmsService has been started
			if (listener != null)
				listener.eventOccured(PM_Event.EventType.SmsService_Started);

		} catch (Exception e) {
			cadSendSms = false;
			System.out.println("Sms service not available");
			EventLog.e(tag, e);
		}

	}

	public void addEventListener(PM_EventListener listener) {
		this.listener = listener;
	}

	public void sendSms(String ph, String msg) {
		if (!cadSendSms)
			return;
		try {
			OutboundMessage msg1 = new OutboundMessage(ph, msg);
			Service.getInstance().queueMessage(msg1);
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	private class OutboundNotification implements IOutboundMessageNotification {
		public void process(AGateway gateway, OutboundMessage msg) {
			System.out.println(msg);
		}
	}

}
