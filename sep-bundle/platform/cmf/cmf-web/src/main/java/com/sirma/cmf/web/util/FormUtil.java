package com.sirma.cmf.web.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * The Class FormUtil.
 */
@Named
@ApplicationScoped
public class FormUtil {

	/**
	 * To array.
	 * 
	 * @param arg
	 *            the arg
	 * @return the string[]
	 */
	public String[] toArray(String arg) {

		return new String[] { arg };
	}

	/**
	 * To array.
	 * 
	 * @param arg1
	 *            the arg1
	 * @param arg2
	 *            the arg2
	 * @return the string[]
	 */
	public String[] toArray(String arg1, String arg2) {

		return new String[] { arg1, arg2 };
	}

	/**
	 * To array.
	 * 
	 * @param arg1
	 *            the arg1
	 * @param arg2
	 *            the arg2
	 * @param arg3
	 *            the arg3
	 * @return the string[]
	 */
	public String[] toArray(String arg1, String arg2, String arg3) {

		return new String[] { arg1, arg2, arg3 };
	}

}
