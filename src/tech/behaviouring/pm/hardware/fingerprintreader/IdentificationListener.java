package tech.behaviouring.pm.hardware.fingerprintreader;

import tech.behaviouring.pm.core.applogic.objects.MemberDetails;

/*
 * Created by Mohan on 30/1/2016
 */

public interface IdentificationListener {

	public void onIdentificationCompleted(MemberDetails identifiedMember);

}
