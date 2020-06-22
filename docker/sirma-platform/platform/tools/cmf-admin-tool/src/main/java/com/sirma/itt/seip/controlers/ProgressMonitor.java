package com.sirma.itt.seip.controlers;

/**
 * The Interface ProgressMonitor.
 */
public interface ProgressMonitor {

	/**
	 * Sets the progress info. From 1 to 100
	 *
	 * @param total
	 *            the new progress info
	 */
	public void setProgressInfo(int total);

	/**
	 * Invoke on Finish.
	 */
	public void finish();

}
