package com.sirma.itt.commons.utils.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sirma.itt.commons.utils.stream.UTF8BOMSkipInputStream;

/**
 * Validates xml against schema.
 * 
 * @author bbanchev
 */
public final class SchemaValidator {
	private final static String NL = System.getProperty("line.separator");

	/**
	 * default private constructor.
	 */
	private SchemaValidator() {

	}

	/** error appender */
	private static StringBuilder errorLogger = new StringBuilder();

	/**
	 * method checks xml and return number of errors;
	 * 
	 * @param pathToSchema
	 *            is the path to xsd file
	 * @param pathToXML
	 *            is the path to xml file to check
	 * @return number of the errors: 0 if correct, n if n errors occurred and -1
	 *         if exception occurred
	 * @throws Exception
	 *             is some occurs
	 */
	public static void validateXml(String pathToSchema, String pathToXML)
			throws Exception {
		errorLogger.delete(0, errorLogger.length());
		Schema schema = loadSchema(pathToSchema);
		if (schema == null) {
			throw new RuntimeException("XSD schema could not be loaded!");
		}
		Validator validator = schema.newValidator();
		validator.setErrorHandler(new SchemaErrorHandler());
		InputStream stream = new UTF8BOMSkipInputStream(new FileInputStream(
				pathToXML));
		SAXSource source = new SAXSource(new InputSource(stream));
		validator.validate(source);
		if (errorLogger.length() > 0) {
			throw new RuntimeException(errorLogger.toString());
		}
	}

	/**
	 * loads schema and returns it.
	 * 
	 * @param name
	 *            is the path to schema.
	 * @return the loaded schema
	 * @throws SAXException
	 *             is occurs
	 */
	private static Schema loadSchema(String name) throws SAXException {
		Schema schema = null;
		String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
		SchemaFactory factory = SchemaFactory.newInstance(language);
		schema = factory.newSchema(new File(name));
		return schema;
	}

	/**
	 * inner class for handler. Used for backtrace and logging of the errors.
	 * 
	 * @author bbanchev
	 */
	private static class SchemaErrorHandler implements ErrorHandler {
		/**
		 * {@inheritDoc}
		 */
		public void warning(SAXParseException e) {
			errorLogger.append("Warning: " + NL);
			printException(e);
		}

		/**
		 * {@inheritDoc}
		 */
		public void error(SAXParseException e) {
			errorLogger.append("Error: " + NL);
			printException(e);
		}

		/**
		 * {@inheritDoc}
		 */
		public void fatalError(SAXParseException e) {
			errorLogger.append("Fattal error: " + NL);
			printException(e);
		}

		/**
		 * print nicely the error and place of occurrence.
		 * 
		 * @param e
		 *            is the exception
		 */
		private void printException(SAXParseException e) {
			errorLogger.append(" Line number: " + e.getLineNumber() + NL);
			errorLogger.append(" Column number: " + e.getColumnNumber() + NL);
			errorLogger.append(" Message: " + e.getMessage() + NL);
			errorLogger.append(NL);
		}
	}

}
