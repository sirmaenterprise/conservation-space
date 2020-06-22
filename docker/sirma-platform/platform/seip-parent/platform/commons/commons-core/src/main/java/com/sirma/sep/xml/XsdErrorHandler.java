package com.sirma.sep.xml;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Handles xsd validation errors from a validator and collects them into a list.
 *
 * @author Vilizar Tsonev
 */
class XsdErrorHandler implements ErrorHandler {

	private List<String> errorsList = new LinkedList<>();

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		// N/A - errors are strict enough
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		addError(exception);
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		addError(exception);
	}

	/**
	 * Gets the list of collected errors.
	 *
	 * @return the list of collected errors
	 */
	public List<String> getErrorsList() {
		return errorsList;
	}

	private void addError(SAXParseException exception) {
		errorsList.add(String.format("On line %d, column %d : %s", Integer.valueOf(exception.getLineNumber()),
				Integer.valueOf(exception.getColumnNumber()), exception.getMessage()));
	}

}
