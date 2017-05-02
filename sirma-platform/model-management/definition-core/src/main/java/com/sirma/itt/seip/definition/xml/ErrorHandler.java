/**
 * Copyright (c) 2010 30.03.2010 , Sirma ITT.
 */
package com.sirma.itt.seip.definition.xml;

import static com.sirma.itt.seip.definition.xml.XmlErrorLevel.ERROR;
import static com.sirma.itt.seip.definition.xml.XmlErrorLevel.FATAL;
import static com.sirma.itt.seip.definition.xml.XmlErrorLevel.WARNING;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler class used for collecting errors when parsing xml.<br>
 * The error handler can be tuned to collect the errors or to throw an exception on error. The default behavior is to
 * collect the errors, if you need the other case you need to call the method
 * {@link ErrorHandler#setValidating(boolean)} with <code>false</code>.<br>
 * The handler ignores the missing DTD declaration from the parsed XML document.
 *
 * @author B.Bonev
 */
public class ErrorHandler extends DefaultHandler {

	private List<XmlError> errorsList = new LinkedList<>();

	protected boolean validating = true;

	@Override
	public void error(SAXParseException e) throws SAXException {
		String string = e.getMessage();
		// ignores the missing DTD declaration from the parsed XML document.
		if (!("Document is invalid: no grammar found.".equals(string) || string.startsWith("Document root element")
				&& string.endsWith("must match DOCTYPE root \"null\"."))) {
			if (validating) {
				addError(e, ERROR);
			} else {
				throw e;
			}
		}
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		if (!validating) {
			throw e;
		}
		addError(e, FATAL);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		if (!validating) {
			throw e;
		}
		addError(e, WARNING);
	}

	/**
	 * Returns a list with all errors if any while parsing a xml file.
	 *
	 * @return the list with errors. If the list is empty then no errors occur.
	 */
	public List<XmlError> getErrorsList() {
		return errorsList;
	}

	/**
	 * Returns <code>true</code> if after parsing there are errors and <code>false</code> if no errors detected.
	 *
	 * @return <code>true</code> if there are detected errors.
	 */
	public boolean hasErrors() {
		return errorsList == null ? false : !errorsList.isEmpty();
	}

	/**
	 * Getter method for validating.
	 *
	 * @return the validating
	 */
	public boolean isValidating() {
		return validating;
	}

	/**
	 * Setter method for validating.
	 *
	 * @param validating
	 *            the validating to set
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Logs new error.
	 *
	 * @param exception
	 *            is the error to log
	 * @param level
	 *            is the error level.
	 */
	private void addError(SAXParseException exception, XmlErrorLevel level) {
		errorsList.add(
				new XmlError(exception.getMessage(), level, exception.getLineNumber(), exception.getColumnNumber()));
	}

}
