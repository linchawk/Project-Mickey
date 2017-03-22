package tech.behaviouring.pm.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.core.applogic.objects.MemberDetails;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.ui.widgets.PM_EmptyLabel;
import tech.behaviouring.pm.ui.widgets.PM_Label;
import tech.behaviouring.pm.util.EventLog;

/*
 * Created by Mohan on 15/2/2016
 */

public class ViewMemberDetails extends PM_Activity {

	private static final String tag = "View Member Details";

	// Whether this activity is up
	private static boolean isActive = false;

	private final int BORDER_WIDTH = 20;
	private MemberDetails md;
	private DBOperations db;

	public ViewMemberDetails(MemberDetails md) {
		isActive = true;
		this.md = md;
	}

	@Override
	public void run() {
		try {
			db = DBOperations.getInstance();
			init();
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	private void init() {
		mainWindow.setTitle("Member Details");
		JLabel memPhoto = new JLabel(new ImageIcon(md.getPicLocation()));
		memPhoto.setBorder(new EmptyBorder(0, BORDER_WIDTH, 0, BORDER_WIDTH));
		mainWindow.add(memPhoto, BorderLayout.WEST);

		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

		JPanel generalDetailsPanel = new JPanel();
		generalDetailsPanel.setLayout(new GridLayout(5, 2));
		generalDetailsPanel.add(new PM_Label("Name", Font.BOLD));
		generalDetailsPanel.add(new PM_Label(md.getName()));
		generalDetailsPanel.add(new PM_Label("Phone", Font.BOLD));
		generalDetailsPanel.add(new PM_Label(md.getPh()));
		generalDetailsPanel.add(new PM_Label("Email", Font.BOLD));
		generalDetailsPanel.add(new PM_Label(md.getEmail()));
		generalDetailsPanel.add(new PM_Label("Date of Birth", Font.BOLD));
		generalDetailsPanel.add(new PM_Label(md.getDob() + ""));
		generalDetailsPanel.add(new PM_Label("Address", Font.BOLD));
		generalDetailsPanel.add(new PM_Label(md.getAddr()));

		JPanel physicalDetailsPanel = new JPanel();
		physicalDetailsPanel.setLayout(new GridLayout(3, 2));
		physicalDetailsPanel.add(new PM_Label("Height", Font.BOLD));
		physicalDetailsPanel.add(new PM_Label(md.getHeightCm() + ""));
		physicalDetailsPanel.add(new PM_Label("Weight", Font.BOLD));
		physicalDetailsPanel.add(new PM_Label(md.getWeightKg() + ""));
		physicalDetailsPanel.add(new PM_Label("Blood Group", Font.BOLD));
		physicalDetailsPanel.add(new PM_Label(md.getBloodGroup()));

		JPanel planDetailsPanel = new JPanel();
		planDetailsPanel.setLayout(new GridLayout(4, 2));
		planDetailsPanel.add(new PM_Label("Date of Joining", Font.BOLD));
		planDetailsPanel.add(new PM_Label(md.getDateJoined() + ""));
		planDetailsPanel.add(new PM_Label("Plan Name", Font.BOLD));
		planDetailsPanel.add(new PM_Label(db.getGymPlanById(md.getPlanId()).getName()));
		planDetailsPanel.add(new PM_Label("No. of Months", Font.BOLD));
		planDetailsPanel.add(new PM_Label(md.getFeePaidForNMonth() + ""));
		planDetailsPanel.add(new PM_Label("Next Renewal Date          ", Font.BOLD));
		planDetailsPanel.add(new PM_Label(md.getNextRenewal() + ""));

		detailsPanel.add(new PM_Label("General Details", Font.BOLD));
		detailsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		detailsPanel.add(new JSeparator());
		detailsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		detailsPanel.add(generalDetailsPanel);
		detailsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));

		detailsPanel.add(new PM_Label("Physical Details", Font.BOLD));
		detailsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		detailsPanel.add(new JSeparator());
		detailsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		detailsPanel.add(physicalDetailsPanel);
		detailsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));

		detailsPanel.add(new PM_Label("Plan Details", Font.BOLD));
		detailsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		detailsPanel.add(new JSeparator());
		detailsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		detailsPanel.add(planDetailsPanel);

		detailsPanel.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));

		mainWindow.add(detailsPanel, BorderLayout.CENTER);
		mainWindow.addWindowListener(this);
		mainWindow.setResizable(false);
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
