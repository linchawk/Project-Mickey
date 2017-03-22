package tech.behaviouring.pm.core.applogic.objects;

/*
 * Created by Mohan on 30/1/2016
 */

public class SingleFingerprint {

	private int memberId;
	private byte[] fpTemplate;

	public SingleFingerprint() {
		/*
		 * Dum dum dum
		 */
	}

	public void setMemberId(int memId) {
		this.memberId = memId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setFpTemplate(byte[] fpTemplate) {
		this.fpTemplate = fpTemplate;
	}

	public byte[] getFpTemplate() {
		return fpTemplate;
	}

	public int getFpTemplateSize() {
		return fpTemplate.length;
	}

}
