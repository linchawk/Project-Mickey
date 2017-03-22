package tech.behaviouring.pm.hardware.fingerprintreader;

import Cogent.*;
import CogentBioSDK.CgtBioSdkApi;
import tech.behaviouring.pm.hardware.fingerprintreader.FpCaptureListener.Finger;
import tech.behaviouring.pm.ui.widgets.PM_FingerprintPanel;
import tech.behaviouring.pm.util.Convert;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
/*
 * Created by Mohan on 23/1/2016
 */

public class FpReader {

	private static FpReader instance = null;
	// Lock object for thread safety
	private static Object lock = new Object();

	// Draw the fingerprint image to this surface
	private PM_FingerprintPanel targetSurface;

	// Send capture finish update to this listener
	private FpCaptureListener listener;

	// Which finger are capturing
	private Finger finger;

	// Captured fingerprint image created from captured byte array
	private BufferedImage capturedImage;

	private boolean captureFinished;

	// Make constructor private to prevent intanziation
	private FpReader() {

	}

	// Return FpReader instance

	public static FpReader getInstance() {
		synchronized (lock) {
			if (instance == null) {
				instance = new FpReader();
			}
			return instance;
		}
	}

	public void captureFingerprint(Finger finger, PM_FingerprintPanel targetSurface, FpCaptureListener listener) {
		this.finger = finger;
		this.targetSurface = targetSurface;
		this.listener = listener;
		Thread captureThread = new Thread(new CaptureWorker());
		captureThread.start();
	}

	public void updateTargetSurface(byte[] capturedImageByteArray) {
		if (capturedImageByteArray == null) {
			return;
		} else {
			capturedImage = null;
			try {
				capturedImage = ImageIO.read(new ByteArrayInputStream(capturedImageByteArray));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			targetSurface.setImage(capturedImage.getScaledInstance(
					(int) (capturedImage.getWidth() / ((double) capturedImage.getHeight() / 240)), 240,
					Image.SCALE_DEFAULT));
		}
	}

	final class CaptureWorker implements Runnable {

		public CaptureWorker() {

		}

		@Override
		public void run() {

			byte[] capturesBytes = captureFpImage();
			updateTargetSurface(capturesBytes);
			Toolkit.getDefaultToolkit().beep();
			targetSurface.repaint();

			if (CgtFpCaptureAccess.cgtFingerPrintGetLastErrorCode() == -1111) {
				JOptionPane.showMessageDialog(null, "NOT a Live Fingerprint.", "Alert",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}

		private byte[] captureFpImage() {

			byte[] imageByteArray = null;
			captureFinished = false;

			try {

				System.out.println("Capturing...");
				Thread snapShotThread = new Thread(new SnapShotWorker());
				snapShotThread.start();

				imageByteArray = CgtFpCaptureAccess.cgtFingerPrintCaptureStart(1,
						CgtFpCaptureAccess.CG4_FLAT_SINGLE_FINGER, CgtFpCaptureAccess.CG4_IMAGE_RESOLUTION_500, true,
						true);
				System.out.println("Capture Completed.");

			} catch (FpCaptureFailedException fpcfe) {
				System.out.println(fpcfe.toString());
				System.out.println("Error Code: " + CgtFpCaptureAccess.cgtFingerPrintGetLastErrorCode());
			}
			captureFinished = true;
			return imageByteArray;
		}
	}

	final class SnapShotWorker implements Runnable {

		public SnapShotWorker() {

		}

		@Override
		public void run() {
			byte[] imageByteArray = null;
			while (!captureFinished) {
				imageByteArray = CgtFpCaptureAccess.cgtFingerPrintSnapShot();

				if (imageByteArray != null) {
					updateTargetSurface(imageByteArray);
				}
				targetSurface.repaint();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}

			if (listener != null && capturedImage != null) {
				// First extract the image pixel buffer from the captured image
				// byte array
				byte[] imageBuffer = Convert.bmpToRaw(imageByteArray);
				int fingerPosition;
				if (finger == Finger.Thumb)
					fingerPosition = CgtBioSdkApi.L_THUMB;
				else
					fingerPosition = CgtBioSdkApi.L_INDEX;
				// Extract the ISO fingerprint template from image pixel buffer
				byte[] fingerPrintTemplate = Convert.rawToISO(imageBuffer, capturedImage.getHeight(),
						capturedImage.getWidth(), 197, 197, fingerPosition);
				listener.onFpCaptured(finger, fingerPrintTemplate);
			}
		}
	}

}
