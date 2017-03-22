package tech.behaviouring.pm.util;

import java.awt.EventQueue;

/*
 * Created by Mohan on 30/12/2015
 */

/*
 * This class provides a way for executing some task in a separate thread and update the UI on Swing Event Queue.
 * This is mostly like Android's AsyncTask class. The class provides three abstract methods for the sub-classes. They are preExecute, executeAsync and postExecute.
 * preExecute and postExecute will be run on Swing Event Queue. So they are ideal for UI updates. 
 * executeAsync will be run on a new thread. So it can be used for heavy processing
 */

public abstract class WorkerThread extends Thread {

	private String tag = "Worker Thread";

	public WorkerThread() {
		/*
		 * dum dum dum
		 */
	}

	public void run() {
		try {
			// First run preExecute task on Event Queue and wait for it to
			// complete
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					preExecute();
				}
			});

			// After preExecute run executeAsync on this thread
			executeAsync();

			// Finally run postExecute task on Event Queue
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					postExecute();
				}
			});

		} catch (Exception e) {
			EventLog.e(tag, e);
		}

	}

	public abstract void preExecute();

	public abstract void executeAsync();

	public abstract void postExecute();

}
