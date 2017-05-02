package com.sirma.itt.seip;

import com.sirma.itt.seip.context.Context;

/**
 * Provides a method that can perform an applicability test. Can be used for dynamic operation routing when combined
 * with supportable interfaces.
 *
 * @author BBonev
 */
public interface Applicable {
	/**
	 * Checks if is applicable. This method can use operations to filter, or some property of instance or type or etc.
	 *
	 * @param context
	 *            current execution context
	 * @return true, if is applicable
	 */
	boolean isApplicable(Context<String, Object> context);
}
