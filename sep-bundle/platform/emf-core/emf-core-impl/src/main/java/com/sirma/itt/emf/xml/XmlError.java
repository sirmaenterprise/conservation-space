/**
 * Copyright (c) 2010 09.04.2010 , Sirma ITT.
 */
package com.sirma.itt.emf.xml;

import java.io.Serializable;

/**
 * Describes a single xml error.
 *
 * @author B.Bonev
 */
public class XmlError implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -5810946687051353937L;
	private String message;
	private XmlErrorLevel level;
	private int lineNumber;
	private int colNumber;

	/**
	 * Default constructor.
	 */
	public XmlError() {
		// nothing here
	}

	/**
	 * Initialize the error object with the given message and error level.
	 *
	 * @param message
	 *            is the error message.
	 * @param level
	 *            is the error level.
	 */
	public XmlError(String message, XmlErrorLevel level) {
		this.message = message;
		this.level = level;
	}

	/**
	 * Initialize the error object with the given message and error level.
	 *
	 * @param message
	 *            is the error message.
	 * @param level
	 *            is the error level.
	 * @param lineNumber
	 *            is the line number on which the error is detected.
	 * @param colNumber
	 *            is the column number on which the error is detected.
	 */
	public XmlError(String message, XmlErrorLevel level, int lineNumber,
			int colNumber) {
		this.message = message;
		this.level = level;
		this.lineNumber = lineNumber;
		this.colNumber = colNumber;
	}

	/**
	 * Getter method for message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Setter method for message.
	 *
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Getter method for level.
	 *
	 * @return the level
	 */
	public XmlErrorLevel getLevel() {
		return level;
	}

	/**
	 * Setter method for level.
	 *
	 * @param level
	 *            the level to set
	 */
	public void setLevel(XmlErrorLevel level) {
		this.level = level;
	}

	/**
	 * Getter method for lineNumber.
	 *
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Setter method for lineNumber.
	 *
	 * @param lineNumber
	 *            the lineNumber to set
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * Getter method for colNumber.
	 *
	 * @return the colNumber
	 */
	public int getColNumber() {
		return colNumber;
	}

	/**
	 * Setter method for colNumber.
	 *
	 * @param colNumber
	 *            the colNumber to set
	 */
	public void setColNumber(int colNumber) {
		this.colNumber = colNumber;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XmlError [colNumber=" + colNumber + ", level=" + level
				+ ", lineNumber=" + lineNumber + ", message=" + message + "]";
	}

	/**
	 * Provides a human-readable version of the error
	 *
	 * @return formatted error.
	 */
	public String getFormattedMessage() {
		StringBuilder formattedError = new StringBuilder();
		formattedError.append("On line '");
		formattedError.append(lineNumber);
		formattedError.append("' column '");
		formattedError.append(colNumber);
		formattedError.append("' found ");
		formattedError.append(message);

		return formattedError.toString();
	}

}
