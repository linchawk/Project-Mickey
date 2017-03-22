package tech.behaviouring.pm.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.ui.widgets.*;
import tech.behaviouring.pm.ui.widgets.PM_Label;
import tech.behaviouring.pm.util.*;

/*
 * Created by Mohan on 12/12/2015
 */

public class GeneralSetup extends PM_Activity {

	// Whether this activity is up
	private static boolean isActive = false;

	private boolean editMode;
	private int BORDER_WIDTH = 25;
	private final String tag = "General Setup";
	PM_TextField txtGymName;
	PM_TextArea txtGymAddress;
	PM_TextField txtAdmissionFee;
	PM_TextField txtAdminUser;
	PM_PasswordField txtAdminPassword;
	PM_PasswordField txtConfirmAdminPassword;
	PM_Label lblMsg;
	JButton btnSave;
	JButton btnCancel;
	DBOperations db;

	/*
	 * Indicates whether the window is being used for getting inputs for the
	 * first time or existing inputs are being edited
	 */

	public GeneralSetup(boolean isEditMode) {
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
			EventLog.e(tag, "Exception in General Setup window");
			EventLog.e(tag, ex);
		}
	}

	/*
	 * Creates the UI for getting general info
	 */

	private void init() {
		mainWindow.setTitle("General info");

		JPanel containerPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(containerPanel, BoxLayout.Y_AXIS);
		containerPanel.setLayout(boxLayout);
		containerPanel.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		containerPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		PM_Label lblHeader = new PM_Label("Please fill general details about your gym");
		PM_Label lblGymName = new PM_Label("Gym name", Font.BOLD);
		txtGymName = new PM_TextField();
		PM_Label lblGymAddress = new PM_Label("Gym address", Font.BOLD);
		txtGymAddress = new PM_TextArea("Tolichowki", 5, 10);
		PM_Label lblAdmissionFee = new PM_Label("Admission fee", Font.BOLD);
		txtAdmissionFee = new PM_TextField();
		PM_Label lblAdminUser = new PM_Label("Admin user name", Font.BOLD);
		txtAdminUser = new PM_TextField();
		PM_Label lblAdminPassword = new PM_Label("Admin password", Font.BOLD);
		txtAdminPassword = new PM_PasswordField();
		PM_Label lblConfirmAdminPassword = new PM_Label("Confirm admin password", Font.BOLD);
		txtConfirmAdminPassword = new PM_PasswordField();
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
		containerPanel.add(lblGymName);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(txtGymName);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblGymAddress);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(txtGymAddress);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblAdmissionFee);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(txtAdmissionFee);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblAdminUser);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(txtAdminUser);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblAdminPassword);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(txtAdminPassword);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblConfirmAdminPassword);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(txtConfirmAdminPassword);
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
		String[][] values = new String[][] { { "gym_name", "" }, { "gym_addr", "" }, { "admission_fee", "" },
				{ "admin_usr", "" } };

		for (int i = 0; i < values.length; i++) {
			// Get the value by sending name
			values[i][1] = db.getValue(values[i][0]);
		}

		txtGymName.setText(values[0][1]);
		txtGymAddress.setText(values[1][1]);
		txtAdmissionFee.setText(values[2][1]);
		txtAdminUser.setText(values[3][1]);
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
					lblMsg.setText("Initial setup complete. Please click on Finish to close this window");
				} else {
					lblMsg.setText("Some error occured while saving information. Please try later");
					EventLog.e(tag, "Error while commiting inputs in database");
				}
			}
		}

		private void validate() {

			if (txtGymName.getText().length() < 1) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter gym name");
						txtGymName.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}

			if (txtGymAddress.getText().length() < 1) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter gym address");
						txtGymAddress.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}

			if (txtAdmissionFee.getText().length() < 1) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter admission fee");
						txtAdmissionFee.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}
			try {
				if (Integer.parseInt(txtAdmissionFee.getText()) < 1) {

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							lblMsg.setText("Please enter a valid admission fee");
							txtAdmissionFee.requestFocus();
						}
					});

					areInputsValid = false;
					return;
				}
			} catch (Exception ex) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter a valid admission fee");
						txtAdmissionFee.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}

			if (txtAdminUser.getText().length() < 1) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please admin user name");
						txtAdminUser.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}

			if ((txtAdminPassword.getPassword().length < 1)) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter password");
						txtAdminPassword.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}

			if ((txtConfirmAdminPassword.getPassword().length < 1)) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please confirm password");
						txtConfirmAdminPassword.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}

			String password = String.valueOf(txtAdminPassword.getPassword());
			String confirmPassword = String.valueOf(txtConfirmAdminPassword.getPassword());

			if (!(password.equals(confirmPassword))) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Passwords do not match. Please try again");
						txtAdminPassword.setText("");
						txtConfirmAdminPassword.setText("");
						txtAdminPassword.requestFocus();
					}
				});

				areInputsValid = false;
				return;
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
					lblMsg.setText("Saving your information. Please wait...");
				}
			});

			// Save the details in db
			isCommitSuccess = commit();
		}

		private boolean commit() {
			// We will store only the MD5 hash of the password and not the clear
			// password
			Hash pwdHash = new Hash(String.valueOf(txtAdminPassword.getPassword()));
			// Prepare the name value pairs in a 2D string array
			String[][] values = new String[][] { { "gym_name", txtGymName.getText() },
					{ "gym_addr", txtGymAddress.getText() }, { "admission_fee", txtAdmissionFee.getText() },
					{ "admin_usr", txtAdminUser.getText() }, { "admin_pwd", pwdHash.getMD5Hash() },
					{ "general_setup_complete", "true" } };

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
			txtGymName.setEditable(false);
			txtGymAddress.setEnabled(false);
			txtAdmissionFee.setEnabled(false);
			txtAdminUser.setEnabled(false);
			txtAdminPassword.setEnabled(false);
			txtConfirmAdminPassword.setEnabled(false);
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
