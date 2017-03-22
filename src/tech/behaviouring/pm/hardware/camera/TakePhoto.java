package tech.behaviouring.pm.hardware.camera;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPicker;
import com.github.sarxos.webcam.WebcamResolution;

import tech.behaviouring.pm.util.*;

/*
 * Created by Mohan on 30/12/2015
 */

public class TakePhoto implements Runnable, ActionListener, WebcamListener, WindowListener, ItemListener,
		WebcamDiscoveryListener, UncaughtExceptionHandler {

	// Whether this activity is up
	private static boolean isActive = false;

	private String tag = "Take Photo";
	private Webcam webcam;
	private WebcamPicker picker = null;
	private JFrame window;
	private WebcamPanel panel;

	// Callback code
	private TakePhotoListener callback;

	public TakePhoto(TakePhotoListener callback) {
		isActive = true;
		this.callback = callback;
	}

	// Implement Runnable interface

	public void run() {
		initCamera();
	}

	private void initCamera() {
		picker = new WebcamPicker();
		picker.setSelectedIndex(picker.getItemCount() - 1);
		picker.addItemListener(this);

		webcam = picker.getSelectedWebcam();
		webcam.setViewSize(WebcamResolution.QVGA.getSize());

		panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		// panel.setDisplayDebugInfo(true);
		panel.setImageSizeDisplayed(true);
		// panel.setMirrored(true);

		JButton takePicture = new JButton("Take Photo");
		takePicture.addActionListener(this);

		window = new JFrame("Capture a new photo");
		window.add(picker, BorderLayout.NORTH);
		window.add(panel, BorderLayout.CENTER);

		// If there is no webcam, we need not add Take Photo button
		if (webcam != null) {
			window.add(takePicture, BorderLayout.SOUTH);
		}

		window.setResizable(true);
		window.addWindowListener(this);
		window.setLocationRelativeTo(null);
		window.pack();
		window.setVisible(true);

		Thread t = new Thread() {

			@Override
			public void run() {
				panel.start();
			}
		};
		t.setDaemon(true);
		t.setUncaughtExceptionHandler(this);
		t.start();
	}

	private class ImageCaptureWorker extends WorkerThread {
		@Override
		public void preExecute() {

		}

		@Override
		public void executeAsync() {
			try {
				if (webcam != null) {
					// get image
					BufferedImage image = webcam.getImage();
					// save image to PNG file
					String fileName = "img_" + System.currentTimeMillis() + ".png";
					String fileLocation = "tmp/" + fileName;
					ImageIO.write(image, "PNG", new File(fileLocation));
					webcam.close();
					window.dispose();

					// Send the location of the photo to the callback code
					if (callback != null)
						callback.photoTaken(fileName, fileLocation);
				}
			} catch (Exception e) {
				EventLog.e(tag, e);
			}
		}

		@Override
		public void postExecute() {

		}
	}

	// Window Listener events

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		webcam.close();
		isActive = false;
	}

	@Override
	public void windowClosing(WindowEvent e) {
		window.dispose();
		isActive = false;
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		System.out.println("webcam viewer resumed");
		panel.resume();
	}

	@Override
	public void windowIconified(WindowEvent e) {
		System.out.println("webcam viewer paused");
		panel.pause();
	}

	// Uncaught Exception

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		EventLog.e(tag, String.format("Exception in thread %s", t.getName()));
	}

	// Implement ActionListener interface

	public void actionPerformed(ActionEvent evnt) {
		String buttonClicked = evnt.getActionCommand();
		if (buttonClicked.equals("Take Photo")) {
			// Click picture and save it to disk on a new thread
			new ImageCaptureWorker().start();
		}
	}

	// Item Changed iterface

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getItem() != webcam) {
			if (webcam != null) {

				panel.stop();

				window.remove(panel);

				webcam.removeWebcamListener(this);
				webcam.close();

				webcam = (Webcam) e.getItem();
				webcam.setViewSize(WebcamResolution.QVGA.getSize());
				webcam.addWebcamListener(this);

				System.out.println("selected " + webcam.getName());

				panel = new WebcamPanel(webcam, false);
				panel.setFPSDisplayed(true);

				window.add(panel, BorderLayout.CENTER);
				window.pack();

				Thread t = new Thread() {

					@Override
					public void run() {
						panel.start();
					}
				};
				t.setUncaughtExceptionHandler(this);
				t.setDaemon(true);
				t.start();
			}
		}
	}

	// Webcam events

	@Override
	public void webcamOpen(WebcamEvent we) {
		System.out.println("webcam open");
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		System.out.println("webcam closed");
	}

	@Override
	public void webcamDisposed(WebcamEvent we) {
		System.out.println("webcam disposed");
	}

	@Override
	public void webcamImageObtained(WebcamEvent we) {
		// do nothing
	}

	// Webcam discovery events

	@Override
	public void webcamFound(WebcamDiscoveryEvent event) {
		if (picker != null) {
			picker.addItem(event.getWebcam());
		}
	}

	@Override
	public void webcamGone(WebcamDiscoveryEvent event) {
		if (picker != null) {
			picker.removeItem(event.getWebcam());
		}
	}

	public static boolean isActive() {
		return isActive;
	}
}
