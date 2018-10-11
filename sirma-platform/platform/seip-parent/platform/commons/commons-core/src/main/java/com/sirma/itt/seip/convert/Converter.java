package com.sirma.itt.seip.convert;

/**
 * Conversion interface.
 *
 * @param <F>
 *            From type
 * @param <T>
 *            To type
 * @author andyh
 */
public interface Converter<F, T> {

	/**
	 * Convert.
	 *
	 * @param source
	 *            the source
	 * @return the t
	 */
	T convert(F source);
}