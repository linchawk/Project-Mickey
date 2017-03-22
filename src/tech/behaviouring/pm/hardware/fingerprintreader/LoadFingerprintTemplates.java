package tech.behaviouring.pm.hardware.fingerprintreader;

import tech.behaviouring.pm.core.applogic.objects.FingerprintWrapper;
import tech.behaviouring.pm.core.database.DBOperations;

/*
 * Created by Mohan on 27/2/2016
 */

public class LoadFingerprintTemplates implements Runnable {

	private FpTemplatesLoadListener fpTemplatesLoadListener;

	public LoadFingerprintTemplates(FpTemplatesLoadListener fpTemplatesLoadListener) {
		this.fpTemplatesLoadListener = fpTemplatesLoadListener;
	}

	@Override
	public void run() {
		// Finally load all the fingerprint templates from database into our app
		System.out.println("Loading fingerprints into our app");
		DBOperations db = DBOperations.getInstance();
		FingerprintWrapper fpWrapper = db.getAllFingerprints();
		FpTemplates fpTemplatesInstance = FpTemplates.getInstance();
		fpTemplatesInstance.setFingerprintTemplates(fpWrapper);

		// Inform the guy who is interested in this event
		if (fpTemplatesLoadListener != null) {
			System.out.println("Calling Fingerprints Load Listener");
			fpTemplatesLoadListener.onFpTemplatesLoaded(fpWrapper.getNFpTemplates());
		}
	}

}
