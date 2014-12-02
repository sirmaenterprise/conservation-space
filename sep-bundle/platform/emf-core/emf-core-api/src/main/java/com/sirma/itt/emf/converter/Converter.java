package com.sirma.itt.emf.converter;

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
	public T convert(F source);
}