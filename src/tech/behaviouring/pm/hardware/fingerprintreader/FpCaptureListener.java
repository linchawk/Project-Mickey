package tech.behaviouring.pm.hardware.fingerprintreader;

/*
 * Created by Mohan on 24/1/2016
 */

public interface FpCaptureListener {

	public enum Finger {
		Thumb, Index
	};

	void onFpCaptured(Finger f, byte[] fpExtractedTemplate);
}
