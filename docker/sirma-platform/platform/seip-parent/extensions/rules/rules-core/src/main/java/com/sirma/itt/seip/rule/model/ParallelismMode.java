package com.sirma.itt.seip.rule.model;

import org.apache.commons.lang.StringUtils;

/**
 * Enum to define the parallel mode for processing.
 *
 * @author BBonev
 */
public enum ParallelismMode {

	/** No parallelism. */
	NONE, /** Parallelism on data provider level. */
	DATA_PROVIDER;

	/**
	 * Parses the given string to enum value
	 *
	 * @param value
	 *            the value
	 * @return the parallelism mode
	 */
	public static ParallelismMode parse(String value) {
		if (StringUtils.isEmpty(value)) {
			return NONE;
		}
		for (ParallelismMode mode : values()) {
			if (mode.toString().equalsIgnoreCase(value)) {
				return mode;
			}
		}
		return NONE;
	}
}
