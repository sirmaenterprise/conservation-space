package com.sirma.cmf.web;

import javax.enterprise.inject.Model;

/**
 * The Class ExceptionThrower.
 * 
 * @author svelikov
 */
@Model
public class ExceptionThrower {

	/**
	 * Throw not implemented.
	 * 
	 * @param message
	 *            the message
	 */
	public void throwNotImplemented(String message) {
		throw new RuntimeException(message);
	}

}
