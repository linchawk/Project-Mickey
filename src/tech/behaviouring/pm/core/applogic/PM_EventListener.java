package tech.behaviouring.pm.core.applogic;

import tech.behaviouring.pm.core.applogic.PM_Event.EventType;

/*
 * Created by Mohan on 7/2/2016
 */

public interface PM_EventListener {

	public void eventOccured(EventType type);

}
