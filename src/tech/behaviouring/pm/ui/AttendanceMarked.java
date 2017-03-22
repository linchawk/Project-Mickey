package tech.behaviouring.pm.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.core.applogic.objects.DailyAttendance;
import tech.behaviouring.pm.core.applogic.objects.MemberDetails;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.ui.widgets.PM_EmptyLabel;
import tech.behaviouring.pm.ui.widgets.PM_Label;
import tech.behaviouring.pm.util.Calculate;
import tech.behaviouring.pm.util.EventLog;

/*
 * Created by Mohan on 5/1/2016
 */

public class AttendanceMarked extends PM_Activity {

	private static final String tag = "Attendance Marked";

	// Instance object for Singleton class. Lets make AttendanceMarked a
	// singleton
	private static AttendanceMarked instance = null;
	// Lock object for thread safety
	private static Object lock = new Object();
	private DBOperations db;

	// The MemberDetails object of the member who marked the attendance
	private MemberDetails md = null;
	private JPanel memberDetailsPanel;
	JButton btnMakePayment;
	private int BORDER_WIDTH = 20;
	// Difference in number of days between today and next renewal date of this
	// member
	private int dateDiff;

	// DailyAttendance object which stores attendance marked by members
	private DailyAttendance dailyAttendance;

	private AttendanceMarked() {
		/*
		 * Make constructor to beat instantiation
		 */
		initAttendanceForDay();
	}

	// Create the singleton object and return it

	public static AttendanceMarked getInstance() {
		synchronized (lock) {
			if (instance == null)
				instance = new AttendanceMarked();
			return instance;
		}
	}

	private void initAttendanceForDay() {
		db = DBOperations.getInstance();
		// Get attendance object for today
		dailyAttendance = db.getAttendanceForDate(new Date());
	}

	public void setNewMember(MemberDetails memberDetails) {

		if (memberDetails == null)
			return;
		mainWindow = new JFrame();
		md = memberDetails;
	}

	// Implement Runnable interface
	@Override
	public void run() {
		try {
			if (md != null) {
				calculateDateDiff();
				init();
				markAttendance();
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	// Implement ActionListener interface
	@Override
	public void actionPerformed(ActionEvent event) {

		String buttonClicked = event.getActionCommand();
		if (buttonClicked.equals("More Details")) {
			if (!ViewMemberDetails.isActive())
				EventQueue.invokeLater(new ViewMemberDetails(md));
		} else if (buttonClicked.equals("Make Payment")) {
			EventQueue.invokeLater(new MakePayment(md));
		}

	}

	// Prepare the UI elements and dipslay them

	private void init() {
		// Initialize the frame itself

		mainWindow.setTitle("Attendance Marker");
		// Now init the the panel
		memberDetailsPanel = new JPanel();

		renderMemberDetails();

		mainWindow.getContentPane().add(memberDetailsPanel);

		// containerFrame.setMinimumSize(new Dimension(800, 600));
		mainWindow.setLocationRelativeTo(null);
		mainWindow.addWindowListener(this);
		mainWindow.setResizable(false);
		mainWindow.pack();
		mainWindow.setVisible(true);
	}

	private void renderMemberDetails() {
		memberDetailsPanel.setLayout(new BorderLayout());

		// Set border for Personal Details Panel
		memberDetailsPanel.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));

		// Add header panel to North
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		PM_Label lblHeader = new PM_Label("A member has marked attendance");
		headerPanel.add(lblHeader);
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		headerPanel.add(new JSeparator());
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		memberDetailsPanel.add(headerPanel, BorderLayout.NORTH);

		// Add photo panel to West
		JPanel photoContainer = new JPanel();
		photoContainer.setLayout(new BoxLayout(photoContainer, BoxLayout.Y_AXIS));
		photoContainer.setBorder(new EmptyBorder(0, 0, 0, BORDER_WIDTH * 5));
		JLabel memPhoto = new JLabel(new ImageIcon(md.getPicLocation()));
		PM_Label memName = new PM_Label(md.getName());
		JButton btnMoreInfo = new JButton("More Details");
		btnMoreInfo.addActionListener(this);

		photoContainer.add(memPhoto);
		photoContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		photoContainer.add(memName);
		photoContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		photoContainer.add(btnMoreInfo);
		memberDetailsPanel.add(photoContainer, BorderLayout.WEST);

		// Add member next renewal details to Center
		JPanel otherDetailsContainer = new JPanel();
		otherDetailsContainer.setLayout(new BoxLayout(otherDetailsContainer, BoxLayout.Y_AXIS));
		PM_Label lblNextRenewal = new PM_Label("Next payment date as on " + new Date().toString(), Font.BOLD);
		JLabel lblNextRenewalIcon = new JLabel(new ImageIcon(getNextRenewalIcon()));
		PM_Label lblNextRenewalMsg = new PM_Label(getNextRenewalMsg());
		btnMakePayment = new JButton("Make Payment");
		// If next payment due date is more than 5 days, disable make payment
		// button
		if (dateDiff > 5)
			btnMakePayment.setEnabled(false);
		btnMakePayment.addActionListener(this);

		otherDetailsContainer.add(lblNextRenewal);
		otherDetailsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		otherDetailsContainer.add(lblNextRenewalIcon);
		otherDetailsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		otherDetailsContainer.add(lblNextRenewalMsg);
		otherDetailsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		otherDetailsContainer.add(btnMakePayment);
		memberDetailsPanel.add(otherDetailsContainer, BorderLayout.CENTER);

	}

	private void calculateDateDiff() {
		Date today = Calculate.getTodayDateWithoutTime();

		System.out.println("Today in long: " + today.getTime());

		Date nextRenewal = md.getNextRenewal();
		dateDiff = Calculate.dateDiffInDays(today, nextRenewal);
	}

	private String getNextRenewalIcon() {
		String imagesPath = "res/img/";
		// The member has a lot of time for next renewal
		if (dateDiff > 5)
			return imagesPath + "hand_thumbsup.png";

		// The renewal date has already gone
		if (dateDiff < 0)
			return imagesPath + "hand_thumbsdown.png";

		switch (dateDiff) {
		case 1:
			return imagesPath + "day_1.png";
		case 2:
			return imagesPath + "day_2.png";
		case 3:
			return imagesPath + "day_3.png";
		case 4:
			return imagesPath + "day_4.png";
		case 5:
			return imagesPath + "day_5.png";
		}

		// If we reached this point, the renewal date is today
		return imagesPath + "hand_point.png";

	}

	private String getNextRenewalMsg() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String renewalDate = sdf.format(md.getNextRenewal());
		if (dateDiff > 5)
			return "The next payment is on " + renewalDate;

		// The renewal date has already gone
		if (dateDiff < 0)
			return "Payment missed. The payment date was " + renewalDate;

		switch (dateDiff) {
		case 1:
			return "Next payment is in 1 day (" + renewalDate + ")";
		case 2:
			return "Next payment is in 2 days (" + renewalDate + ")";
		case 3:
			return "Next payment is in 3 days (" + renewalDate + ")";
		case 4:
			return "Next payment is in 4 days (" + renewalDate + ")";
		case 5:
			return "Next payment is in 5 days (" + renewalDate + ")";
		}

		// If we reached this point, the renewal date is today
		return "Today is the payment date";
	}

	private void markAttendance() {
		dailyAttendance.registerAttendance(md.getId(), Calculate.getTimeOfDay());
		dailyAttendance.printAttendanceRegister();
		db.updateAttendanceForDay(dailyAttendance);
		md.setLastSeenOn(new Date()); // Update last seen date of member to
		// today
		db.updateMember(md);
	}

	public DailyAttendance getAttendanceObject() {
		return dailyAttendance;
	}

}
