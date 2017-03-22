package tech.behaviouring.pm.core.applogic.objects;

import tech.behaviouring.pm.hardware.fingerprintreader.FpCaptureListener.Finger;

/*
 * Created by Mohan on 29/1/2016
 */

public class Fingerprint {

	private int memberId;
	private byte[] thumbFingerTemplate;
	private byte[] indexFingerTemplate;

	public Fingerprint() {
		/*
		 * Dum dum dum
		 */
	}

	public void setMemberId(int memId) {
		memberId = memId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setFingerprintTemplate(Finger finger, byte[] fingerprintTemplate) {
		if (finger == Finger.Thumb)
			thumbFingerTemplate = fingerprintTemplate;
		else if (finger == Finger.Index)
			indexFingerTemplate = fingerprintTemplate;
	}

	public byte[] getFingerprintTemplate(Finger finger) {
		if (finger == Finger.Thumb)
			return thumbFingerTemplate;
		else
			return indexFingerTemplate;
	}

}
