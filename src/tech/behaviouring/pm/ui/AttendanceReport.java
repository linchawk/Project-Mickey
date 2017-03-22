package tech.behaviouring.pm.ui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import tech.behaviouring.pm.core.applogic.objects.DailyAttendance;
import tech.behaviouring.pm.core.applogic.objects.MemberDetails;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.ui.widgets.*;
import tech.behaviouring.pm.util.Calculate;
import tech.behaviouring.pm.util.EventLog;
import tech.behaviouring.pm.util.DataStructures.TimeOfDay;
import tech.behaviouring.pm.util.WorkerThread;

/*
 * Created by Mohan on 21/2/2016
 */
public class AttendanceReport extends PM_Activity {

	private static final String tag = "Attendance Report";
	// Whether this activity is up
	private static boolean isActive = false;

	private int BORDER_WIDTH = 20;

	private JFrame mainWindow;
	private JButton btnNext;
	private JButton btnPrev;
	private JTable tblMemberRecords;
	private JPanel tblPanel;

	private int currentPage;
	private int memberRecordsPerPage;
	private int currentOffset;
	private int totalMemberRecords;
	private String[] columnNames;
	private String[] attendanceRecords;

	private List<MemberDetails> memberRecords;
	private DBOperations db;

	public AttendanceReport() {
		isActive = true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String buttonClicked = e.getActionCommand();
		new PagingButtonClickHandler(buttonClicked).start();
	}

	@Override
	public void run() {
		try {
			db = DBOperations.getInstance();
			currentPage = 1;
			memberRecordsPerPage = 10;
			getMemberRecords();
			getAttendanceRecords();
			setColumnNames();
			init();
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	private void getMemberRecords() {
		currentOffset = (currentPage - 1) * memberRecordsPerPage;
		memberRecords = db.getAllMembers(currentOffset, memberRecordsPerPage);
	}

	private void getAttendanceRecords() {
		int nAttendanceRecords = 7;
		// Attendance records for today and last 6 days
		attendanceRecords = new String[nAttendanceRecords];
		int i;
		for (i = 0; i < attendanceRecords.length; i++) {
			attendanceRecords[i] = "";
		}

		Date today = Calculate.getTodayDateWithoutTime();

		List<DailyAttendance> last7DaysAttendance = db.getAttendanceAfterDate(Calculate.addDaysToDate1(today, -6));
		for (DailyAttendance da : last7DaysAttendance) {
			/*
			 * Since we retrieved the attendance records of 7 days, the date
			 * diff in days between today and the attendance record date will be
			 * in the range of 0 - 6
			 * 
			 */
			int dateDiffInDays = Calculate.dateDiffInDays(da.getDate(), today);
			// Merge morning comers and evening comers into a single large
			// string. (nAttendanceRecords - 1) - dateDiffInDays because the
			// attendance records are
			// pulled from db in ascending by attendance date
			attendanceRecords[(nAttendanceRecords - 1) - dateDiffInDays] = da.getAttendees(TimeOfDay.Morning)
					+ da.getAttendees(TimeOfDay.Evening);
		}
	}

	private void init() {
		mainWindow = new JFrame("Attendance Report");

		// Header panel
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setBorder(new EmptyBorder(BORDER_WIDTH / 2, BORDER_WIDTH / 2, BORDER_WIDTH / 2, BORDER_WIDTH / 2));
		headerPanel.add(new PM_Label("Attendance records of members for today and last 6 days"));
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		headerPanel.add(new JSeparator());
		mainWindow.add(headerPanel, BorderLayout.NORTH);
		constructTable();
		addFooter();
		setPaging();
		mainWindow.setMinimumSize(new Dimension(800, 600));
		mainWindow.pack();
		mainWindow.setLocationRelativeTo(null);
		mainWindow.addWindowListener(this);
		mainWindow.setResizable(false);
		mainWindow.setVisible(true);
	}

	private void setColumnNames() {
		Date today = new Date();
		columnNames = new String[8];
		columnNames[0] = "Member Name";
		columnNames[1] = Calculate.addDaysToDate(today, -6);
		columnNames[2] = Calculate.addDaysToDate(today, -5);
		columnNames[3] = Calculate.addDaysToDate(today, -4);
		columnNames[4] = Calculate.addDaysToDate(today, -3);
		columnNames[5] = Calculate.addDaysToDate(today, -2);
		columnNames[6] = Calculate.addDaysToDate(today, -1);
		columnNames[7] = Calculate.addDaysToDate(today, 0);
	}

	private void constructTable() {
		ImageIcon imgYes = new ImageIcon("res/img/checkmark.png");
		ImageIcon imgNo = new ImageIcon("res/img/cross.png");
		Object[][] tableDate = new Object[memberRecords.size()][columnNames.length];
		for (int i = 0; i < memberRecords.size(); i++) {
			MemberDetails md = memberRecords.get(i);
			tableDate[i][0] = md.getName();
			for (int j = 0; j < 7; j++) {
				if (attendanceRecords[j].contains(md.getId() + ""))
					tableDate[i][j + 1] = imgYes;
				else
					tableDate[i][j + 1] = imgNo;
			}
		}
		DefaultTableModel model = new DefaultTableModel(tableDate, columnNames) {

			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int column) {
				// Column 0 is Member Name which is string
				if (column == 0)
					return String.class;
				// Column 1 - 6 are image icons
				else
					return ImageIcon.class;
			}
		};

		tblMemberRecords = new JTable(model);
		tblMemberRecords.setPreferredScrollableViewportSize(tblMemberRecords.getPreferredSize());
		tblMemberRecords.setEnabled(false);
		tblPanel = new JPanel();
		tblPanel.setBorder(new EmptyBorder(BORDER_WIDTH / 2, BORDER_WIDTH / 2, BORDER_WIDTH / 2, BORDER_WIDTH / 2));
		tblPanel.add(new JScrollPane(tblMemberRecords));
		mainWindow.add(tblPanel, BorderLayout.CENTER);
	}

	private void addFooter() {
		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		footerPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		footerPanel.setBorder(new EmptyBorder(BORDER_WIDTH / 2, BORDER_WIDTH / 2, BORDER_WIDTH / 2, BORDER_WIDTH / 2));
		btnPrev = new JButton("Previous");
		btnPrev.addActionListener(this);
		btnNext = new JButton("Next");
		btnNext.addActionListener(this);
		footerPanel.add(btnNext);
		footerPanel.add(btnPrev);
		mainWindow.add(footerPanel, BorderLayout.SOUTH);
	}

	private void setPaging() {
		totalMemberRecords = db.getMembersCount();
		int totalMembersDisplayedSoFar = currentPage * memberRecordsPerPage;
		boolean nextPageAvailable = false;
		if (totalMembersDisplayedSoFar < totalMemberRecords)
			nextPageAvailable = true;

		if (currentPage == 1)
			btnPrev.setVisible(false);
		else
			btnPrev.setVisible(true);

		if (nextPageAvailable)
			btnNext.setVisible(true);
		else
			btnNext.setVisible(false);
	}

	private class PagingButtonClickHandler extends WorkerThread {

		String buttonClicked;

		public PagingButtonClickHandler(String buttonClicked) {
			this.buttonClicked = buttonClicked;
		}

		@Override
		public void preExecute() {
			mainWindow.remove(tblPanel);

		}

		@Override
		public void executeAsync() {
			if (buttonClicked.equals("Next"))
				currentPage++;
			else if (buttonClicked.equals("Previous"))
				currentPage--;

			getMemberRecords();
			constructTable();
			setPaging();
			mainWindow.invalidate();
			mainWindow.validate();
			mainWindow.pack();
			mainWindow.repaint();
		}

		@Override
		public void postExecute() {
			// TODO Auto-generated method stub

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
