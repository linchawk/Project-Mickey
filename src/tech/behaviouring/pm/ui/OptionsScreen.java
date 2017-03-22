package tech.behaviouring.pm.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;

import javax.swing.*;

import tech.behaviouring.pm.ui.widgets.PM_ImageButton1;
import tech.behaviouring.pm.util.EventLog;

/*
 * Created by Mohan on 6/2/2016
 */

public class OptionsScreen extends PM_Activity {

	private static final String tag = "Option Screen";

	// Whether this activity is up
	private static boolean isActive = false;

	public OptionsScreen() {
		isActive = true;
	}

	// Implement Runnable method
	@Override
	public void run() {
		try {
			init();
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	private void init() {
		mainWindow.setTitle("Options");
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new GridLayout(4, 1));

		PM_ImageButton1 gymPlanManagement = new PM_ImageButton1("res/img/notepad.png", "Gym Plan Management",
				new Runnable() {
					@Override
					public void run() {
						if (!PlanManagement.isActive())
							EventQueue.invokeLater(new PlanManagement());
					}
				});

		gymPlanManagement.setAlignmentX(Component.LEFT_ALIGNMENT);

		PM_ImageButton1 membershipManagement = new PM_ImageButton1("res/img/group.png", "Membership Management",
				new Runnable() {
					@Override
					public void run() {
						if (!MemberManagement.isActive())
							EventQueue.invokeLater(new MemberManagement());
					}
				});
		membershipManagement.setAlignmentX(Component.LEFT_ALIGNMENT);

		PM_ImageButton1 generalSetup = new PM_ImageButton1("res/img/wizard.png", "General Setup", new Runnable() {
			@Override
			public void run() {
				if (!GeneralSetup.isActive())
					EventQueue.invokeLater(new GeneralSetup(true));
			}
		});
		generalSetup.setAlignmentX(Component.LEFT_ALIGNMENT);

		PM_ImageButton1 modemSetup = new PM_ImageButton1("res/img/satellite.png", "Modem Setup", new Runnable() {
			@Override
			public void run() {
				if (!ModemSetup.isActive())
					EventQueue.invokeLater(new ModemSetup(true));
			}
		});
		modemSetup.setAlignmentX(Component.LEFT_ALIGNMENT);

		optionsPanel.add(gymPlanManagement);
		optionsPanel.add(membershipManagement);
		optionsPanel.add(generalSetup);
		optionsPanel.add(modemSetup);
		mainWindow.add(optionsPanel);
		mainWindow.pack();
		// mainWindow.setMinimumSize(new Dimension(640, 480));
		mainWindow.addWindowListener(this);
		mainWindow.setResizable(false);
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setVisible(true);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		isActive = false;
	}

	@Override
	public void windowClosed(WindowEvent e) {
		isActive = false;
	}

	public static boolean isActive() {
		return isActive;
	}

}
