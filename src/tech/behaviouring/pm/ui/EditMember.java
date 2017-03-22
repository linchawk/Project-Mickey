package tech.behaviouring.pm.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.core.applogic.PM_Event.EventType;
import tech.behaviouring.pm.core.applogic.PM_EventListener;
import tech.behaviouring.pm.core.applogic.objects.*;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.hardware.camera.TakePhoto;
import tech.behaviouring.pm.hardware.camera.TakePhotoListener;
import tech.behaviouring.pm.ui.widgets.*;
import tech.behaviouring.pm.util.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by Mohan on 22/12/2015
 */

public class EditMember extends PM_Activity implements ItemListener, PropertyChangeListener {

	// Whether this activity is up
	private static boolean isActive = false;

	private String tag = "Edit New Member";
	private String defaultUserPic = "res/img/user.png";
	private int BORDER_WIDTH = 20;
	private String newPhotoName;
	private String newPhotoLocation;
	private List<GymPlan> availableGymPlans;

	private enum Step {
		STEP1, STEP2, STEP3
	};

	// DBOperations object
	private DBOperations db;

	// If any guy is interested in Member management CRUD events
	private PM_EventListener listener;

	// To track which step we are currently in
	private Step currentStep;

	private MemberDetails md;

	// Three panels for three steps
	private JPanel personalDetails;
	private JPanel physicalDetails;
	private JPanel gymPlanDetails;

	// Step 1 - Personal details widgets
	private JLabel memPhoto;
	private JButton btnChangePhoto;
	private PM_TextField memName;
	private PM_TextField memPhone;
	private PM_TextField memEmail;
	private PM_DatePicker memDob;
	private PM_TextArea memAddr;
	private PM_Label step1Msg;

	// Step 2 - Physical details widgets
	private PM_TextField memBloodGroup;
	private PM_TextField memHeight;
	private PM_TextField memWeight;
	private PM_Label step2Msg;

	// Step 3 - Gym plan details widgets
	private PM_DatePicker memDateJoined;
	private JComboBox<GymPlan> memPlanName;
	private JComboBox<Integer> memFeePaidForNMonths;
	private PM_Label memAmountPayable;
	private PM_Label memNextRenewal;
	private PM_Label step3Msg;

	// Action buttons
	private JButton btnLeft;
	private JButton btnCenter;
	private JButton btnRight;

	public EditMember(MemberDetails member, PM_EventListener listener) {
		isActive = true;
		md = member;
		this.listener = listener;
	}

	// Implement Runnable
	@Override
	public void run() {
		try {
			db = DBOperations.getInstance();
			init();
			if (md != null) {
				preFillMemberDetails();
			}
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	// Implement ActionListener
	@Override
	public void actionPerformed(ActionEvent event) {
		// Get which button has been clicked
		String buttonClicked = event.getActionCommand();

		if (currentStep == Step.STEP1) {
			// If Change Photo button was clicked, we need to start TakePhoto
			// frame
			if (buttonClicked.equals("Change Photo")) {
				if (TakePhoto.isActive())
					return;
				TakePhoto takePhoto = new TakePhoto(new TakePhotoListener() {
					public void photoTaken(final String photoName, final String photoLocation) {
						EventQueue.invokeLater(new Runnable() {

							public void run() {
								// Update the default picture with the photo
								// taken
								newPhotoName = photoName;
								newPhotoLocation = photoLocation;
								memPhoto.setIcon(new ImageIcon(photoLocation));
								mainWindow.repaint();
							}

						});
					}
				});

				EventQueue.invokeLater(takePhoto);
				return;
			}

			// If next button is clicked we need to validate all inputs
			// before
			// proceeding to Step2
			if (buttonClicked.equals("Next")) {
				new ValidateWorker().start();
			} else if (buttonClicked.equals("Cancel")) {
				mainWindow.dispose();
			}

		} else if (currentStep == Step.STEP2) {
			if (buttonClicked.equals("Next")) {
				new ValidateWorker().start();
			} else if (buttonClicked.equals("Previous")) {
				mainWindow.remove(physicalDetails);
				mainWindow.add(personalDetails);
				mainWindow.invalidate();
				mainWindow.validate();
				mainWindow.repaint();
				currentStep = Step.STEP1;

			} else {
				mainWindow.dispose();
			}

		} else {
			if (buttonClicked.equals("Update")) {
				new ValidateWorker().start();
			} else if (buttonClicked.equals("Previous")) {
				mainWindow.remove(gymPlanDetails);
				mainWindow.add(physicalDetails);
				mainWindow.invalidate();
				mainWindow.validate();
				mainWindow.repaint();
				currentStep = Step.STEP2;

			} else {
				mainWindow.dispose();
			}
		}
	}

	// Implement ItemListener interface

	public void itemStateChanged(ItemEvent event) {
		calculateAmountPayable();
		calculateRenewalDate();
	}

	// Implement PropertyChangeListener interface
	public void propertyChange(PropertyChangeEvent event) {
		calculateRenewalDate();
	}

	// Calculates next renewal date based on dateJoined and feePaidForNMonths

	private void calculateRenewalDate() {
		try {
			// Try to parse the value of Date Joined field and Fee Paid For
			// field
			Date dateJoined = memDateJoined.getDate();
			int feePaidForNMonths = ((Integer) memFeePaidForNMonths.getSelectedItem()).intValue();
			// Update other fields accordingly
			String nextRenewal = Calculate.addMonthsToDate(dateJoined, feePaidForNMonths);
			memNextRenewal.setText(nextRenewal);
		} catch (Exception e) {
			// In case of exception we don't have to do anything other than
			// clearing the fields in case we updated values earlier
			memNextRenewal.setText("");
		}
	}

	// Calculates total amount payable based on gym plan and feePaidForNMonths

	private void calculateAmountPayable() {
		try {
			int feePaidForNMonths = ((Integer) memFeePaidForNMonths.getSelectedItem()).intValue();
			GymPlan selectedGymPlan = (GymPlan) memPlanName.getSelectedItem();
			int admissionFee = Integer.parseInt(db.getValue("admission_fee"));
			int planAmount = 0;
			switch (feePaidForNMonths) {
			case 1:
				planAmount = selectedGymPlan.getFee1Month();
				break;
			case 3:
				planAmount = selectedGymPlan.getFee3Month();
				break;
			case 6:
				planAmount = selectedGymPlan.getFee6Month();
				break;
			case 12:
				planAmount = selectedGymPlan.getFee12Month();
				break;

			}
			int totalAmount = admissionFee + planAmount;
			memAmountPayable
					.setText(totalAmount + " (Admission fee: " + admissionFee + ", Plan amount: " + planAmount + ")");
		} catch (Exception e) {
			memAmountPayable.setText("");
		}
	}

	// Pre-fills all data fields

	private void preFillMemberDetails() {
		memPhoto.setIcon(new ImageIcon(md.getPicLocation()));
		memName.setText(md.getName());
		memDob.setDate(md.getDob());
		memPhone.setText(md.getPh());
		memEmail.setText(md.getEmail());
		memAddr.setText(md.getAddr());
		memHeight.setText(md.getHeightCm() + "");
		memWeight.setText(md.getWeightKg() + "");
		memBloodGroup.setText(md.getBloodGroup());
		memDateJoined.setDate(md.getDateJoined());

		// Freeze date joined field. It cannot be edited
		memDateJoined.setEnabled(false);

		int selectedIndex = 0;
		for (GymPlan gymPlan : availableGymPlans) {
			if (gymPlan.getId() == md.getPlanId())
				break;
			selectedIndex++;
		}

		memPlanName.setSelectedIndex(selectedIndex);

		switch (md.getFeePaidForNMonth()) {
		case 1:
			selectedIndex = 0;
			break;
		case 3:
			selectedIndex = 1;
			break;
		case 6:
			selectedIndex = 2;
			break;
		case 12:
			selectedIndex = 3;
		}

		memFeePaidForNMonths.setSelectedIndex(selectedIndex);

		// Calculate amount payable and next renewal for first time load
		calculateAmountPayable();
		calculateRenewalDate();
	}

	private void init() {
		// Initially make new photo location same as current user pic
		newPhotoLocation = md.getPicLocation();

		// Initialize the frame itself

		mainWindow.setTitle("Edit member");
		// Now init the three panels for 4 steps
		personalDetails = new JPanel();
		physicalDetails = new JPanel();
		gymPlanDetails = new JPanel();

		// Create step 1 to step 3 widgets
		initStep1();
		initStep2();
		initStep3();
		// When the frame is shown for the first time, we are in step1. So add
		// Personal Details Panel to the frame
		currentStep = Step.STEP1;
		mainWindow.add(personalDetails);

		mainWindow.setMinimumSize(new Dimension(800, 600));
		mainWindow.setLocationRelativeTo(null);
		mainWindow.addWindowListener(this);
		mainWindow.setResizable(false);
		mainWindow.setVisible(true);
	}

	private void initStep1() {
		personalDetails.setLayout(new BorderLayout());

		// Set border for Personal Details Panel
		personalDetails.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));

		// Add header panel to North
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		PM_Label lblHeader = new PM_Label("Step 1 - Please fill personal details");
		headerPanel.add(lblHeader);
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		headerPanel.add(new JSeparator());
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		personalDetails.add(headerPanel, BorderLayout.NORTH);

		// Add photo panel to West
		JPanel photoContainer = new JPanel();
		photoContainer.setLayout(new BoxLayout(photoContainer, BoxLayout.Y_AXIS));
		photoContainer.setBorder(new EmptyBorder(0, 0, 0, BORDER_WIDTH * 5));
		memPhoto = new JLabel(new ImageIcon(defaultUserPic));
		btnChangePhoto = new JButton("Change Photo");
		btnChangePhoto.addActionListener(this);
		photoContainer.add(memPhoto);
		photoContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		photoContainer.add(btnChangePhoto);
		photoContainer.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		personalDetails.add(photoContainer, BorderLayout.WEST);

		// Add inputs panel to Center
		JPanel inputsPanel = new JPanel();
		inputsPanel.setLayout(new BoxLayout(inputsPanel, BoxLayout.Y_AXIS));

		PM_Label lblMemName = new PM_Label("Name", Font.BOLD);
		memName = new PM_TextField();
		PM_Label lblMemPhone = new PM_Label("Phone number", Font.BOLD);
		memPhone = new PM_TextField();
		PM_Label lblMemEmail = new PM_Label("Email address", Font.BOLD);
		memEmail = new PM_TextField();
		PM_Label lblMemDob = new PM_Label("Date of Birth", Font.BOLD);
		memDob = new PM_DatePicker("dd/MM/yyyy", new Date());
		PM_Label lblMemAddr = new PM_Label("Address", Font.BOLD);
		memAddr = new PM_TextArea();

		inputsPanel.add(lblMemName);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memName);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		inputsPanel.add(lblMemPhone);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memPhone);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		inputsPanel.add(lblMemEmail);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memEmail);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		inputsPanel.add(lblMemDob);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memDob);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		inputsPanel.add(lblMemAddr);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memAddr);
		step1Msg = new PM_Label();
		step1Msg.setVisible(false); // Hide msg label initially
		step1Msg.setForeground(Color.RED);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(step1Msg);
		personalDetails.add(inputsPanel, BorderLayout.CENTER);

		// Prepare and add footer to South
		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
		JPanel footerPanelInner = new JPanel();
		footerPanelInner.setLayout(new FlowLayout(FlowLayout.RIGHT));
		footerPanelInner.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		btnLeft = new JButton("Previous");
		btnLeft.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnLeft.addActionListener(this);
		btnLeft.setVisible(false);
		btnCenter = new JButton("Next");
		btnCenter.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnCenter.addActionListener(this);
		btnRight = new JButton("Cancel");
		btnRight.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnRight.addActionListener(this);

		footerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		footerPanel.add(new JSeparator());
		// Our flow layout adds buttons from right to left. So btnRight button
		// should be added first
		footerPanelInner.add(btnRight);
		footerPanelInner.add(btnCenter);
		footerPanelInner.add(btnLeft);
		footerPanel.add(footerPanelInner);
		personalDetails.add(footerPanel, BorderLayout.SOUTH);
	}

	private void initStep2() {
		physicalDetails.setLayout(new BorderLayout());

		// Set border for Physical Details Panel
		physicalDetails.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));

		// Add header panel to North
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		PM_Label lblHeader = new PM_Label("Step 2 - Please fill physical details");
		headerPanel.add(lblHeader);
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		headerPanel.add(new JSeparator());
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		physicalDetails.add(headerPanel, BorderLayout.NORTH);

		// Add inputs panel to Center
		JPanel inputsPanel = new JPanel();
		inputsPanel.setLayout(new BoxLayout(inputsPanel, BoxLayout.Y_AXIS));

		PM_Label lblHeight = new PM_Label("Height in centimeters", Font.BOLD);
		memHeight = new PM_TextField();
		PM_Label lblWeight = new PM_Label("Weight in kilograms", Font.BOLD);
		memWeight = new PM_TextField();
		PM_Label lblBloodGroup = new PM_Label("Blood group", Font.BOLD);
		memBloodGroup = new PM_TextField();

		inputsPanel.add(lblHeight);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memHeight);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		inputsPanel.add(lblWeight);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memWeight);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		inputsPanel.add(lblBloodGroup);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memBloodGroup);
		step2Msg = new PM_Label();
		step2Msg.setVisible(false); // Hide msg label initially
		step2Msg.setForeground(Color.RED);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(step2Msg);
		physicalDetails.add(inputsPanel, BorderLayout.CENTER);

		// Prepare and add footer to South
		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
		JPanel footerPanelInner = new JPanel();
		footerPanelInner.setLayout(new FlowLayout(FlowLayout.RIGHT));
		footerPanelInner.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		btnLeft = new JButton("Previous");
		btnLeft.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnLeft.addActionListener(this);
		btnCenter = new JButton("Next");
		btnCenter.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnCenter.addActionListener(this);
		btnRight = new JButton("Cancel");
		btnRight.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnRight.addActionListener(this);

		footerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		footerPanel.add(new JSeparator());
		// Our flow layout adds buttons from right to left. So btnRight button
		// should be added first
		footerPanelInner.add(btnRight);
		footerPanelInner.add(btnCenter);
		footerPanelInner.add(btnLeft);
		footerPanel.add(footerPanelInner);
		physicalDetails.add(footerPanel, BorderLayout.SOUTH);
	}

	private void initStep3() {
		gymPlanDetails.setLayout(new BorderLayout());

		// Set border for Gym Plan Details Panel
		gymPlanDetails.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));

		// Add header panel to North
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		PM_Label lblHeader = new PM_Label("Step 3 - Please fill gym plan details");
		headerPanel.add(lblHeader);
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		headerPanel.add(new JSeparator());
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		gymPlanDetails.add(headerPanel, BorderLayout.NORTH);

		// Add inputs panel to Center
		JPanel inputsPanel = new JPanel();
		inputsPanel.setLayout(new BoxLayout(inputsPanel, BoxLayout.Y_AXIS));

		PM_Label lblDateJoined = new PM_Label("Joining date", Font.BOLD);
		memDateJoined = new PM_DatePicker("dd/MM/yyyy", new Date());
		memDateJoined.getDateEditor().addPropertyChangeListener(this);
		PM_Label lblGymPlan = new PM_Label("Choose gym plan", Font.BOLD);

		memPlanName = new JComboBox<GymPlan>();
		memPlanName.setAlignmentX(Component.LEFT_ALIGNMENT);
		// Get all GymPlan from database and add them to the combo box
		availableGymPlans = db.getAllGymPlans();
		for (GymPlan gymPlan : availableGymPlans)
			memPlanName.addItem(gymPlan);
		memPlanName.addItemListener(this);

		PM_Label lblFeePaidForNMonths = new PM_Label("Subscription months", Font.BOLD);
		memFeePaidForNMonths = new JComboBox<Integer>();
		memFeePaidForNMonths.setAlignmentX(Component.LEFT_ALIGNMENT);
		memFeePaidForNMonths.addItem(1);
		memFeePaidForNMonths.addItem(3);
		memFeePaidForNMonths.addItem(6);
		memFeePaidForNMonths.addItem(12);
		memFeePaidForNMonths.addItemListener(this);

		PM_Label lblAmountPayable = new PM_Label("Amount Payable", Font.BOLD);
		memAmountPayable = new PM_Label();

		PM_Label lblNextRenewal = new PM_Label("Next Renewal", Font.BOLD);
		memNextRenewal = new PM_Label();

		// Initially set next renewal as 1 month from today
		String nextRenewal = Calculate.addMonthsToDate(new Date(), 1);
		memNextRenewal.setText(nextRenewal);

		inputsPanel.add(lblDateJoined);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memDateJoined);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		inputsPanel.add(lblGymPlan);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memPlanName);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		inputsPanel.add(lblFeePaidForNMonths);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memFeePaidForNMonths);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		inputsPanel.add(lblAmountPayable);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memAmountPayable);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		inputsPanel.add(lblNextRenewal);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(memNextRenewal);
		step3Msg = new PM_Label();
		step3Msg.setVisible(false); // Hide msg label initially
		step3Msg.setForeground(Color.RED);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(step3Msg);
		gymPlanDetails.add(inputsPanel, BorderLayout.CENTER);

		// Prepare and add footer to South
		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
		JPanel footerPanelInner = new JPanel();
		footerPanelInner.setLayout(new FlowLayout(FlowLayout.RIGHT));
		footerPanelInner.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		btnLeft = new JButton("Previous");
		btnLeft.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnLeft.addActionListener(this);
		btnCenter = new JButton("Update");
		btnCenter.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnCenter.addActionListener(this);
		btnRight = new JButton("Cancel");
		btnRight.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnRight.addActionListener(this);

		footerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		footerPanel.add(new JSeparator());
		// Our flow layout adds buttons from right to left. So btnRight button
		// should be added first
		footerPanelInner.add(btnRight);
		footerPanelInner.add(btnCenter);
		footerPanelInner.add(btnLeft);
		footerPanel.add(footerPanelInner);
		gymPlanDetails.add(footerPanel, BorderLayout.SOUTH);
	}

	private class ValidateWorker extends WorkerThread {

		private boolean areInputsOk;
		private boolean commitSuccess;
		private boolean commitCancelled = false;

		@Override
		public void preExecute() {
			if (currentStep == Step.STEP1) {
				step1Msg.setText("Validing your inputs. Please wait...");
				step1Msg.setVisible(true);
			} else if (currentStep == Step.STEP2) {
				step2Msg.setText("Validing your inputs. Please wait...");
				step2Msg.setVisible(true);
			} else {
				step3Msg.setText("Validing your inputs. Please wait...");
				step3Msg.setVisible(true);
			}
		}

		@Override
		public void executeAsync() {
			if (currentStep == Step.STEP1)
				validateStep1Inputs();
			else if (currentStep == Step.STEP2)
				validateStep2Inputs();
			else {
				validateStep3Inputs();
				if (areInputsOk) {
					// If all inputs are valid, just confirm with the user
					// before
					// commiting
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							step3Msg.setVisible(false);
						}
					});

					int response = JOptionPane.showConfirmDialog(null, "Do you want to save the information?",
							"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.NO_OPTION) {
						commitCancelled = true;
						return;
					}
					commit();
				}
			}
		}

		@Override
		public void postExecute() {
			if (areInputsOk) {

				if (currentStep == Step.STEP1) {
					// If all inputs are acceptable, move to step 2. For
					// this we
					// have remove personalDetails panel from the frame and
					// add
					// physicalDetails panel to it
					step1Msg.setVisible(false);
					mainWindow.remove(personalDetails);
					mainWindow.add(physicalDetails);
					mainWindow.invalidate();
					mainWindow.validate();
					mainWindow.repaint();
					currentStep = Step.STEP2;
				} else if (currentStep == Step.STEP2) {
					// If all inputs are acceptable, move to step 3. For this we
					// have remove physicalDetails panel from the frame and add
					// fingerprintDetails panel to it
					step2Msg.setVisible(false);
					mainWindow.remove(physicalDetails);
					mainWindow.add(gymPlanDetails);
					mainWindow.invalidate();
					mainWindow.validate();
					mainWindow.repaint();
					currentStep = Step.STEP3;
				} else {

					// If commit was cancelled just return
					if (commitCancelled)
						return;

					// Remember we hid step3Msgs before we showed the confirm
					// dialog
					step3Msg.setVisible(true);

					// If all inputs were accepted, check if the commit in db
					// was success
					if (commitSuccess) {
						btnLeft.setVisible(false);
						btnCenter.setVisible(false);
						btnRight.setText("Finish");
						freeze();
						step3Msg.setText(memName.getText()
								+ " updated successfully. Please click on Finish to close this window");
					} else {
						step3Msg.setText(
								"Some error occured while updating " + memName.getText() + ". Please try later");
					}
				}
			}

		}

		/*
		 * Validates all inputs in Step1 for correctness and returns true if
		 * they are acceptable. Otherwise returns false
		 */
		private void validateStep1Inputs() {
			if (memName.getText().length() < 1) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						step1Msg.setText("Please enter name");
					}
				});

				memName.requestFocus();
				areInputsOk = false;
				return;
			}

			if (memPhone.getText().length() < 1) {

				EventQueue.invokeLater(new Runnable() {

					public void run() {
						step1Msg.setText("Please enter phone number");
					}
				});

				memPhone.requestFocus();
				areInputsOk = false;
				return;
			} else if (memPhone.getText().length() > 10) { // If phone number is
															// more than 10
															// chars

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						step1Msg.setText("Please enter a valid phone number");
					}
				});

				memPhone.requestFocus();
				areInputsOk = false;
				return;
			} else

			{ // If phone number is exactly 10 chars, check if they are
				// all numbers

				// Regex pattern of 10 digit phone number
				Pattern pattern = Pattern.compile("\\d{10}");
				Matcher matcher = pattern.matcher(memPhone.getText());

				if (!matcher.matches()) {

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							step1Msg.setText("Please enter a valid phone number");
						}
					});

					memPhone.requestFocus();
					areInputsOk = false;
					return;
				}
			}

			try

			{

				if (memDob.getDate() == null) {

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							step1Msg.setText("Please select date of birth");
						}
					});

					memDob.requestFocus();
					areInputsOk = false;
					return;
				}
			} catch (

			Exception e)

			{

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						step1Msg.setText("Please select date of birth");
					}
				});

				memDob.requestFocus();
				areInputsOk = false;
				return;
			}

			if (memAddr.getText().length() < 1)

			{

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						step1Msg.setText("Please enter address");
					}
				});

				memAddr.requestFocus();
				areInputsOk = false;
				return;
			}
			// If we reached this point, all inputs of step 1 are valid
			areInputsOk = true;

		}

		/*
		 * Validates all inputs in Step2 for correctness and returns true if
		 * they are acceptable. Otherwise returns false
		 */
		private void validateStep2Inputs() {
			// Height is not mandatory. But if it is not empty, it should be a
			// proper floating point number
			if (memHeight.getText().length() > 0) {
				try {
					Float.parseFloat(memHeight.getText());
				} catch (Exception e) {

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							step2Msg.setText("Please enter a valid height");
						}
					});

					areInputsOk = false;
					memHeight.requestFocus();
					return;
				}
			} else {
				memHeight.setText("0");
			}

			// Weight is not mandatory. But if it is not empty, it should be a
			// proper floating point number
			if (memWeight.getText().length() > 0) {
				try {
					Float.parseFloat(memWeight.getText());
				} catch (Exception e) {

					EventQueue.invokeLater(new Runnable() {

						public void run() {
							step2Msg.setText("Please enter a valid weight");
						}

					});

					areInputsOk = false;
					memWeight.requestFocus();
					return;
				}
			} else {
				memWeight.setText("0");
			}

			// If we reached here all inputs of step 2 are valid
			areInputsOk = true;
		}

		/*
		 * Validates all inputs in Step3 for correctness and returns true if
		 * they are acceptable. Otherwise returns false
		 */
		private void validateStep3Inputs() {
			try {

				if (memDateJoined.getDate() == null) {

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							step3Msg.setText("Please select date of joining");
						}
					});

					memDateJoined.requestFocus();
					areInputsOk = false;
					return;
				}
			} catch (Exception e) {

				EventQueue.invokeLater(new Runnable() {

					public void run() {
						step3Msg.setText("Please select date of joining");
					}

				});

				memDateJoined.requestFocus();
				areInputsOk = false;
				return;
			}

			areInputsOk = true;
		}

		private void freeze() {
			memDateJoined.setEnabled(false);
			memPlanName.setEnabled(false);
			memFeePaidForNMonths.setEnabled(false);
		}

		private void commit() {

			if (!defaultUserPic.equals(memPhoto.getIcon().toString())) {
				File sourceFile = new File(memPhoto.getIcon().toString());
				File destinationFile = new File("photos/" + newPhotoName);
				try {
					Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					commitSuccess = false;
					EventLog.e(tag, e);
					return;
				}
			}

			// Update MemberDetails object from the input fields and commit it
			// in db

			md.setName(memName.getText());
			md.setPicLocation(newPhotoLocation);
			md.setDob(memDob.getDate());
			md.setPh(memPhone.getText());
			md.setAddr(memAddr.getText());
			md.setEmail(memEmail.getText());
			md.setHeightCm(Integer.parseInt(memHeight.getText()));
			md.setWeightKg(Integer.parseInt(memWeight.getText()));
			md.setBloodGroup(memBloodGroup.getText());
			md.setDateJoined(memDateJoined.getDate());
			md.setPlanId(((GymPlan) memPlanName.getSelectedItem()).getId());
			md.setFeePaidForNMonth(((Integer) memFeePaidForNMonths.getSelectedItem()).intValue());

			String dateString = memNextRenewal.getText();
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
			Date nextRenewalDate;
			try {
				nextRenewalDate = format.parse(dateString);
				md.setNextRenewal(nextRenewalDate);
			} catch (ParseException e) {
				md.setNextRenewal(new Date());
				EventLog.e(tag, e);
			}

			md.setLastSeenOn(new Date());
			md.setIsVerified(0);

			commitSuccess = db.updateMember(md);
			if (!commitSuccess)
				return;
			if (listener != null) {
				System.out.println("Calling event listener");
				listener.eventOccured(EventType.Member_Modified);
			}
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
