package tech.behaviouring.pm.hardware.fingerprintreader;

import java.util.List;

import tech.behaviouring.pm.core.applogic.objects.FingerprintWrapper;
import tech.behaviouring.pm.core.applogic.objects.SingleFingerprint;
import tech.behaviouring.pm.hardware.fingerprintreader.FpCaptureListener.Finger;

/*
 * Created by Mohan on 29/1/2016
 */

public class FpTemplates {

	private static FpTemplates instance = null;
	private List<SingleFingerprint> thumbFpTemplates;
	private List<SingleFingerprint> indexFpTemplates;
	private static Object lock = new Object(); // Lock object

	private FpTemplates() {
		/*
		 * FpTemplates is a singleton object. So the constructor is made private
		 * to beat instantiation
		 */
	}

	// Return FpTemplates instance

	public static FpTemplates getInstance() {
		synchronized (lock) {
			if (instance == null) {
				instance = new FpTemplates();
			}
			return instance;
		}
	}

	public void setFingerprintTemplates(FingerprintWrapper fpWrapper) {
		thumbFpTemplates = fpWrapper.getFpTemplates(Finger.Thumb);
		indexFpTemplates = fpWrapper.getFpTemplates(Finger.Index);
		System.out.println(
				"Fingerprints successfully loaded into our app. Total fingerprints : " + fpWrapper.getNFpTemplates());
	}

	public List<SingleFingerprint> getFingerprintTemplates(Finger finger) {
		if (finger == Finger.Thumb)
			return thumbFpTemplates;
		else
			return indexFpTemplates;
	}

}
