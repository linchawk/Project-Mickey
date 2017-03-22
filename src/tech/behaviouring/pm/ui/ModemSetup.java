package tech.behaviouring.pm.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.ui.widgets.*;
import tech.behaviouring.pm.ui.widgets.PM_Label;
import tech.behaviouring.pm.util.*;

/*
 * Created by Mohan on 12/12/2015
 */

public class ModemSetup extends PM_Activity {

	// Whether this activity is up
	private static boolean isActive = false;

	private boolean editMode;
	private int BORDER_WIDTH = 25;
	private final String tag = "Modem Setup";
	PM_TextField modemComPort;
	PM_TextField modemManufacturer;
	PM_TextField modemSmsCenter;
	PM_Label lblMsg;
	JButton btnSave;
	JButton btnCancel;
	DBOperations db;

	/*
	 * Indicates whether the window is being used for getting inputs for the
	 * first time or existing inputs are being edited
	 */

	public ModemSetup(boolean isEditMode) {
		isActive = true;
		editMode = isEditMode;
	}

	@Override
	public void run() {
		try {
			init();
			if (editMode) {
				fetchAndFillGeneralInfo();
			}
		} catch (Exception ex) {
			EventLog.e(tag, "Exception in Modem Setup window");
			EventLog.e(tag, ex);
		}
	}

	/*
	 * Creates the UI for getting general info
	 */

	private void init() {
		mainWindow.setTitle("Modem details");

		JPanel containerPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(containerPanel, BoxLayout.Y_AXIS);
		containerPanel.setLayout(boxLayout);
		containerPanel.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		containerPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		PM_Label lblHeader = new PM_Label("Please modem details");
		PM_Label lblComPort = new PM_Label("Modem COM port", Font.BOLD);
		modemComPort = new PM_TextField();
		PM_Label lblManufacturer = new PM_Label("Modem manufacturer", Font.BOLD);
		modemManufacturer = new PM_TextField();
		PM_Label lblSmsCenter = new PM_Label("SMS center number", Font.BOLD);
		modemSmsCenter = new PM_TextField();
		lblMsg = new PM_Label();
		lblMsg.setForeground(Color.RED);
		lblMsg.setVisible(false);

		btnSave = new JButton("Save");
		btnSave.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnSave.addActionListener(this);
		btnCancel = new JButton("Cancel");
		btnCancel.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnCancel.addActionListener(this);

		JPanel actionButtonPanel = new JPanel();
		actionButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		actionButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		actionButtonPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		// Our flow layout adds buttons from right to left. So Cancel button
		// should be added first
		actionButtonPanel.add(btnCancel);
		actionButtonPanel.add(btnSave);

		containerPanel.add(lblHeader);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(new JSeparator());
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(lblComPort);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(modemComPort);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblManufacturer);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(modemManufacturer);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblSmsCenter);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(modemSmsCenter);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblMsg);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(new JSeparator());
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(actionButtonPanel);

		mainWindow.add(containerPanel);
		mainWindow.addWindowListener(this);
		mainWindow.setMinimumSize(new Dimension(800, 600));
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setResizable(false);
		mainWindow.setVisible(true);

		// Get DBOperations instance after showing the window
		db = DBOperations.getInstance();

	}

	private void fetchAndFillGeneralInfo() {
		// Prepare the name value pairs in a 2D string array
		String[][] values = new String[][] { { "modem_com_port", "" }, { "modem_manufacturer", "" },
				{ "modem_sms_center", "" } };

		for (int i = 0; i < values.length; i++) {
			// Get the value by sending name
			values[i][1] = db.getValue(values[i][0]);
		}

		modemComPort.setText(values[0][1]);
		modemManufacturer.setText(values[1][1]);
		modemSmsCenter.setText(values[2][1]);
	}

	@Override
	public void actionPerformed(ActionEvent evnt) {
		String button = evnt.getActionCommand();
		// If the button pressed is save, commit the inputs in db
		if (button.equals("Save")) {
			// Validate the inputs in a worker thread
			new ValidateWorker().start();

		} else if (button.equals("Cancel") || button.equals("Finish")) {
			// If the button pressed is either cancel or finish, close the
			// window
			mainWindow.dispose();
		}
	}

	private class ValidateWorker extends WorkerThread {
		private boolean areInputsValid;
		private boolean isCommitSuccess;
		private boolean commitCancelled = false;

		@Override
		public void preExecute() {
			lblMsg.setVisible(true);
			lblMsg.setText("Validating information");
		}

		@Override
		public void executeAsync() {
			validate();
		}

		@Override
		public void postExecute() {

			// If user did not confirm commit just return
			if (commitCancelled)
				return;
			if (areInputsValid) {
				if (isCommitSuccess) {
					// All name value pairs committed in the db successfully.
					// Change the label of Cancel button to Finish and hide Save
					// button. We are changing the label of Cancel button and
					// not Save button's to Finish 'cause
					// Cancel is the right most button.
					btnSave.setVisible(false);
					btnCancel.setText("Finish");
					freeze(); // Disable all input fields
					lblMsg.setText("Modem setup complete. Please click on Finish to close this window");
				} else {
					lblMsg.setText("Some error occured while saving information. Please try later");
					EventLog.e(tag, "Error while commiting inputs in database");
				}
			}
		}

		private void validate() {

			if (modemComPort.getText().length() < 1) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter modem COM port");
						modemComPort.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}

			if (modemManufacturer.getText().length() < 1) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter modem manufacturer");
						modemManufacturer.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}

			if (modemSmsCenter.getText().length() < 1) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter SMS center number");
						modemSmsCenter.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			} else if (modemSmsCenter.getText().length() > 13) { // If phone
																	// number is
				// more than 13
				// chars including +91

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter a valid SMS center number");
					}
				});

				modemSmsCenter.requestFocus();
				areInputsValid = false;
				return;
			} else { // If phone number is exactly 10 chars, check if they are
				// all numbers

				// Regex pattern of 10 digit phone number
				Pattern pattern = Pattern.compile("\\+91\\d{10}");
				Matcher matcher = pattern.matcher(modemSmsCenter.getText());

				if (!matcher.matches()) {

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							lblMsg.setText("Please enter a valid SMS center number");
						}
					});

					modemSmsCenter.requestFocus();
					areInputsValid = false;
					return;
				}
			}

			// If we reached this point, then all inputs are valid
			areInputsValid = true;

			// If all inputs are valid, just confirm with the user before
			// commiting
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					lblMsg.setVisible(false);
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
					// Just remember, we hid the label before showing confirm
					// dialog
					lblMsg.setVisible(true);
					lblMsg.setText("Saving modem information. Please wait...");
				}
			});

			// Save the details in db
			isCommitSuccess = commit();
		}

		private boolean commit() {
			// Prepare the name value pairs in a 2D string array
			String[][] values = new String[][] { { "modem_com_port", modemComPort.getText() },
					{ "modem_manufacturer", modemManufacturer.getText() },
					{ "modem_sms_center", modemSmsCenter.getText() }, { "modem_setup_complete", "true" } };

			boolean isCommitSuccess = true;

			for (int i = 0; i < values.length; i++) {
				// If any of the name value pair is not commited in db, break
				// the loop
				isCommitSuccess = db.setValue(values[i][0], values[i][1]);
				if (!isCommitSuccess)
					break;
			}

			return isCommitSuccess;

		}

		private void freeze() {
			modemComPort.setEditable(false);
			modemManufacturer.setEnabled(false);
			modemSmsCenter.setEnabled(false);
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
