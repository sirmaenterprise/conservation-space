/**
 *
 */
package com.sirma.itt.seip.definition.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.sirma.itt.seip.domain.xml.SchemaBuilder;
import com.sirma.itt.seip.domain.xml.XmlSchemaProvider;
import com.sirma.itt.seip.domain.xml.XmlValidatorError;

/**
 * Validates XMLs by given schema that is in the project. The schemes are located at com/sirma/itt/cmf/xml/schema. The
 * names of the schema are stored in SchemaBuilder
 *
 * @author BBonev
 */
public class XmlValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Disable instantiation.
	 */
	private XmlValidator() {
		// utility class
	}

	/**
	 * Validates the given String representation of the XML according to the schema
	 *
	 * @param xml
	 *            The String representation of the XML
	 * @param schemaName
	 *            The name of the schema. The names of the schema are stored in SchemaBuilder
	 * @param handler
	 *            Error handler
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 */
	public static boolean validateXml(InputStream xml, String schemaName, ErrorHandler handler) {
		Schema schema = SchemaBuilder.getCachedSchema(schemaName);
		SAXParser parser = XmlSchemaValidator.getParser(schema);
		try {
			parser.parse(xml, handler);
			if (handler.getErrorsList().isEmpty()) {
				return true;
			}
		} catch (SAXException | IOException e) {
			throw new XmlValidatorError(e);
		}
		return false;
	}

	/**
	 * Validates the given String representation of the XML according to the schema
	 *
	 * @param xml
	 *            The String representation of the XML
	 * @param schemaName
	 *            The name of the schema. The names of the schema are stored in SchemaBuilder
	 * @param handler
	 *            Error handler
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 */
	public static boolean validateXml(String xml, String schemaName, ErrorHandler handler) {
		Schema schema = SchemaBuilder.getCachedSchema(schemaName);
		SAXParser parser = XmlSchemaValidator.getParser(schema);
		try {
			parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), handler);
			if (handler.getErrorsList().isEmpty()) {
				return true;
			}
		} catch (SAXException | IOException e) {
			throw new XmlValidatorError(e);
		}
		return false;
	}

	/**
	 * Validates the given String representation of the XML according to the schema for the given IE message type.
	 *
	 * @param xml
	 *            The String representation of the XML
	 * @param ieMessageType
	 *            is the IE type to parse the given xml against.
	 * @param handler
	 *            Error handler
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 */
	public static boolean validateXml(InputStream xml, XmlSchemaProvider ieMessageType, ErrorHandler handler) {
		Schema schema = SchemaBuilder.getSchema(ieMessageType);
		SAXParser parser = XmlSchemaValidator.getParser(schema);
		try {
			parser.parse(xml, handler);
		} catch (SAXException e) {
			// swallow the exception and add its message to the handler errors list
			handler.getErrorsList().add(new XmlError(e.getMessage(), XmlErrorLevel.FATAL));
			LOGGER.trace("XSD error: ", e);
		} catch (IOException e) {
			throw new XmlValidatorError(e);
		}
		return handler.getErrorsList().isEmpty();
	}

	/**
	 * Validates the given String representation of the XML according to the schema for the given IE message type.
	 *
	 * @param xml
	 *            The String representation of the XML
	 * @param ieMessageType
	 *            is the IE type to parse the given xml against.
	 * @param handler
	 *            Error handler
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 */
	public static boolean validateXml(String xml, XmlSchemaProvider ieMessageType, ErrorHandler handler) {
		Schema schema = SchemaBuilder.getSchema(ieMessageType);
		SAXParser parser = XmlSchemaValidator.getParser(schema);
		try (InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
			parser.parse(inputStream, handler);
		} catch (SAXException e) {
			LOGGER.trace("XSD error: ", e);
			// swallow the exception and add its message to the handler errors list
			handler.getErrorsList().add(new XmlError(e.getMessage(), XmlErrorLevel.FATAL));
		} catch (IOException e) {
			throw new XmlValidatorError(e);
		}
		return handler.getErrorsList().isEmpty();
	}

	/**
	 * Parses the given xml string against the given schema and returns the list with found errors.
	 *
	 * @param xml
	 *            is loaded xml that need to be validated
	 * @param schemaName
	 *            is the path to the schema file from the file system to use.
	 * @return the list with found errors or empty list if nothing found.
	 */
	public static List<XmlError> resolveErrors(String xml, String schemaName) {
		ErrorHandler errorHandler = new ErrorHandler();
		validateXml(xml, schemaName, errorHandler);

		return errorHandler.getErrorsList();
	}

	/**
	 * Parses the given xml and validates it against the schema provided by the given {@link XmlSchemaProvider} and
	 * returns a list with found errors.
	 *
	 * @param xml
	 *            is the path to the xml from the file system.
	 * @param schemaProvider
	 *            provides information that will be used to validate the given XML against
	 * @return the list with found errors or empty list if nothing found.
	 */
	public static List<XmlError> resolveErrors(String xml, XmlSchemaProvider schemaProvider) {
		ErrorHandler errorHandler = new ErrorHandler();
		validateXml(xml, schemaProvider, errorHandler);

		return errorHandler.getErrorsList();
	}

	/**
	 * Parses the given xml and validates it against the schema provided by the given {@link XmlSchemaProvider} and
	 * returns a list with found errors.
	 *
	 * @param xmlStream
	 *            stream to the xml to validate
	 * @param schemaProvider
	 *            provides information that will be used to validate the given XML against
	 * @return the list with found errors or empty list if nothing found.
	 */
	public static List<XmlError> resolveErrors(InputStream xmlStream, XmlSchemaProvider schemaProvider) {
		ErrorHandler errorHandler = new ErrorHandler();
		validateXml(xmlStream, schemaProvider, errorHandler);

		return errorHandler.getErrorsList();
	}

}
