package tech.behaviouring.pm.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tech.behaviouring.pm.core.applogic.PM_Event.EventType;
import tech.behaviouring.pm.core.applogic.PM_EventListener;
import tech.behaviouring.pm.core.applogic.objects.GymPlan;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.util.*;
import tech.behaviouring.pm.ui.widgets.*;

/*
 * Created by Mohan on 15/12/2015
 */

public class GymPlanSetup extends PM_Activity implements DocumentListener {

	// Whether this activity is up
	private static boolean isActive = false;

	private int BORDER_WIDTH = 25;
	private final String tag = "Gym Plan Setup";
	private PM_TextField txtPlanName;
	private PM_TextField txtFee1Month;
	private PM_TextField txtFee3Month;
	private PM_TextField txtFee6Month;
	private PM_TextField txtFee12Month;
	private PM_Label lblMsg;
	private JButton btnCancel;
	private JButton btnCreate;
	private DBOperations db;
	// If any guy is interested in GymPlan CRUD events
	private PM_EventListener listener;

	private GymPlan gymPlan;
	private boolean isEditMode;

	public GymPlanSetup(GymPlan gymplan) {
		isActive = true;
		this.gymPlan = gymplan;
		isEditMode = false;
		listener = null;
	}

	public void addEventListener(PM_EventListener listener) {
		this.listener = listener;
	}

	@Override
	public void run() {
		try {
			init();
			if (gymPlan != null) { // Get the gymplan object. A null object
									// indicates, the window is not in edit
									// mode
				isEditMode = true;
				fetchAndFillGymPlanInfo();
			}
		} catch (Exception ex) {
			EventLog.e(tag, "Exception in Gym Plan Setup window");
			EventLog.e(tag, ex);
		}
	}

	/*
	 * Creates the UI for getting general info
	 */

	private void init() {
		mainWindow.setTitle("Create gym plan");

		JPanel containerPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(containerPanel, BoxLayout.Y_AXIS);
		containerPanel.setLayout(boxLayout);
		containerPanel.setBorder(new EmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		containerPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		PM_Label lblHeader = new PM_Label("Please fill details about the gym plan");
		PM_Label lblPlanName = new PM_Label("Plan name", Font.BOLD);
		txtPlanName = new PM_TextField();
		PM_Label lblFee1Month = new PM_Label("Fee for 1 month", Font.BOLD);
		txtFee1Month = new PM_TextField();
		// Add document listener to fee1Month input field. We will calculate fee
		// for the rest of the months as and when user inputs value for this
		// field
		txtFee1Month.getDocument().addDocumentListener(this);
		PM_Label lblFee3Month = new PM_Label("Fee for 3 months", Font.BOLD);
		txtFee3Month = new PM_TextField();
		PM_Label lblFee6Month = new PM_Label("Fee for 6 months", Font.BOLD);
		txtFee6Month = new PM_TextField();
		PM_Label lblFee12Month = new PM_Label("Fee for 12 months", Font.BOLD);
		txtFee12Month = new PM_TextField();
		lblMsg = new PM_Label();
		lblMsg.setForeground(Color.RED);
		lblMsg.setVisible(false);

		btnCreate = new JButton("Create");
		btnCreate.setFont(FontLoader.getFont(Font.BOLD, 12));
		btnCreate.addActionListener(this);
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
		actionButtonPanel.add(btnCreate);

		containerPanel.add(lblHeader);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(new JSeparator());
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(lblPlanName);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(txtPlanName);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblFee1Month);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(txtFee1Month);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblFee3Month);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(txtFee3Month);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblFee6Month);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(txtFee6Month);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 2));
		containerPanel.add(lblFee12Month);
		containerPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		containerPanel.add(txtFee12Month);
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

	private void fetchAndFillGymPlanInfo() {
		txtPlanName.setText(gymPlan.getName());
		txtFee1Month.setText(gymPlan.getFee1Month() + "");
		txtFee3Month.setText(gymPlan.getFee3Month() + "");
		txtFee6Month.setText(gymPlan.getFee6Month() + "");
		txtFee12Month.setText(gymPlan.getFee12Month() + "");
		lblMsg.setVisible(false);
		btnCreate.setText("Update");
		btnCancel.setText("Cancel");
		txtPlanName.requestFocus();
	}

	private void resetWindow() {
		txtPlanName.setText("");
		txtFee1Month.setText("");
		txtFee3Month.setText("");
		txtFee6Month.setText("");
		txtFee12Month.setText("");
		txtPlanName.setEnabled(true);
		txtFee1Month.setEnabled(true);
		txtFee3Month.setEnabled(true);
		txtFee6Month.setEnabled(true);
		txtFee12Month.setEnabled(true);
		lblMsg.setVisible(false);
		btnCreate.setText("Create");
		btnCancel.setText("Cancel");
		txtPlanName.requestFocus();
	}

	/*
	 * Implement ActionListener interface
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(ActionEvent evnt) {
		String button = evnt.getActionCommand();
		// If the button pressed is save, commit the inputs in db
		if (button.equals("Create") || button.equals("Update")) {
			// Validate the inputs in a worker thread
			new ValidateWorker().start();

		} else if (button.equals("Cancel") || button.equals("Done")) {
			// If the button pressed is Done or Cancel, close the
			// window
			mainWindow.dispose();
		} else if (button.equals("Create More")) {
			// If the button pressed is Create More, reset the form for next gym
			// plan
			resetWindow();
		}
	}

	/*
	 * Implement DocumentListener interface
	 * 
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
	 * DocumentEvent)
	 */

	public void changedUpdate(DocumentEvent documentEvent) {
		calculateFee();
	}

	public void insertUpdate(DocumentEvent documentEvent) {
		calculateFee();
	}

	public void removeUpdate(DocumentEvent documentEvent) {
		calculateFee();
	}

	private void calculateFee() {
		try {
			// Try to parse the value of fee1Month field
			int fee1Month = Integer.parseInt(txtFee1Month.getText());
			// Update other fields accordingly
			txtFee3Month.setText((fee1Month * 3) + "");
			txtFee6Month.setText((fee1Month * 6) + "");
			txtFee12Month.setText((fee1Month * 12) + "");

		} catch (Exception e) {
			// In case of exception we don't have to do anything other than
			// clearing the fields in case we updated values earlier

			txtFee3Month.setText("");
			txtFee6Month.setText("");
			txtFee12Month.setText("");
		}

	}

	/*
	 * This thread validates the values of the input fields in the window
	 */

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
			if (isCommitSuccess) {
				// Let the interested Guy know that a new Gym Plan has been
				// created
				if (listener != null)
					listener.eventOccured(EventType.GymPlan_Created);
			}
		}

		@Override
		public void postExecute() {

			// If commit was cancelled by user just return
			if (commitCancelled)
				return;

			if (areInputsValid) {
				if (isCommitSuccess) {
					if (isEditMode) {
						// Plan update successfully in db. Now disable Create
						// button
						btnCreate.setEnabled(false);
						btnCancel.setText("Done");
						lblMsg.setText("Plan updated successfully. Please click on Done to close this window.");
					} else {
						// Plan created successfully in db. Now switch the roles
						// of
						// Create and Cancel button
						btnCreate.setText("Create More");
						btnCancel.setText("Done");
						lblMsg.setText("Plan created successfully. Please click on Create More to create another plan");
					}
					freeze(); // Disable all input fields
				} else {
					lblMsg.setText("Some error occured while saving information. Please try later");
					EventLog.e(tag, "Error while commiting gym plan in database");
				}
			}
		}

		private void validate() {

			if (txtPlanName.getText().length() < 1) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter plan/ package name");
						txtPlanName.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}

			if (txtFee1Month.getText().length() < 1) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter 1 month fee");
						txtFee1Month.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}
			try {
				if (Integer.parseInt(txtFee1Month.getText()) < 1) {

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							lblMsg.setText("Please enter a valid 1 month fee");
							txtFee1Month.requestFocus();
						}
					});

					areInputsValid = false;
					return;
				}
			} catch (Exception ex) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						lblMsg.setText("Please enter a valid 1 month fee");
						txtFee1Month.requestFocus();
					}
				});

				areInputsValid = false;
				return;
			}

			// If we reached this point, then all inputs are valid
			areInputsValid = true;

			// If all inputs are valid, just confirm with the user
			// before
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
					// Remember we hid lblMsg before showing confirm dialog
					lblMsg.setVisible(true);
				}
			});

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					lblMsg.setText("Saving your information. Please wait...");
				}
			});

			if (!isEditMode)
				isCommitSuccess = commitSave();
			else
				isCommitSuccess = commitEdit();
		}

		private void preCommit() {
			gymPlan.setName(txtPlanName.getText());
			gymPlan.setFee1Month(Integer.parseInt(txtFee1Month.getText()));
			gymPlan.setFee3Month(Integer.parseInt(txtFee3Month.getText()));
			gymPlan.setFee6Month(Integer.parseInt(txtFee6Month.getText()));
			gymPlan.setFee12Month(Integer.parseInt(txtFee12Month.getText()));
		}

		private boolean commitSave() {

			gymPlan = new GymPlan();
			preCommit();

			boolean isCommitSuccess = db.createGymPlan(gymPlan);
			if (isCommitSuccess) {
				isCommitSuccess = db.setValue("plans_setup_complete", "true");
			}
			return isCommitSuccess;

		}

		private boolean commitEdit() {
			preCommit();
			return db.updateGymPlan(gymPlan);
		}

		private void freeze() {
			txtPlanName.setEnabled(false);
			txtFee1Month.setEnabled(false);
			txtFee3Month.setEnabled(false);
			txtFee6Month.setEnabled(false);
			txtFee12Month.setEnabled(false);
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
