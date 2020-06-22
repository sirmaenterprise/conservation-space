package com.sirma.itt.seip.configuration.sync;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Synchronizer that uses {@link ReadWriteLock} to enforce synchronization
 *
 * @author BBonev
 */
public class ReadWriteSynchronizer implements Synchronizer {

	/** The lock. */
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * Begin read.
	 */
	@Override
	public void beginRead() {
		lock.readLock().lock();
	}

	/**
	 * End read.
	 */
	@Override
	public void endRead() {
		lock.readLock().unlock();
	}

	/**
	 * Begin write.
	 */
	@Override
	public void beginWrite() {
		lock.writeLock().lock();
	}

	/**
	 * End write.
	 */
	@Override
	public void endWrite() {
		lock.writeLock().unlock();
	}

}
