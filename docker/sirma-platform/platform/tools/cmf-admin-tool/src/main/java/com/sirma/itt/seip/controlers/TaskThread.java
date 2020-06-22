package com.sirma.itt.seip.controlers;

import java.awt.Toolkit;

import javax.swing.SwingWorker;

/**
 * The Class TaskThread.
 */
public abstract class TaskThread extends SwingWorker<Void, Void>implements ProgressMonitor {

	/*
	 * Executed in event dispatching thread
	 */
	/*
	 * (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	public void done() {
		Toolkit.getDefaultToolkit().beep();
		finish();
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.controlers.ProgressMonitor#setProgressInfo(int)
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProgressInfo(int progress) {
		System.out.println(progress);
		setProgress(progress);
	}
}
