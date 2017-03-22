package tech.behaviouring.pm.ui;

import tech.behaviouring.pm.core.applogic.objects.MemberDetails;
import tech.behaviouring.pm.hardware.fingerprintreader.FpCaptureListener;
import tech.behaviouring.pm.hardware.fingerprintreader.FpReader;
import tech.behaviouring.pm.hardware.fingerprintreader.IdentificationListener;
import tech.behaviouring.pm.hardware.fingerprintreader.IdentificationWorker;
import tech.behaviouring.pm.ui.widgets.PM_FingerprintPanel;
import tech.behaviouring.pm.util.EventLog;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.*;

/*
 * Created by Mohan on 26/1/2016
 */

public class IdentifyMember extends PM_Activity implements FpCaptureListener, IdentificationListener {

	private static final String tag = "Identify Member";

	// Whether this activity is up
	private static boolean isActive = false;

	private PM_FingerprintPanel thumbFingerprintPanel;
	private PM_FingerprintPanel indexFingerprintPanel;

	private FpReader fpReader;

	// AttendanceMarked window
	private AttendanceMarked attendanceMarked;

	// Implement Runnable interface
	@Override
	public void run() {
		try {
			isActive = true;
			fpReader = FpReader.getInstance();
			init();
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

	// Implement FpCaptureListener
	public void onFpCaptured(Finger f, byte[] fpExtractedTemplate) {
		IdentificationWorker identificationWorker = new IdentificationWorker(f, fpExtractedTemplate, this);
		identificationWorker.start();
	}

	// Implement IdentificationListener interface
	public void onIdentificationCompleted(MemberDetails identifiedMember) {
		updateAttendanceMarkedWindow(identifiedMember);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// Unfreeze capture buttons
				thumbFingerprintPanel.setCaptureEnabled(true);
				indexFingerprintPanel.setCaptureEnabled(true);
				fpReader.captureFingerprint(Finger.Thumb, thumbFingerprintPanel, IdentifyMember.this);
			}
		});
	}

	private void init() {
		// Initialize the window
		mainWindow.setTitle("Identify Member");
		thumbFingerprintPanel = new PM_FingerprintPanel("Thumb Fingerprint", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fpReader.captureFingerprint(Finger.Thumb, thumbFingerprintPanel, IdentifyMember.this);

				// Freeze capture buttons till identification process is
				// completed
				thumbFingerprintPanel.setCaptureEnabled(false);
				indexFingerprintPanel.setCaptureEnabled(false);
			}
		});
		indexFingerprintPanel = new PM_FingerprintPanel("Index Fingerprint", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fpReader.captureFingerprint(Finger.Index, indexFingerprintPanel, IdentifyMember.this);

				// Freeze capture buttons till identification process is
				// completed
				thumbFingerprintPanel.setCaptureEnabled(false);
				indexFingerprintPanel.setCaptureEnabled(false);
			}
		});

		mainWindow.getContentPane().add(thumbFingerprintPanel, BorderLayout.CENTER);
		// mainWindow.getContentPane().add(indexFingerprintPanel,
		// BorderLayout.EAST);
		mainWindow.addWindowListener(this);
		mainWindow.setResizable(false);
		mainWindow.pack();
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setVisible(true);

		fpReader.captureFingerprint(Finger.Thumb, thumbFingerprintPanel, IdentifyMember.this);

	}

	// Update AttendanceMarked window with the identified member

	private void updateAttendanceMarkedWindow(MemberDetails md) {
		if (md == null) {
			attendanceMarked.closeActivity();
			return;
		}
		attendanceMarked = AttendanceMarked.getInstance();
		attendanceMarked.closeActivity();
		attendanceMarked.setNewMember(md);
		EventQueue.invokeLater(attendanceMarked);
	}

	public void die() {
		isActive = false;
		mainWindow.dispose();
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
