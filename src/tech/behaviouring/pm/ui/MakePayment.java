package tech.behaviouring.pm.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.core.applogic.objects.GymPlan;
import tech.behaviouring.pm.core.applogic.objects.MemberDetails;
import tech.behaviouring.pm.core.applogic.objects.PaymentDetails;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.service.SmsService;
import tech.behaviouring.pm.ui.widgets.PM_EmptyLabel;
import tech.behaviouring.pm.ui.widgets.PM_Label;
import tech.behaviouring.pm.util.Calculate;
import tech.behaviouring.pm.util.EventLog;
import tech.behaviouring.pm.util.WorkerThread;

/*
 * Created by Mohan on 6/1/2016
 */

public class MakePayment extends PM_Activity implements ItemListener {

	private static final String tag = "Make Payment";

	// Whether this activity is up
	private static boolean isActive = false;

	// The MemberDetails object of the member who is making the payment
	private MemberDetails md;
	private DBOperations db;
	private int BORDER_WIDTH = 20;

	private JPanel paymentInputsPanel;
	private JComboBox<GymPlan> gymPlanName;
	private JComboBox<Integer> feePaidForNMonths;
	private PM_Label txtAmountPayable;
	private PM_Label txtNextRenewal;
	private PM_Label makePaymentMsg;
	private JButton btnMakePayment;

	private Date dtNextRenewal;

	public MakePayment(MemberDetails member) {
		isActive = true;
		md = member;
	}

	// Implement Runnable interface
	@Override
	public void run() {
		try {
			if (md != null) {
				db = DBOperations.getInstance();
				init();
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
		}

	}

	// Implement ActionListener interface
	@Override
	public void actionPerformed(ActionEvent event) {
		String buttonClicked = event.getActionCommand();
		if (buttonClicked.equals("Make Payment"))
			new CommitPaymentWorker().start();
		else if (buttonClicked.equals("Close"))
			mainWindow.dispose();
	}

	// Implement ItemListener interface

	public void itemStateChanged(ItemEvent event) {
		calculateAmountPayableAndNextRenewal();
	}

	private void calculateAmountPayableAndNextRenewal() {

		try {
			// Calculate amount payable;
			GymPlan selectedGymPlan = (GymPlan) gymPlanName.getSelectedItem();
			int nMonths = ((Integer) feePaidForNMonths.getSelectedItem()).intValue();
			int amountPayable = 0;
			switch (nMonths) {
			case 1:
				amountPayable = selectedGymPlan.getFee1Month();
				break;
			case 3:
				amountPayable = selectedGymPlan.getFee3Month();
				break;
			case 6:
				amountPayable = selectedGymPlan.getFee6Month();
				break;
			case 12:
				amountPayable = selectedGymPlan.getFee12Month();
			}
			// Calculate next renewal date based on today's date and number of
			// months
			String nextRenewal = Calculate.addMonthsToDate(new Date(), nMonths);
			dtNextRenewal = Calculate.addMonthsToDate1(new Date(), nMonths);
			txtAmountPayable.setText(amountPayable + "");
			txtNextRenewal.setText(nextRenewal);
		} catch (Exception e) {
			// In case of exception we don't have to do anything other than
			// clearing the fields in case we updated values earlier
			txtAmountPayable.setText("");
			txtNextRenewal.setText("");
		}

	}

	private void init() {
		// Initialize the frame itself

		mainWindow.setTitle("Make Payment");
		// Now init the the panel
		paymentInputsPanel = new JPanel();

		renderPaymentInputs();

		mainWindow.getContentPane().add(paymentInputsPanel);
		mainWindow.setLocationRelativeTo(null);
		mainWindow.addWindowListener(this);
		mainWindow.setResizable(false);
		mainWindow.pack();
		mainWindow.setVisible(true);
	}

	private void renderPaymentInputs() {
		paymentInputsPanel.setLayout(new BorderLayout());

		// Set border for Personal Details Panel
		paymentInputsPanel.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));

		// Add header panel to North
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		PM_Label lblHeader = new PM_Label("Please provide inputs for new payment");
		headerPanel.add(lblHeader);
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		headerPanel.add(new JSeparator());
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		paymentInputsPanel.add(headerPanel, BorderLayout.NORTH);

		// Add photo panel to West
		JPanel photoContainer = new JPanel();
		photoContainer.setLayout(new BoxLayout(photoContainer, BoxLayout.Y_AXIS));
		photoContainer.setBorder(new EmptyBorder(0, 0, 0, BORDER_WIDTH * 5));
		JLabel memPhoto = new JLabel(new ImageIcon(md.getPicLocation()));
		PM_Label memName = new PM_Label(md.getName());

		photoContainer.add(memPhoto);
		photoContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		photoContainer.add(memName);
		paymentInputsPanel.add(photoContainer, BorderLayout.WEST);

		// Add payment inputs to Center
		JPanel paymentInputsContainer = new JPanel();
		paymentInputsContainer.setLayout(new BoxLayout(paymentInputsContainer, BoxLayout.Y_AXIS));
		PM_Label lblDateJoined = new PM_Label("Date of Joining", Font.BOLD);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String dateJoined = sdf.format(md.getDateJoined());
		PM_Label txtDateJoined = new PM_Label(dateJoined);

		PM_Label lblPlanName = new PM_Label("Plan Name", Font.BOLD);
		gymPlanName = new JComboBox<GymPlan>();
		gymPlanName.setAlignmentX(Component.LEFT_ALIGNMENT);
		// Get all GymPlan from database and add them to the combo box
		int currentGymPlanObjectIndex = 0;
		int counter = 0;
		for (GymPlan gymPlan : db.getAllGymPlans()) {
			gymPlanName.addItem(gymPlan);
			if (md.getPlanId() == gymPlan.getId())
				currentGymPlanObjectIndex = counter;
			counter++;
		}
		gymPlanName.setSelectedIndex(currentGymPlanObjectIndex);
		gymPlanName.addItemListener(this);

		PM_Label lblFeePaidForNMonths = new PM_Label("Subscription months", Font.BOLD);
		feePaidForNMonths = new JComboBox<Integer>();
		feePaidForNMonths.setAlignmentX(Component.LEFT_ALIGNMENT);
		feePaidForNMonths.addItem(1);
		feePaidForNMonths.addItem(3);
		feePaidForNMonths.addItem(6);
		feePaidForNMonths.addItem(12);
		switch (md.getFeePaidForNMonth()) {
		case 1:
			feePaidForNMonths.setSelectedIndex(0);
			break;
		case 3:
			feePaidForNMonths.setSelectedIndex(1);
			break;
		case 6:
			feePaidForNMonths.setSelectedIndex(2);
			break;
		case 12:
			feePaidForNMonths.setSelectedIndex(3);
			break;
		}
		feePaidForNMonths.addItemListener(this);

		PM_Label lblAmountPayable = new PM_Label("Amount payable", Font.BOLD);
		txtAmountPayable = new PM_Label();
		PM_Label lblNextRenewal = new PM_Label("Next renewal", Font.BOLD);
		txtNextRenewal = new PM_Label();

		makePaymentMsg = new PM_Label("");
		makePaymentMsg.setVisible(false); // Hide msg label initially
		makePaymentMsg.setForeground(Color.RED);

		// Prepare and add footer to South
		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
		JPanel footerPanelInner = new JPanel();
		footerPanelInner.setLayout(new FlowLayout(FlowLayout.RIGHT));
		footerPanelInner.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		btnMakePayment = new JButton("Make Payment");
		btnMakePayment.addActionListener(this);
		footerPanel.add(btnMakePayment);

		// Calculate amount payable and next renewal based on the current gym
		// plan of the member
		calculateAmountPayableAndNextRenewal();

		paymentInputsContainer.add(lblDateJoined);
		paymentInputsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		paymentInputsContainer.add(txtDateJoined);
		paymentInputsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		paymentInputsContainer.add(lblPlanName);
		paymentInputsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		paymentInputsContainer.add(gymPlanName);
		paymentInputsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		paymentInputsContainer.add(lblFeePaidForNMonths);
		paymentInputsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		paymentInputsContainer.add(feePaidForNMonths);
		paymentInputsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		paymentInputsContainer.add(lblAmountPayable);
		paymentInputsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		paymentInputsContainer.add(txtAmountPayable);
		paymentInputsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		paymentInputsContainer.add(lblNextRenewal);
		paymentInputsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		paymentInputsContainer.add(txtNextRenewal);
		paymentInputsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		paymentInputsContainer.add(makePaymentMsg);
		paymentInputsContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		paymentInputsContainer.add(footerPanel);
		paymentInputsPanel.add(paymentInputsContainer, BorderLayout.CENTER);

	}

	private class CommitPaymentWorker extends WorkerThread {

		private boolean commitSuccess;
		private boolean commitCancelled = false;

		@Override
		public void preExecute() {
			makePaymentMsg.setVisible(true);
			makePaymentMsg.setText("Processing payment. Please wait..");

		}

		@Override
		public void executeAsync() {
			PaymentDetails pd = new PaymentDetails();
			pd.setMemId(md.getId());
			int paymentAmount;
			try {
				paymentAmount = Integer.parseInt(txtAmountPayable.getText());
			} catch (Exception e) {
				paymentAmount = -1;
			}
			pd.setPayAmount(paymentAmount);
			pd.setPayDate(new Date());

			// If all inputs are valid, just confirm with the user
			// before
			// commiting
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					makePaymentMsg.setVisible(false);
				}
			});

			int response = JOptionPane.showConfirmDialog(null, "Do you want to save the information?", "Confirm",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.NO_OPTION) {
				commitCancelled = true;
				return;
			}

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					// Remember we hid makePaymentMsg before showing confirm
					// dialog
					makePaymentMsg.setVisible(true);
				}
			});

			commitSuccess = db.createPayment(pd);
			if (commitSuccess) {
				md.setPlanId(((GymPlan) gymPlanName.getSelectedItem()).getId());
				md.setFeePaidForNMonth(((Integer) feePaidForNMonths.getSelectedItem()).intValue());
				md.setNextRenewal(dtNextRenewal);
				commitSuccess = db.updateMember(md);
				if (commitSuccess)
					sendPaymentAckSms(md, pd, ((GymPlan) gymPlanName.getSelectedItem()).getName());
			}
		}

		@Override
		public void postExecute() {

			// If commit was cancelled just return
			if (commitCancelled)
				return;

			if (commitSuccess) {
				makePaymentMsg.setText("Payment made successfully");
				btnMakePayment.setText("Close");
				// Freeze payment inputs so that the admin user won't make
				// payment multiple times
				freezePaymentInputs();

			} else {
				makePaymentMsg.setText("Some error occured while making payment");
			}
			mainWindow.invalidate();
			mainWindow.validate();
			mainWindow.pack();
			mainWindow.repaint();
		}

		private void freezePaymentInputs() {
			gymPlanName.setEnabled(false);
			feePaidForNMonths.setEnabled(false);
		}

		private void sendPaymentAckSms(MemberDetails md, PaymentDetails pd, String planName) {
			String ackMsg = db.getValue("payment_ack_sms");
			ackMsg = ackMsg.replace("%mem_name%", md.getName());
			ackMsg = ackMsg.replace("%pay_amount%", pd.getPayAmount() + "");
			ackMsg = ackMsg.replace("%plan_name%", planName);
			ackMsg = ackMsg.replace("%payment_date%", new Date() + "");
			ackMsg = ackMsg.replace("%gym_name%", db.getValue("gym_name"));

			SmsService smsService = SmsService.getInstance();
			smsService.sendSms(md.getPh(), ackMsg);

			// After sending ack sms, reset the remainders sms sent count back
			// to 0
			md.setNRemainderSmsSent(0);
			db.updateMember(md);
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
