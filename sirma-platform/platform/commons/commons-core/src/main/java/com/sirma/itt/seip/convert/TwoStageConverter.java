package com.sirma.itt.seip.convert;

/**
 * Support for chaining conversions.
 *
 * @param <F>
 *            From Type
 * @param <T>
 *            To Type
 * @author andyh
 */
public class TwoStageConverter<F, T> implements Converter<F, T> {

	@SuppressWarnings("rawtypes")
	private Converter first;

	@SuppressWarnings("rawtypes")
	private Converter second;

	/**
	 * Instantiates a new two stage converter.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 */
	@SuppressWarnings("rawtypes")
	public TwoStageConverter(Converter first, Converter second) {
		this.first = first;
		this.second = second;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T convert(F source) {
		return (T) second.convert(first.convert(source));
	}
}