package tech.behaviouring.pm.ui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.core.applogic.objects.MemberDetails;
import tech.behaviouring.pm.core.applogic.objects.PaymentDetails;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.ui.widgets.*;
import tech.behaviouring.pm.util.Calculate;
import tech.behaviouring.pm.util.EventLog;
import tech.behaviouring.pm.util.WorkerThread;

/*
 * Created by Mohan on 20/2/2016
 */
public class PaymentReport extends PM_Activity {

	private static final String tag = "Payment Report";

	// Whether this activity is up
	private static boolean isActive = false;

	private int BORDER_WIDTH = 20;

	private PM_DatePicker startDate;
	private PM_DatePicker endDate;
	private JButton btnNext;
	private JButton btnPrev;
	private JButton btnFilter;
	private JTable tblPaymentRecords;
	private JPanel tblPanel;

	private int currentPage;
	private int paymentRecordsPerPage;
	private int currentOffset;
	private int totalPaymentRecords;
	private Date startDate1;
	private Date endDate1;

	private List<PaymentDetails> paymentRecords;
	private DBOperations db;

	public PaymentReport() {
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
			paymentRecordsPerPage = 10;
			// Set initial start date to 6 months before today
			startDate1 = Calculate.addMonthsToDate1(new Date(), -6);
			// Set initial end date to today
			endDate1 = new Date();
			getRecords();
			init();
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	private void getRecords() {
		currentOffset = (currentPage - 1) * paymentRecordsPerPage;
		paymentRecords = db.getPaymentDetailsInDateRange(startDate1, endDate1, currentOffset, paymentRecordsPerPage);
	}

	private void init() {
		mainWindow.setTitle("Payment Report");

		// Header panel
		JPanel headerPanel = new JPanel();
		GridLayout gl = new GridLayout(2, 3);
		gl.setHgap(20);
		gl.setVgap(10);
		headerPanel.setLayout(gl);
		headerPanel.setBorder(new EmptyBorder(BORDER_WIDTH / 2, BORDER_WIDTH / 2, BORDER_WIDTH / 2, BORDER_WIDTH / 2));
		startDate = new PM_DatePicker(startDate1);
		// startDate.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH,
		// BORDER_WIDTH, BORDER_WIDTH));
		endDate = new PM_DatePicker(endDate1);
		btnFilter = new JButton("Filter");
		btnFilter.addActionListener(this);

		headerPanel.add(new PM_Label("Start date"));
		headerPanel.add(new PM_Label("End date"));
		headerPanel.add(new PM_Label());
		headerPanel.add(startDate);
		headerPanel.add(endDate);
		headerPanel.add(btnFilter);

		mainWindow.add(headerPanel, BorderLayout.NORTH);
		constructTable();
		addFooter();
		setPaging();

		mainWindow.pack();
		mainWindow.setLocationRelativeTo(null);
		mainWindow.addWindowListener(this);
		mainWindow.setMinimumSize(new Dimension(640, 480));
		mainWindow.setResizable(false);
		mainWindow.setVisible(true);
	}

	private void constructTable() {
		String[] columnNames = { "Payment Id", "Member Name", "Payment Date", "Payment Amount" };
		Object[][] tableDate = new Object[paymentRecords.size() + 2][columnNames.length];
		int i;
		int totalAmountInThisPage = 0;
		for (i = 0; i < paymentRecords.size(); i++) {
			MemberDetails md = db.getMemberById(paymentRecords.get(i).getMemId());
			PaymentDetails pd = paymentRecords.get(i);
			tableDate[i][0] = pd.getPayId();
			tableDate[i][1] = md.getName();
			tableDate[i][2] = pd.getPayDate();
			tableDate[i][3] = pd.getPayAmount();
			totalAmountInThisPage += pd.getPayAmount();
		}

		// Empty row in table
		tableDate[i][0] = "";
		tableDate[i][1] = "";
		tableDate[i][2] = "";
		tableDate[i][3] = "";

		tableDate[i + 1][0] = "";
		tableDate[i + 1][1] = "";
		tableDate[i + 1][2] = "Total Amount";
		tableDate[i + 1][3] = totalAmountInThisPage;

		tblPaymentRecords = new JTable(tableDate, columnNames);
		tblPaymentRecords.setEnabled(false);
		tblPanel = new JPanel();
		tblPanel.setBorder(new EmptyBorder(BORDER_WIDTH / 2, BORDER_WIDTH / 2, BORDER_WIDTH / 2, BORDER_WIDTH / 2));
		tblPanel.add(new JScrollPane(tblPaymentRecords));
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
		totalPaymentRecords = db.getPaymentCountInDateRange(startDate1, endDate1);
		int totalMembersDisplayedSoFar = currentPage * paymentRecordsPerPage;
		boolean nextPageAvailable = false;
		if (totalMembersDisplayedSoFar < totalPaymentRecords)
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
			else if (buttonClicked.equals("Filter")) {
				startDate1 = startDate.getDate();
				endDate1 = endDate.getDate();
				currentPage = 1;
			}

			getRecords();
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
