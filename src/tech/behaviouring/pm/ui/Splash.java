package tech.behaviouring.pm.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.util.EventLog;

/*
 * Created by Mohan on 1/12/2015
 */
public class Splash extends PM_Activity {

	private String tag = "Splash";
	private JLabel splashImageLabel;
	private ImageIcon splashImageIcon;

	public Splash() {
		/*
		 * dum dum dum
		 */
	}

	@Override
	public void run() {
		try {
			initSplash();
			showSplash();
			hideSplash();
		} catch (Exception ex) {
			EventLog.e(tag, "Exception in splash screen");
			EventLog.e(tag, ex);
		}
	}

	private void initSplash() {
		EventLog.e(tag, "Showing splash screen");
		splashImageIcon = new ImageIcon("res/img/splash.png");
		splashImageLabel = new JLabel(splashImageIcon);
	}

	private void showSplash() {
		mainWindow.setUndecorated(true);
		mainWindow.getContentPane().add(splashImageLabel);
		mainWindow.pack();
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setVisible(true);
	}

	private void hideSplash() {
		// Wait for 5 secs before hiding splash screen
		final Timer timer = new Timer(1000 * 5, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DBOperations db = DBOperations.getInstance();
				String generalSetupComplete = db.getValue("general_setup_complete");
				mainWindow.dispose();
				if (generalSetupComplete.equals("false")) {
					// Show general setup window to get inputs for the first
					// time
					EventQueue.invokeLater(new GeneralSetup(false));
					System.out.println("General set up not complete yet");
				} else {
					System.out.println("General set up completed");
					EventQueue.invokeLater(new LandingScreen()); // Show landing
					// screen
				}
			}
		});
		timer.setRepeats(false);
		timer.start();
	}
}
