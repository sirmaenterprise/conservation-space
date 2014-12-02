/**
 *
 */
package com.sirma.itt.emf.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.validation.Schema;

import org.xml.sax.SAXException;

import com.sirma.itt.emf.xml.XmlSchemaProvider;
import com.sirma.itt.emf.xml.schema.SchemaBuilder;

/**
 * Validates XMLs by given schema that is in the project. The schemes are located at
 * com/sirma/itt/cmf/xml/schema. The names of the schema are stored in SchemaBuilder
 * 
 * @author BBonev
 */
public class CmfXmlValidator extends XmlSchemaValidator {

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
	 * @throws XmlValidatorError
	 */
	public static boolean validateXml(InputStream xml, String schemaName, ErrorHandler handler)
			throws XmlValidatorError {
		Schema schema = SchemaBuilder.getSchema(null, schemaName);
		SAXParser parser = getParser(schema);
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
	 * @throws XmlValidatorError
	 */
	public static boolean validateXml(String xml, String schemaName, ErrorHandler handler)
			throws XmlValidatorError {
		Schema schema = SchemaBuilder.getSchema(null, schemaName);
		SAXParser parser = getParser(schema);
		try {
			parser.parse(new ByteArrayInputStream(xml.getBytes()), handler);
			if (handler.getErrorsList().isEmpty()) {
				return true;
			}
		} catch (SAXException | IOException e) {
			throw new XmlValidatorError(e);
		}
		return false;
	}

	/**
	 * Validates the given String representation of the XML according to the schema for the given IE
	 * message type.
	 * 
	 * @param xml
	 *            The String representation of the XML
	 * @param ieMessageType
	 *            is the IE type to parse the given xml against.
	 * @param handler
	 *            Error handler
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 * @throws XmlValidatorError
	 */
	public static boolean validateXml(InputStream xml, XmlSchemaProvider ieMessageType,
			ErrorHandler handler) throws XmlValidatorError {
		Schema schema = SchemaBuilder.getSchema(ieMessageType);
		SAXParser parser = getParser(schema);
		try {
			parser.parse(xml, handler);
		} catch (SAXException e) {
			// swallow the exception and add its message to the handler errors list
			handler.getErrorsList().add(new XmlError(e.getMessage(), XmlErrorLevel.FATAL));
		} catch (IOException e) {
			throw new XmlValidatorError(e);
		}
		return handler.getErrorsList().isEmpty();
	}

	/**
	 * Validates the given String representation of the XML according to the schema for the given IE
	 * message type.
	 * 
	 * @param xml
	 *            The String representation of the XML
	 * @param ieMessageType
	 *            is the IE type to parse the given xml against.
	 * @param handler
	 *            Error handler
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 * @throws XmlValidatorError
	 */
	public static boolean validateXml(String xml, XmlSchemaProvider ieMessageType,
			ErrorHandler handler) throws XmlValidatorError {
		Schema schema = SchemaBuilder.getSchema(ieMessageType);
		SAXParser parser = getParser(schema);
		try {
			parser.parse(new ByteArrayInputStream(xml.getBytes()), handler);
		} catch (SAXException e) {
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
	 *            is the path to the xml from the file system.
	 * @param schemaName
	 *            is the path to the schema file from the file system to use.
	 * @return the list with found errors or empty list if nothing found.
	 * @throws XmlValidatorError
	 */
	public static List<XmlError> resolveErrors(String xml, String schemaName)
			throws XmlValidatorError {
		ErrorHandler errorHandler = new ErrorHandler();
		validateXml(xml, schemaName, errorHandler);

		return errorHandler.getErrorsList();
	}

	/**
	 * Parses the given xml string against the given IE message type and returns the list with found
	 * errors.
	 * 
	 * @param xml
	 *            is the path to the xml from the file system.
	 * @param ieMessageType
	 *            is the IE type to parse the given xml against.
	 * @return the list with found errors or empty list if nothing found.
	 * @throws XmlValidatorError
	 */
	public static List<XmlError> resolveErrors(String xml, XmlSchemaProvider ieMessageType)
			throws XmlValidatorError {
		ErrorHandler errorHandler = new ErrorHandler();
		validateXml(xml, ieMessageType, errorHandler);

		return errorHandler.getErrorsList();
	}

	/**
	 * Parses the given xml string against the given IE message type and returns the list with found
	 * errors.
	 * 
	 * @param xml
	 *            is the path to the xml from the file system.
	 * @param ieMessageType
	 *            is the IE type to parse the given xml against.
	 * @return the list with found errors or empty list if nothing found.
	 * @throws XmlValidatorError
	 */
	public static List<XmlError> resolveErrors(InputStream xml, XmlSchemaProvider ieMessageType)
			throws XmlValidatorError {
		ErrorHandler errorHandler = new ErrorHandler();
		validateXml(xml, ieMessageType, errorHandler);

		return errorHandler.getErrorsList();
	}

}
