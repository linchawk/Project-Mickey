package tech.behaviouring.pm.ui;

import tech.behaviouring.pm.core.applogic.FileLock;
import tech.behaviouring.pm.core.applogic.PM_Event.EventType;
import tech.behaviouring.pm.core.applogic.PM_EventListener;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.hardware.fingerprintreader.FpTemplatesLoadListener;
import tech.behaviouring.pm.hardware.fingerprintreader.LoadFingerprintTemplates;
import tech.behaviouring.pm.ui.widgets.*;
import tech.behaviouring.pm.util.EventLog;

import java.awt.*;
import java.awt.event.WindowEvent;

import javax.swing.*;

/*
 * Created by Mohan on 3/12/2015
 */

public class LandingScreen extends PM_Activity implements PM_EventListener {

	private final String tag = "Landing Screen";

	private PM_ImageButton newMember;
	private PM_ImageButton fpScanner;
	private PM_ImageButton reports;
	private PM_ImageButton options;
	private Color bgColor; // Normal bg color of the frame
	private IdentifyMember identifyMember;
	private DBOperations db;

	public LandingScreen() {
		identifyMember = null;
	}

	@Override
	public void run() {
		try {
			db = DBOperations.getInstance();
			String plansSetupComplete = db.getValue("plans_setup_complete");
			// If gym plans are not set up yet, ask the user to set them up
			// first
			if (plansSetupComplete.equals("false")) {
				EventQueue.invokeLater(new GymPlanSetup(null));
			} else {
				init();
			}
		} catch (Exception ex) {
			EventLog.e(tag, "Exception in landing screen");
			EventLog.e(tag, ex);
		}
	}

	private void init() {
		bgColor = new Color(0xF5, 0xF5, 0xF5);
		newMember = new PM_ImageButton("res/img/id.png", "Add new member", new Runnable() {

			@Override
			public void run() {
				System.out.println(AddNewMember.isActive());
				if (!AddNewMember.isActive()) {
					EventQueue.invokeLater(new AddNewMember(null, LandingScreen.this));
				}
			}

		});
		fpScanner = new PM_ImageButton("res/img/fingerprint.png", "Fingerprint Scanner", new Runnable() {

			@Override
			public void run() {
				startMemberIdentification();
			}

		});
		reports = new PM_ImageButton("res/img/stats_bars.png", "Reports", new Runnable() {

			@Override
			public void run() {
				System.out.println(ReportsHome.isActive());
				if (!ReportsHome.isActive()) {
					EventQueue.invokeLater(new ReportsHome());
				}
			}

		});
		options = new PM_ImageButton("res/img/gear.png", "Options", new Runnable() {

			@Override
			public void run() {
				System.out.println(OptionsScreen.isActive());
				if (!OptionsScreen.isActive()) {
					EventQueue.invokeLater(new OptionsScreen());
				}
			}

		});

		DBOperations db = DBOperations.getInstance();
		String gymName = db.getValue("gym_name"); // Get gym name from database
		if (gymName == null)
			gymName = "Mickey - For fitness temples";

		mainWindow.setTitle(gymName);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setBackground(bgColor);
		mainWindow.setLayout(new GridLayout(2, 2));
		mainWindow.getContentPane().add(newMember);
		mainWindow.getContentPane().add(fpScanner);
		mainWindow.getContentPane().add(reports);
		mainWindow.getContentPane().add(options);
		mainWindow.setResizable(false);
		mainWindow.addWindowListener(this);
		mainWindow.pack();
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setVisible(true);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// Release the file lock when the app exits
		FileLock.releaseLock();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		FileLock.releaseLock();
	}

	@Override
	public void eventOccured(EventType type) {
		if (type == EventType.Member_Created) {
			// Re-create identification window, so that new member's fingerprint
			// is included and FpReader instance is re-claimed by IdentifyWorker
			// frame from Add new Member frame
			if (identifyMember != null) {
				identifyMember.die();
				System.out.println("Restarting Identify Member");
				startMemberIdentification();
			}
		}

	}

	private void startMemberIdentification() {
		System.out.println(IdentifyMember.isActive());
		if (!IdentifyMember.isActive()) {
			Thread fingerPrintLoader = new Thread(new LoadFingerprintTemplates(new FpTemplatesLoadListener() {
				@Override
				public void onFpTemplatesLoaded(int nFingerprintTemplates) {
					/*
					 * When member fingerprint templates are loaded into our
					 * app, we are ready to identify members. So launch
					 * IdentifyMember window
					 */
					if (nFingerprintTemplates > 0) {
						identifyMember = new IdentifyMember();
						EventQueue.invokeLater(identifyMember);
					}
				}
			}));
			fingerPrintLoader.start();
		}
	}
}
