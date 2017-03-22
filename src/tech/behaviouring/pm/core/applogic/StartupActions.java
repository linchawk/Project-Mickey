package tech.behaviouring.pm.core.applogic;

import java.awt.EventQueue;
import java.util.Date;

import javax.swing.JOptionPane;

import Cogent.IDeviceConnected;
import tech.behaviouring.pm.core.applogic.PM_Event.EventType;
import tech.behaviouring.pm.core.database.BaseTables;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.hardware.fingerprintreader.FpTemplatesLoadListener;
import tech.behaviouring.pm.hardware.fingerprintreader.InitFpReader;
import tech.behaviouring.pm.hardware.fingerprintreader.LoadFingerprintTemplates;
import tech.behaviouring.pm.service.SendPaymentRemainder;
import tech.behaviouring.pm.service.SmsService;
import tech.behaviouring.pm.ui.IdentifyMember;
import tech.behaviouring.pm.ui.Splash;
import tech.behaviouring.pm.util.Calculate;
import tech.behaviouring.pm.util.EventLog;
import tech.behaviouring.pm.util.FontLoader;

public class StartupActions {

	private static final String tag = "Starup Actions";

	private static DBOperations db;

	public static void start() {
		// Initialize the event logger. First thing to do in the app
		EventLog.init();

		// Acquire the file lock. If we cannot acquire the file, then another
		// instance of our app must be runnning. So just exit
		if (!FileLock.acquireLock()) {
			showErrorMsg();
			System.exit(0);
		}

		// Init DB API
		db = DBOperations.getInstance();

		// Show the splash screen
		if (areBaseTablesCreated()) {
			EventQueue.invokeLater(new Splash());

			// Load OpenSans font in our app
			FontLoader.loadFont();

			// Initialize the fingerprint hardware
			intiFpReader();

			// De-activate inactive members' fingerprints
			deactivateInactiveMembers();

			// Load fingerprint templates
			// loadFingerprintTemplates();

			// Start sms service
			startSmsService();
		} else
			// If we are not going to start the app, release the file lock we
			// acquired previously
			FileLock.releaseLock();

	}

	private static boolean areBaseTablesCreated() {
		String generalSetupComplete = db.getValue("general_setup_complete");
		if (generalSetupComplete == null) {
			System.out.println("Base tables not set up yet");
			BaseTables baseTables = new BaseTables();
			boolean baseTablesCreation = baseTables.createAllBaseTables();
			// All base tables created successfully. Get general info inputs
			if (baseTablesCreation) {
				return true;
			} else {
				System.out.println("Could not create database tables");
				return false;
			}
		} else {
			return true;
		}
	}

	private static void showErrorMsg() {
		JOptionPane.showMessageDialog(null, "Another instance of Mickey is already running on this system.");
	}

	private static void intiFpReader() {
		// Initialize the Fingerprint Reader asyncly
		Thread initFpReader = new Thread(new InitFpReader(new IDeviceConnected() {

			@Override
			public void onDeviceIsConnected(boolean isConnected) {
				if (isConnected)
					System.out.println("Fingerprint Reader connected");
				else
					System.out.println("Fingerprint Reader disconnected");
			}

		}));
		initFpReader.start();
	}

	private static void deactivateInactiveMembers() {
		// Ignore the fingerprints of members who were not seen in last 365
		// days.
		// They are real pain in the ass
		Date oneYearInPastFromToday = Calculate.addDaysToDate1(new Date(), -365);
		db.ignoreInactiveMembersFingerprints(oneYearInPastFromToday);
	}

	private static void loadFingerprintTemplates() {
		// Finally load all the fingerprint templates from database into our app
		Thread fingerPrintLoader = new Thread(new LoadFingerprintTemplates(new FpTemplatesLoadListener() {
			@Override
			public void onFpTemplatesLoaded(int nFingerprintTemplates) {
				/*
				 * When member fingerprint templates are loaded into our app, we
				 * are ready to identify members. So launch IdentifyMember
				 * window
				 */
				if (nFingerprintTemplates > 0)
					EventQueue.invokeLater(new IdentifyMember());
			}
		}));
		fingerPrintLoader.start();
	}

	private static void startSmsService() {

		// Create SmsService singleton object and start sms service
		SmsService smsService = SmsService.getInstance();

		smsService.addEventListener(new PM_EventListener() {
			// When sms service is started start PaymentRemainder service
			@Override
			public void eventOccured(EventType type) {
				EventLog.e(tag, "Starting Payment Remainder service");
				SendPaymentRemainder.getInstance().start();
			}

		});
		smsService.start();
	}

}
