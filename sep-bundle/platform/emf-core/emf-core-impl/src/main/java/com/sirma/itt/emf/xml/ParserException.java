/**
 * Copyright (c) 2010 30.03.2010 , Sirma ITT.
 */
package com.sirma.itt.emf.xml;

import java.util.List;

import com.sirma.itt.emf.exceptions.EmfRuntimeException;

/**
 * Exception thrown when parsing exception are detected.
 * 
 * @author B.Bonev
 */
public class ParserException extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4293430087107390753L;
	private List<XmlError> exceptionList;
	private String msg;

	/**
	 * @param exceptionList
	 *            the list with exception
	 */
	public ParserException(List<XmlError> exceptionList) {
		this.exceptionList = exceptionList;
		StringBuilder builder = new StringBuilder();
		for (XmlError xmlError : exceptionList) {
			builder.append("Found ").append(xmlError.getLevel()).append(
					" on line=").append(xmlError.getLineNumber()).append(
					" and column=").append(xmlError.getColNumber()).append(
					" with message=").append(xmlError.getMessage())
					.append("\n");
		}
		msg = builder.toString();
	}

	/**
	 * Setter method for exceptionList.
	 * 
	 * @param exceptionList
	 *            the exceptionList to set
	 */
	public void setExceptionList(List<XmlError> exceptionList) {
		this.exceptionList = exceptionList;
	}

	/**
	 * Getter method for exceptionList.
	 * 
	 * @return the exceptionList
	 */
	public List<XmlError> getExceptionList() {
		return exceptionList;
	}

	@Override
	public String getMessage() {
		return msg;
	}

}
