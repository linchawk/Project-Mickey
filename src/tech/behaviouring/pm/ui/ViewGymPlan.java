package tech.behaviouring.pm.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.core.applogic.objects.GymPlan;
import tech.behaviouring.pm.ui.widgets.PM_Label;
import tech.behaviouring.pm.util.EventLog;

/*
 * Created by Mohan on 8/2/2015
 */

public class ViewGymPlan extends PM_Activity {

	private static final String tag = "View Gym Plan";

	// Whether this activity is up
	private static boolean isActive = false;

	private int BORDER_WIDTH = 25;
	private GymPlan gymPlan;

	public ViewGymPlan(GymPlan gymPlan) {
		isActive = true;
		this.gymPlan = gymPlan;
	}

	// Implement Runnable interface
	@Override
	public void run() {
		try {
			init();
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	private void init() {
		mainWindow.setTitle("Plan Details");
		PM_Label planName = new PM_Label("Plan Name", Font.BOLD);
		PM_Label planName1 = new PM_Label(gymPlan.getName());
		PM_Label fee1Month = new PM_Label("Fee for 1 month", Font.BOLD);
		PM_Label fee1Month1 = new PM_Label(gymPlan.getFee1Month() + "");
		PM_Label fee3Month = new PM_Label("Fee for 3 month", Font.BOLD);
		PM_Label fee3Month1 = new PM_Label(gymPlan.getFee3Month() + "");
		PM_Label fee6Month = new PM_Label("Fee for 6 month", Font.BOLD);
		PM_Label fee6Month1 = new PM_Label(gymPlan.getFee6Month() + "");
		PM_Label fee12Month = new PM_Label("Fee for 12 month", Font.BOLD);
		PM_Label fee12Month1 = new PM_Label(gymPlan.getFee12Month() + "");

		JPanel detailsPanel = new JPanel();
		detailsPanel.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		detailsPanel.setLayout(new GridLayout(5, 2));

		detailsPanel.add(planName);
		detailsPanel.add(planName1);
		detailsPanel.add(fee1Month);
		detailsPanel.add(fee1Month1);
		detailsPanel.add(fee3Month);
		detailsPanel.add(fee3Month1);
		detailsPanel.add(fee6Month);
		detailsPanel.add(fee6Month1);
		detailsPanel.add(fee12Month);
		detailsPanel.add(fee12Month1);
		mainWindow.add(detailsPanel, BorderLayout.CENTER);

		mainWindow.setMinimumSize(new Dimension(400, 300));
		mainWindow.addWindowListener(this);
		mainWindow.setLocationRelativeTo(null);
		mainWindow.pack();
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
