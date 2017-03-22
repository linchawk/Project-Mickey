package tech.behaviouring.pm.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import tech.behaviouring.pm.core.applogic.PM_Event.EventType;
import tech.behaviouring.pm.core.applogic.PM_EventListener;
import tech.behaviouring.pm.core.applogic.objects.MemberDetails;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.ui.widgets.PM_EmptyLabel;
import tech.behaviouring.pm.ui.widgets.PM_ListSingleRow;
import tech.behaviouring.pm.util.EventLog;
import tech.behaviouring.pm.util.WorkerThread;

/*
 * Created by Mohan on 13/2/2015
 */

public class MemberManagement extends PM_Activity implements PM_EventListener {

	private static final String tag = "Member Management";

	// Whether this activity is up
	private static boolean isActive = false;

	private JPanel listViewPanel;
	private JButton btnPrev;
	private JButton btnNext;
	private DBOperations db;
	private List<MemberDetails> members;
	private int totalMembers;
	private int currentPage;
	private int membersPerPage;
	private int currentOffset;

	private int BORDER_WIDTH = 20;

	public MemberManagement() {
		isActive = true;
	}

	// Implement Runnable interface

	@Override
	public void run() {
		try {
			db = DBOperations.getInstance();
			currentPage = 1;
			membersPerPage = 10;
			getMembers();
			init();
		} catch (Exception e) {
			EventLog.e(tag, e);
		}

	}

	private void init() {
		mainWindow.setTitle("Current Members");
		addRowsToListView();
		addFooter();
		setPaging();
		mainWindow.pack();
		mainWindow.addWindowListener(this);
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setVisible(true);
	}

	private void getMembers() {
		currentOffset = (currentPage - 1) * membersPerPage;
		members = db.getAllMembers(currentOffset, membersPerPage);
	}

	private void addRowsToListView() {
		listViewPanel = new JPanel();
		listViewPanel.setLayout(new BoxLayout(listViewPanel, BoxLayout.Y_AXIS));
		// listViewPanel.setBorder(new EmptyBorder(BORDER_WIDTH, 0, 0, 0));
		int count = 0;
		for (MemberDetails member : members) {
			// Alternate bgcolor for odd and even rows
			Color currentRowColor;
			if (count % 2 == 0)
				currentRowColor = Color.WHITE;
			else
				currentRowColor = null;
			listViewPanel
					.add(new PM_ListSingleRow(member.getName(), String.valueOf(member.getId()), currentRowColor, this));
			count++;
		}
		listViewPanel.add(new JSeparator());
		listViewPanel.add(new PM_EmptyLabel(BORDER_WIDTH / 4));
		mainWindow.add(listViewPanel, BorderLayout.CENTER);
	}

	private void addFooter() {
		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		footerPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		btnPrev = new JButton("Previous");
		btnPrev.addActionListener(this);
		btnNext = new JButton("Next");
		btnNext.addActionListener(this);
		footerPanel.add(btnNext);
		footerPanel.add(btnPrev);
		mainWindow.add(footerPanel, BorderLayout.SOUTH);
	}

	private void setPaging() {
		totalMembers = db.getMembersCount();
		int totalMembersDisplayedSoFar = currentPage * membersPerPage;
		boolean nextPageAvailable = false;
		if (totalMembersDisplayedSoFar < totalMembers)
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

	// Implement Mouse Listener interface

	@Override
	public void mouseClicked(MouseEvent e) {
		JLabel labelClicked = (JLabel) e.getSource();
		System.out.println("Clicked on " + labelClicked.getText() + " for " + labelClicked.getName());
		ListActionWorker listActionWorker = new ListActionWorker(labelClicked.getText(),
				Integer.parseInt(labelClicked.getName()));
		listActionWorker.start();

	}

	// Implement PM_EventListener interface

	@Override
	public void eventOccured(EventType type) {
		if (type == EventType.Member_Modified)
			new ListActionWorker("Refresh", -1).run();

	}

	// Implement Action Listener interface

	@Override
	public void actionPerformed(ActionEvent e) {
		String buttonClicked = e.getActionCommand();
		new PagingButtonClickHandler(buttonClicked).start();
	}

	private class PagingButtonClickHandler extends WorkerThread {

		String buttonClicked;

		public PagingButtonClickHandler(String buttonClicked) {
			this.buttonClicked = buttonClicked;
		}

		@Override
		public void preExecute() {
			mainWindow.remove(listViewPanel);

		}

		@Override
		public void executeAsync() {
			if (buttonClicked.equals("Next"))
				currentPage++;
			else
				currentPage--;

			getMembers();
			addRowsToListView();
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

	// This thread performs an action such as displaying a member detail based
	// on the command

	private class ListActionWorker extends WorkerThread {

		// Member management command to perform
		private String command;
		// Member id
		private int memberId;
		private boolean commandCancelled = false;

		public ListActionWorker(String command, int memberId) {
			this.command = command;
			this.memberId = memberId;
		}

		@Override
		public void preExecute() {
			// If command is delete or refresh, there will be UI update
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

			if (command.equals("Refresh")) {
				mainWindow.remove(listViewPanel);
			}
		}

		@Override
		public void executeAsync() {
			if (command.equals("View")) {
				if (!ViewMemberDetails.isActive())
					EventQueue.invokeLater(new ViewMemberDetails(db.getMemberById(memberId)));
				return;
			}
			if (command.equals("Edit")) {
				if (!EditMember.isActive())
					EventQueue.invokeLater(new EditMember(db.getMemberById(memberId), MemberManagement.this));
				return;
			}
			if (command.equals("Delete")) {

				// If the command is cancelled just return
				if (commandCancelled)
					return;

				db.deleteMember(memberId);
				getMembers();
				/*
				 * After deleting a member if current page is greater than 1 and
				 * the number of members returned from db for this page offset
				 * is zero, then we have just deleted the only row in the last
				 * page. In this case we have move to the previous page and
				 * re-render the rows for that page
				 */
				if (currentPage > 1 && members.size() <= 0) {
					currentPage--;
					getMembers();
				}
				renderRows();
				return;
			}
			if (command.equals("Refresh")) {
				System.out.println("Refresh action called");
				getMembers();
				renderRows();
				return;
			}
		}

		@Override
		public void postExecute() {
			/*
			 * Dum dum dum
			 */
		}

		private void renderRows() {
			addRowsToListView();
			setPaging();
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
