package tech.behaviouring.pm.core.applogic;

import java.io.File;
import tech.behaviouring.pm.util.EventLog;

/*
 * Created by Mohan on 4/3/2016
 */

public class FileLock {

	private static String tag = "Lock File";
	private static File lockFile;
	private static boolean lockAcquired = false;

	public static boolean acquireLock() {
		try {

			lockFile = new File("lock/lck.bin");
			if (!lockFile.exists()) {
				lockFile.createNewFile();
				lockAcquired = true;
			} else
				lockAcquired = false;
			return lockAcquired;
		} catch (Exception e) {
			EventLog.e(tag, e);
			return false;
		}

	}

	public static void releaseLock() {
		try {
			if (lockAcquired)
				lockFile.delete();
		} catch (Exception e) {
			EventLog.e(tag, e);
		}
	}

}
