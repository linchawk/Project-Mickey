package tech.behaviouring.pm.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.core.applogic.PM_Event.EventType;
import tech.behaviouring.pm.core.applogic.PM_EventListener;
import tech.behaviouring.pm.core.applogic.objects.*;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.hardware.camera.TakePhoto;
import tech.behaviouring.pm.hardware.camera.TakePhotoListener;
import tech.behaviouring.pm.hardware.fingerprintreader.FpCaptureListener;
import tech.behaviouring.pm.hardware.fingerprintreader.FpReader;
import tech.behaviouring.pm.service.SmsService;
import tech.behaviouring.pm.ui.widgets.*;
import tech.behaviouring.pm.util.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by Mohan on 22/12/2015
 */

public class AddNewMember extends PM_Activity implements ItemListener, PropertyChangeListener, FpCaptureListener {
	// Whether this activity is up
	private static boolean isActive = false;

	private String tag = "Add New Member";
	private String defaultUserPic = "res/img/user.png";
	private int BORDER_WIDTH = 20;
	private String newPhotoName;
	private String newPhotoLocation;
	// Extracted thumb and index fingerprint templates
	private byte[] thumbFpTemplate;
	private byte[] indexFpTemplate;
	// Calculated first fee payable by member based on gym plan and no of months
	private int firstPaymentAmount;

	private enum Step {
		STEP1, STEP2, STEP3, STEP4
	};

	// DBOperations object
	private DBOperations db;

	// Fingerprint Reader object
	private FpReader fpReader;

	// The guy who is interested in new member added event
	private PM_EventListener listener;

	// To track which step we are currently in
	private Step currentStep;

	private MemberDetails md;

	// Three panels for three steps
	private JPanel personalDetails;
	private JPanel physicalDetails;
	private JPanel fingerprintDetails;
	private JPanel gymPlanDetails;

	// Step 1 - Personal details widgets
	private PM_Label memId;
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

	// Step 3 - Fingerprint details widgets
	private PM_FingerprintPanel thumbFingerprintPanel;
	private PM_FingerprintPanel indexFingerprintPanel;
	private PM_Label step3Msg;

	// Step 4 - Gym plan details widgets
	private PM_DatePicker memDateJoined;
	private JComboBox<GymPlan> memPlanName;
	private JComboBox<Integer> memFeePaidForNMonths;
	private PM_Label memAmountPayable;
	private PM_Label memNextRenewal;
	private PM_Label step4Msg;

	// Action buttons
	private JButton btnLeft;
	private JButton btnCenter;
	private JButton btnRight;

	public AddNewMember(MemberDetails member, PM_EventListener listener) {
		isActive = true;
		md = member;
		this.listener = listener;
	}

	// Implement Runnable
	@Override
	public void run() {
		try {
			db = DBOperations.getInstance();
			fpReader = FpReader.getInstance();
			thumbFpTemplate = null;
			indexFpTemplate = null;
			if (md == null) {
				init();
			} else {

			}
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	// Implement FpCaptureListener interface

	public void onFpCaptured(Finger finger, byte[] fpExtractedTemplate) {
		if (finger == Finger.Thumb) {
			thumbFpTemplate = fpExtractedTemplate;
		} else {
			indexFpTemplate = fpExtractedTemplate;
		}
		// When the finger is captured successfully, enable capture buttons
		// again
		thumbFingerprintPanel.setCaptureEnabled(true);
		indexFingerprintPanel.setCaptureEnabled(true);
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

		} else if (currentStep == Step.STEP3) {
			if (buttonClicked.equals("Next")) {
				new ValidateWorker().start();
			} else if (buttonClicked.equals("Previous")) {
				mainWindow.remove(fingerprintDetails);
				mainWindow.add(physicalDetails);
				mainWindow.invalidate();
				mainWindow.validate();
				mainWindow.repaint();
				currentStep = Step.STEP2;

			} else {
				mainWindow.dispose();
			}

		} else {
			if (buttonClicked.equals("Create")) {
				new ValidateWorker().start();
			} else if (buttonClicked.equals("Previous")) {
				mainWindow.remove(gymPlanDetails);
				mainWindow.add(fingerprintDetails);
				mainWindow.invalidate();
				mainWindow.validate();
				mainWindow.repaint();
				currentStep = Step.STEP3;

			} else {
				mainWindow.dispose();
				if (buttonClicked.equals("Finish")) {
					// Let the interested guy know a new member has been created
					if (listener != null)
						listener.eventOccured(EventType.Member_Created);
				}
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
			firstPaymentAmount = admissionFee + planAmount;
			memAmountPayable.setText(
					firstPaymentAmount + " (Admission fee: " + admissionFee + ", Plan amount: " + planAmount + ")");
		} catch (Exception e) {
			memAmountPayable.setText("");
		}
	}

	private void init() {
		// Initially make new photo location same as default user pic
		newPhotoLocation = defaultUserPic;

		// Initialize the frame itself

		mainWindow.setTitle("Add new member");
		// Now init the three panels for 4 steps
		personalDetails = new JPanel();
		physicalDetails = new JPanel();
		fingerprintDetails = new JPanel();
		gymPlanDetails = new JPanel();

		// Create step 1 to step 4 widgets
		initStep1();
		initStep2();
		initStep3();
		initStep4();
		// When the frame is shown for the first time, we are in step1. So add
		// Personal Details Panel to the frame
		currentStep = Step.STEP1;
		mainWindow.add(personalDetails);

		mainWindow.setMinimumSize(new Dimension(800, 600));
		mainWindow.addWindowListener(this);
		mainWindow.setLocationRelativeTo(null);
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

		PM_Label lblMemId = new PM_Label("Membership Id", Font.BOLD);
		memId = new PM_Label();
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

		if (md != null) { // If MemberDetails object is null, we are creating
							// new member now. The new member does not have id
							// yet. So no need to add Member Id field.
			inputsPanel.add(lblMemId);
			inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
			inputsPanel.add(memId);
		}
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
		fingerprintDetails.setLayout(new BorderLayout());

		// Set border for Fingerprint Details Panel
		fingerprintDetails.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));

		// Add header panel to North
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		PM_Label lblHeader = new PM_Label("Step 3 - Please register thumb finger print followed by index finger");
		headerPanel.add(lblHeader);
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		headerPanel.add(new JSeparator());
		headerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		fingerprintDetails.add(headerPanel, BorderLayout.NORTH);

		// Add finperprints panel to Center
		JPanel fingerprintsPanel = new JPanel();
		fingerprintsPanel.setLayout(new FlowLayout());

		thumbFingerprintPanel = new PM_FingerprintPanel("Thumb finger print", new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				// Disable capture buttons when capture is in process
				thumbFingerprintPanel.setCaptureEnabled(false);
				indexFingerprintPanel.setCaptureEnabled(false);
				thumbFpTemplate = null;
				fpReader.captureFingerprint(Finger.Thumb, thumbFingerprintPanel, AddNewMember.this);
			}
		});
		indexFingerprintPanel = new PM_FingerprintPanel("Index finger print", new ActionListener() {
			public void actionPerformed(ActionEvent evnt) {
				thumbFingerprintPanel.setCaptureEnabled(false);
				indexFingerprintPanel.setCaptureEnabled(false);
				indexFpTemplate = null;
				fpReader.captureFingerprint(Finger.Index, indexFingerprintPanel, AddNewMember.this);
			}
		});
		fingerprintsPanel.add(thumbFingerprintPanel);
		fingerprintsPanel.add(indexFingerprintPanel);

		fingerprintDetails.add(fingerprintsPanel, BorderLayout.CENTER);

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
		step3Msg = new PM_Label("Error message");
		step3Msg.setVisible(false); // Hide msg label initially
		step3Msg.setForeground(Color.RED);
		footerPanel.add(step3Msg);
		footerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		footerPanel.add(new JSeparator());
		// Our flow layout adds buttons from right to left. So btnRight button
		// should be added first
		footerPanelInner.add(btnRight);
		footerPanelInner.add(btnCenter);
		footerPanelInner.add(btnLeft);
		footerPanel.add(footerPanelInner);
		fingerprintDetails.add(footerPanel, BorderLayout.SOUTH);

	}

	private void initStep4() {
		gymPlanDetails.setLayout(new BorderLayout());

		// Set border for Gym Plan Details Panel
		gymPlanDetails.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));

		// Add header panel to North
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		PM_Label lblHeader = new PM_Label("Step 4 - Please fill gym plan details");
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
		for (GymPlan gymPlan : db.getAllGymPlans())
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
		step4Msg = new PM_Label();
		step4Msg.setVisible(false); // Hide msg label initially
		step4Msg.setForeground(Color.RED);
		inputsPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		inputsPanel.add(step4Msg);
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
		btnCenter = new JButton("Create");
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

		// Calculate amount payable for first time load
		calculateAmountPayable();

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
			} else if (currentStep == Step.STEP3) {
				step3Msg.setText("Validing your inputs. Please wait...");
				step3Msg.setVisible(true);
			} else {
				step4Msg.setText("Validing your inputs. Please wait...");
				step4Msg.setVisible(true);
			}

		}

		@Override
		public void executeAsync() {
			if (currentStep == Step.STEP1)
				validateStep1Inputs();
			else if (currentStep == Step.STEP2)
				validateStep2Inputs();
			else if (currentStep == Step.STEP3)
				validateStep3Inputs();
			else {
				validateStep4Inputs();
				if (areInputsOk) {
					// If all inputs are valid, just confirm with the user
					// before
					// commiting
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							step4Msg.setVisible(false);
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
					mainWindow.add(fingerprintDetails);
					mainWindow.invalidate();
					mainWindow.validate();
					mainWindow.repaint();
					currentStep = Step.STEP3;
				} else if (currentStep == Step.STEP3) {
					// If all inputs are acceptable, move to step 3. For this we
					// have remove fingerprintDetails panel from the frame and
					// add
					// gymPlanDetails panel to it
					step3Msg.setVisible(false);
					mainWindow.remove(fingerprintDetails);
					mainWindow.add(gymPlanDetails);
					mainWindow.invalidate();
					mainWindow.validate();
					mainWindow.repaint();
					currentStep = Step.STEP4;
				} else {
					// If the commit was cancelled just return
					if (commitCancelled)
						return;

					// Remember we hid step4Msg before showing confirm dialog
					step4Msg.setVisible(true);

					// If all inputs were accepted, check if the commit in db
					// was success
					if (commitSuccess) {
						btnLeft.setVisible(false);
						btnCenter.setVisible(false);
						btnRight.setText("Finish");
						freeze();
						step4Msg.setText(
								memName.getText() + " added successfully. Please click on Finish to close this window");
					} else {
						step4Msg.setText("Some error occured while adding " + memName.getText() + ". Please try later");
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
			} else { // If phone number is exactly 10 chars, check if they are
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

			try {

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
			} catch (Exception e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						step1Msg.setText("Please select date of birth");
					}
				});

				memDob.requestFocus();
				areInputsOk = false;
				return;
			}

			if (memAddr.getText().length() < 1) {

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
			if (thumbFpTemplate == null) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						step3Msg.setText("Please register thumb finger print");
					}
				});
				return;
			}

			if (indexFpTemplate == null) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						step3Msg.setText("Please register index finger print");
					}
				});
				return;
			}
			// If we are here then both fingerprints haven been taken
			areInputsOk = true;
		}

		/*
		 * Validates all inputs in Step4 for correctness and returns true if
		 * they are acceptable. Otherwise returns false
		 */
		private void validateStep4Inputs() {
			try {

				if (memDateJoined.getDate() == null) {

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							step4Msg.setText("Please select date of joining");
						}
					});

					memDateJoined.requestFocus();
					areInputsOk = false;
					return;
				}
			} catch (Exception e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						step4Msg.setText("Please select date of joining");
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

			// Create MemberDetails object from the input fields and commit it
			// in db

			Fingerprint fingerprint = new Fingerprint();
			fingerprint.setFingerprintTemplate(Finger.Thumb, thumbFpTemplate);
			fingerprint.setFingerprintTemplate(Finger.Index, indexFpTemplate);

			MemberDetails memberDetails = new MemberDetails();
			memberDetails.setFingerprint(fingerprint);
			memberDetails.setName(memName.getText());
			memberDetails.setPicLocation(newPhotoLocation);
			memberDetails.setDob(memDob.getDate());
			memberDetails.setPh(memPhone.getText());
			memberDetails.setAddr(memAddr.getText());
			memberDetails.setEmail(memEmail.getText());
			memberDetails.setHeightCm(Integer.parseInt(memHeight.getText()));
			memberDetails.setWeightKg(Integer.parseInt(memWeight.getText()));
			memberDetails.setBloodGroup(memBloodGroup.getText());
			memberDetails.setDateJoined(memDateJoined.getDate());
			memberDetails.setPlanId(((GymPlan) memPlanName.getSelectedItem()).getId());
			memberDetails.setFeePaidForNMonth(((Integer) memFeePaidForNMonths.getSelectedItem()).intValue());
			memberDetails.setFirstPaymentAmount(firstPaymentAmount);

			String dateString = memNextRenewal.getText();
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
			Date nextRenewalDate;
			try {
				nextRenewalDate = format.parse(dateString);
				memberDetails.setNextRenewal(nextRenewalDate);
			} catch (ParseException e) {
				memberDetails.setNextRenewal(new Date());
				EventLog.e(tag, e);
			}

			memberDetails.setLastSeenOn(new Date());
			memberDetails.setIsVerified(0);

			commitSuccess = db.createMember(memberDetails);
			// Send welcome sms
			if (commitSuccess)
				sendWelcomeSms(memberDetails);
		}

		private void sendWelcomeSms(MemberDetails memberDetails) {
			SmsService smsService = SmsService.getInstance();
			String welcomeSmsMsg = db.getValue("welcome_sms");
			welcomeSmsMsg = welcomeSmsMsg.replace("%mem_name%", memberDetails.getName());
			welcomeSmsMsg = welcomeSmsMsg.replace("%gym_name%", db.getValue("gym_name"));
			smsService.sendSms(memberDetails.getPh(), welcomeSmsMsg);
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
