package tech.behaviouring.pm.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import tech.behaviouring.pm.core.applogic.PM_Event.EventType;
import tech.behaviouring.pm.core.applogic.PM_EventListener;
import tech.behaviouring.pm.core.applogic.objects.GymPlan;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.ui.widgets.PM_ListSingleRow;
import tech.behaviouring.pm.util.EventLog;
import tech.behaviouring.pm.util.WorkerThread;

/*
 * Created by Mohan on 6/2/2016
 */

public class PlanManagement extends PM_Activity implements PM_EventListener {

	private static final String tag = "Plan Management";

	// Whether this activity is up
	private static boolean isActive = false;

	private DBOperations db;
	private List<GymPlan> gymPlans;
	private JPanel listViewPanel;

	public PlanManagement() {
		isActive = true;
	}

	// Implement PM_EventListener interface
	public void eventOccured(EventType type) {
		if (type == EventType.GymPlan_Created) {
			new UpdateListWorker().start();
		}
	}

	// Implement Runnable interface
	@Override
	public void run() {
		try {
			db = DBOperations.getInstance();
			init();
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	// Implement MouseListener interface

	@Override
	public void mouseClicked(MouseEvent e) {
		JLabel labelClicked = (JLabel) e.getSource();
		System.out.println("Clicked on " + labelClicked.getText() + " for " + labelClicked.getName());
		ListActionWorker listActionWorker = new ListActionWorker(labelClicked.getText(),
				Integer.parseInt(labelClicked.getName()));
		listActionWorker.start();
	}

	private void init() {
		mainWindow.setTitle("Manage Gym Plans");

		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

		JPanel titleRow = new JPanel();
		titleRow.setLayout(new GridLayout(1, 2));
		titleRow.add(new JLabel("Currently available plans"));
		JButton createNewPlan = new JButton("Create A New Plan");
		createNewPlan.setAlignmentX(Component.RIGHT_ALIGNMENT);
		createNewPlan.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!GymPlanSetup.isActive()) {
					GymPlanSetup gymPlanSetup = new GymPlanSetup(null);
					gymPlanSetup.addEventListener(PlanManagement.this);
					EventQueue.invokeLater(gymPlanSetup);
				}
			}

		});
		titleRow.add(createNewPlan);
		titleRow.setBorder(new EmptyBorder(20, 20, 20, 20));
		headerPanel.add(titleRow);
		headerPanel.add(new JSeparator());

		// Populate the listview
		addRowsToListView();

		mainWindow.add(headerPanel, BorderLayout.NORTH);
		mainWindow.add(listViewPanel, BorderLayout.CENTER);

		mainWindow.pack();
		mainWindow.addWindowListener(this);
		mainWindow.setResizable(false);
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setVisible(true);
	}

	private void addRowsToListView() {
		gymPlans = db.getAllGymPlans();
		listViewPanel = new JPanel();
		listViewPanel.setLayout(new BoxLayout(listViewPanel, BoxLayout.Y_AXIS));
		int count = 0;
		for (GymPlan gymPlan : gymPlans) {
			// Alternate bgcolor for odd and even rows
			Color currentRowColor;
			if (count % 2 == 0)
				currentRowColor = Color.WHITE;
			else
				currentRowColor = null;
			listViewPanel.add(
					new PM_ListSingleRow(gymPlan.getName(), String.valueOf(gymPlan.getId()), currentRowColor, this));
			count++;
		}
	}

	// This thread updates the UI with the new gym plan when it is created

	private class UpdateListWorker extends WorkerThread {

		@Override
		public void preExecute() {
			mainWindow.remove(listViewPanel);
		}

		@Override
		public void executeAsync() {
			// Populate the listview to include newly created gym plan
			addRowsToListView();
		}

		@Override
		public void postExecute() {
			mainWindow.add(listViewPanel, BorderLayout.CENTER);
			mainWindow.invalidate();
			mainWindow.validate();
			mainWindow.pack();
			mainWindow.repaint();
		}

	}

	// This thread performs an action such as displaying a gym plan detail based
	// on the command

	private class ListActionWorker extends WorkerThread {

		// GymPlan management command to perform
		private String command;
		// GymPlan id
		private int gymPlanId;

		private boolean commandCancelled = true;

		public ListActionWorker(String command, int gymPlanId) {
			this.command = command;
			this.gymPlanId = gymPlanId;
		}

		@Override
		public void preExecute() {
			// If command is delete, there will be UI update
			if (command.equals("Delete")) {
				// Just confirm with the user
				// before
				// deleting

				int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete?", "Confirm",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION) {
					commandCancelled = true;
					return;
				}
				mainWindow.remove(listViewPanel);
			}
		}

		@Override
		public void executeAsync() {
			if (command.equals("View")) {
				EventQueue.invokeLater(new ViewGymPlan(db.getGymPlanById(gymPlanId)));
				return;
			}
			if (command.equals("Edit")) {
				EventQueue.invokeLater(new GymPlanSetup(db.getGymPlanById(gymPlanId)));
				return;
			}
			if (command.equals("Delete")) {

				// If the command is cancelled just return
				if (commandCancelled)
					return;

				db.deleteGymPlan(gymPlanId);
				// Populate the listview to remove last deleted gym plan
				addRowsToListView();
			}
		}

		@Override
		public void postExecute() {
			mainWindow.add(listViewPanel, BorderLayout.CENTER);
			mainWindow.invalidate();
			mainWindow.validate();
			mainWindow.pack();
			mainWindow.repaint();
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
