package tech.behaviouring.pm.core.applogic.objects;

import java.util.List;

import tech.behaviouring.pm.hardware.fingerprintreader.FpCaptureListener.Finger;

/*
 * Created by Mohan on 30/1/2016
 */

public class FingerprintWrapper {

	private List<SingleFingerprint> thumbFpTemplates;
	private List<SingleFingerprint> indexFpTemplates;

	public FingerprintWrapper() {
		/*
		 * Dum dum dum
		 */
	}

	public void setFpTemplates(Finger finger, List<SingleFingerprint> fpTemplates) {
		if (finger == Finger.Thumb) {
			thumbFpTemplates = fpTemplates;
		} else {
			indexFpTemplates = fpTemplates;
		}
	}

	public List<SingleFingerprint> getFpTemplates(Finger finger) {
		if (finger == Finger.Thumb)
			return thumbFpTemplates;
		else
			return indexFpTemplates;
	}

	public int getNFpTemplates() {
		return thumbFpTemplates.size();
	}

}
