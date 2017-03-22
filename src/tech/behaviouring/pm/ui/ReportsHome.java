package tech.behaviouring.pm.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.ui.widgets.*;
import tech.behaviouring.pm.util.EventLog;
import tech.behaviouring.pm.util.DataStructures.TimeOfDay;

/*
 * Created by Mohan on 20/2/2016
 */

public class ReportsHome extends PM_Activity {

	private static final String tag = "Reports Home";

	// Whether this activity is up
	private static boolean isActive = false;

	private DBOperations db;
	private int BORDER_WIDTH = 20;
	private int amountCollectedToday;
	private int footfallMorning;
	private int footfallEvening;

	public ReportsHome() {
		isActive = true;
	}

	// Implement Runnable interface

	@Override
	public void run() {
		try {
			db = DBOperations.getInstance();
			getTodaysNumbers();
			init();
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	private void getTodaysNumbers() {
		// Get footfall numbers from attendance cache
		AttendanceMarked am = AttendanceMarked.getInstance();
		footfallMorning = am.getAttendanceObject().getNAttendees(TimeOfDay.Morning);
		footfallEvening = am.getAttendanceObject().getNAttendees(TimeOfDay.Evening);

		amountCollectedToday = db.getTotalPaymentAfterDate(Calendar.getInstance().getTime());
	}

	private void init() {
		mainWindow.setTitle("Reports");

		// Header panel
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		headerPanel.add(new PM_Label("Today at a glance"));
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		headerPanel.add(new JSeparator());
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));

		// Report panel
		JPanel reportPanel = new JPanel();
		reportPanel.setLayout(new GridLayout(1, 3));
		reportPanel.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		reportPanel.add(new PM_BigNumber(amountCollectedToday, "Amount Collected", new Color(0x45, 0x53, 0x6B)));
		reportPanel.add(new PM_BigNumber(footfallMorning, "Morning Attendance", new Color(0x45, 0x53, 0x6B)));
		reportPanel.add(new PM_BigNumber(footfallEvening, "Evening Attendance", new Color(0x45, 0x53, 0x6B)));

		// Footer panel
		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
		footerPanel.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		footerPanel.add(new JSeparator());

		PM_Label detailedReportAttendance = new PM_Label("Detailed Attendance Report");
		detailedReportAttendance.setName("Attendance Report");
		Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		Font font = detailedReportAttendance.getFont();
		Map attributes = font.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		detailedReportAttendance.setFont(font.deriveFont(attributes));
		detailedReportAttendance.setCursor(cursor);
		detailedReportAttendance.addMouseListener(this);

		PM_Label detailedReportPayment = new PM_Label("Detailed Payment Report");
		detailedReportPayment.setName("Payment Report");
		font = detailedReportPayment.getFont();
		attributes = font.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		detailedReportPayment.setFont(font.deriveFont(attributes));
		detailedReportPayment.setCursor(cursor);
		detailedReportPayment.addMouseListener(this);

		footerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		footerPanel.add(detailedReportAttendance);
		footerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		footerPanel.add(detailedReportPayment);
		footerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		footerPanel.add(new PM_Label("Last refresh time: " + new Date()));

		mainWindow.add(headerPanel, BorderLayout.NORTH);
		mainWindow.add(reportPanel, BorderLayout.CENTER);
		mainWindow.add(footerPanel, BorderLayout.SOUTH);
		mainWindow.pack();
		mainWindow.setLocationRelativeTo(null);
		mainWindow.addWindowListener(this);
		mainWindow.setResizable(false);
		mainWindow.setVisible(true);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		PM_Label labelClicked = (PM_Label) e.getSource();
		if (labelClicked.getName().equals("Payment Report")) {
			if (!PaymentReport.isActive())
				EventQueue.invokeLater(new PaymentReport());
		} else if (labelClicked.getName().equals("Attendance Report")) {
			if (!AttendanceReport.isActive())
				EventQueue.invokeLater(new AttendanceReport());
		}
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
