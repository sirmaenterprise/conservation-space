package com.sirma.itt.seip.instance.dao;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * An asynchronous update interface for receiving notifications about OnInstanceLoad information as the OnInstanceLoad
 * is constructed.
 *
 * @author BBonev
 */
@FunctionalInterface
public interface OnInstanceLoadObserver {

	/**
	 * This method is called when information about an OnInstanceLoad which was previously requested using an
	 * asynchronous interface becomes available.
	 *
	 * @param <I>
	 *            the generic type
	 * @param processed
	 *            the processed
	 * @param batchLoad
	 *            if the instance is loaded from batch load or single load
	 * @return the i
	 */
	<I extends Instance> I call(I processed, boolean batchLoad);
}
