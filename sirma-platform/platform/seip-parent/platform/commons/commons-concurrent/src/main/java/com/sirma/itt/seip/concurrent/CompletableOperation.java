package com.sirma.itt.seip.concurrent;

import java.util.concurrent.Future;

/**
 * Callback interface for providing result to {@link Future} objects. This is optional operation. Not that if instance
 * of this interface is provided one of the 2 methods {@link #completed(Object)} or {@link #failed(Exception)} should be
 * called to notify that the operation completed in some way.
 *
 * @param <T>
 *            the result type
 * @author BBonev
 */
public interface CompletableOperation<T> {

	/**
	 * When the operation is complete this method should be called to set the result to the future so that it can be
	 * returned.
	 *
	 * @param result
	 *            the result
	 * @return true, if successful
	 */
	boolean completed(T result);

	/**
	 * If the execution completed with exception the exception could be send using this method.
	 *
	 * @param exception
	 *            the exception
	 * @return true, if successful
	 */
	boolean failed(Exception exception);

	/**
	 * Notifies that the operations was canceled internally and no result should be expected.
	 *
	 * @return true, if successful
	 */
	boolean cancel();
}
