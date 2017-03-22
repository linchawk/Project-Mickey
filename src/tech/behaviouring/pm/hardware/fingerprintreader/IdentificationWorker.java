package tech.behaviouring.pm.hardware.fingerprintreader;

import java.util.List;

import CogentBioSDK.CgtBioSdkApi;
import tech.behaviouring.pm.core.applogic.objects.MemberDetails;
import tech.behaviouring.pm.core.applogic.objects.SingleFingerprint;
import tech.behaviouring.pm.core.database.DBOperations;
import tech.behaviouring.pm.hardware.fingerprintreader.FpCaptureListener.Finger;

/*
 * Created by Mohan on 30/1/2016
 */

public class IdentificationWorker extends Thread {

	// Minimum score each fingerprint template should get
	private int MINSCORE = 5000;
	// The finger which we are matching the score for
	private Finger finger;
	// The claimed ISO fingerprint template
	private byte[] claimedFpTemplate;
	// List of available fingerprint templates
	private List<SingleFingerprint> referenceFpTemplates;
	// DB instance
	private DBOperations db;
	// The guy who we have to inform after completing the identification process
	private IdentificationListener listener;
	// Identified member based on the match scores
	private int identifiedMemberId;

	public IdentificationWorker(Finger finger, byte[] fpTemplate, IdentificationListener listener) {
		this.finger = finger;
		this.claimedFpTemplate = fpTemplate;
		this.listener = listener;
		this.identifiedMemberId = -1;
	}

	@Override
	public void run() {

		if (claimedFpTemplate == null)
			return;

		System.out.println("Identification process statrts");
		double start = System.currentTimeMillis();

		getReferenceFpTemplates();
		matchTemplates();

		double end = System.currentTimeMillis();
		System.out.println("Identification process ends. Took " + ((end - start) / 1000) + " seconds");

		MemberDetails md = null;

		if (identifiedMemberId != -1) {
			db = DBOperations.getInstance();
			md = db.getMemberById(identifiedMemberId);
			System.out.println("Member name: " + md.getName());
		}

		if (listener != null)
			listener.onIdentificationCompleted(md);
	}

	private void getReferenceFpTemplates() {
		FpTemplates fpTemplates = FpTemplates.getInstance();
		referenceFpTemplates = fpTemplates.getFingerprintTemplates(finger);
	}

	private void matchTemplates() {
		int maxScore = 0;
		int claimedFpTemplateSize = claimedFpTemplate.length;
		for (SingleFingerprint referenceFpTemplate : referenceFpTemplates) {
			int score = CgtBioSdkApi.matchTemplates(referenceFpTemplate.getFpTemplate(), claimedFpTemplate,
					referenceFpTemplate.getFpTemplateSize(), claimedFpTemplateSize);
			System.out.println("Id:" + referenceFpTemplate.getMemberId() + ", Score: " + score);
			if ((score > MINSCORE) && (score > maxScore)) {
				maxScore = score;
				identifiedMemberId = referenceFpTemplate.getMemberId();
				System.out.println("Current Identified Member: " + identifiedMemberId);
			}
		}
	}

}
