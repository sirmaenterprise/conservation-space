package com.sirma.itt.seip.eai.cs.service.communication;

import com.sirma.itt.seip.exception.EmfException;

/**
 * The {@link ResponseParsingFunction} function allows to throw exception on {@link #applyThrows(Object)}
 *
 * @author bbanchev
 * @param <T>
 *            the input type.
 * @param <R>
 *            the result type.
 */
@FunctionalInterface
public interface ResponseParser<T, R> {

	/**
	 * Parse an input and produces a result. See {@link #apply(Object)}
	 *
	 * @param input
	 *            the input data
	 * @return the result of function call
	 * @throws EmfException
	 *             on error in function
	 */
	R parse(T input) throws EmfException;
}