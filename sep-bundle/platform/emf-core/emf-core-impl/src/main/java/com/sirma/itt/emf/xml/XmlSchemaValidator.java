/**
 * Copyright (c) 2010 09.04.2010 , Sirma ITT.
 */
package com.sirma.itt.emf.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The class offers a methods for validating a xml files using different sources for xml and schema
 * locations. For validation is used the default {@link SAXParser} implementation.
 * 
 * @author B.Bonev
 */
public class XmlSchemaValidator {

	/**
	 * <p>
	 * W3C XML Schema Namespace URI.
	 * </p>
	 * <p>
	 * Defined to be "<code>http://www.w3.org/2001/XMLSchema</code>".
	 * 
	 * @see <a href= "http://www.w3.org/TR/xmlschema-1/#Instance_Document_Constructions"> XML Schema
	 *      Part 1: Structures, 2.6 Schema-Related Markup in Documents Being Validated</a>
	 */
	public static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

	/**
	 * Validates the given xml against the given schema using the default {@link ErrorHandler}
	 * implementation. The files are search on the current file system. The list of error can be
	 * retrieved from the error handler.
	 * 
	 * @param xmlPath
	 *            is the path to the xml from the file system.
	 * @param schemaPath
	 *            is the path to the schema file from the file system to use.
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 * @throws XmlValidatorError
	 */
	public static boolean validate(String xmlPath, String... schemaPath) throws XmlValidatorError {
		try {
			File xml = getAsFile(xmlPath, schemaPath);
			InputStream[] streams = getStreamsFromPaths(schemaPath);

			return validate(new FileInputStream(xml), new ErrorHandler(), streams);
		} catch (FileNotFoundException e) {
			throw new XmlValidatorError("Cannot read the input files.", e);
		}
	}

	/**
	 * Validates the given xml against the given schema using the given {@link ErrorHandler}. The
	 * files are search on the current file system. The list of error can be retrieved from the
	 * error handler.
	 * <p>
	 * The error handler can be tuned to collect the errors or to throw an exception on error. The
	 * default behavior is to collect the errors, if you need the other case you need to call the
	 * method {@link ErrorHandler#setValidating(boolean)} with <code>false</code>.
	 * <p>
	 * This method can be used and to add a custom logic to the error handler to perform a data
	 * extraction from the validated xml for optimizing the data processing with a single xml
	 * parsing.
	 * 
	 * @param xmlPath
	 *            is the path to the xml from the file system.
	 * @param handler
	 *            is the error handler to use.
	 * @param schemaPath
	 *            is the path to the schema file from the file system to use.
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 * @throws XmlValidatorError
	 */
	public static boolean validate(String xmlPath, ErrorHandler handler, String... schemaPath)
			throws XmlValidatorError {
		try {
			File xml = getAsFile(xmlPath, schemaPath);
			InputStream[] streams = getStreamsFromPaths(schemaPath);

			return validate(new FileInputStream(xml), handler, streams);
		} catch (FileNotFoundException e) {
			throw new XmlValidatorError("Cannot read the input files.", e);
		}
	}

	/**
	 * Validates the given xml against the given schema using the default {@link ErrorHandler}
	 * implementation.
	 * 
	 * @param xmlStream
	 *            is the xml source to use.
	 * @param schemaStream
	 *            is the schema stream to use.
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 * @throws XmlValidatorError
	 */
	public static boolean validate(InputStream xmlStream, InputStream... schemaStream)
			throws XmlValidatorError {
		return validate(xmlStream, new ErrorHandler(), schemaStream);
	}

	/**
	 * Validates the given xml against the given schema using the given {@link ErrorHandler}. The
	 * list of error can be retrieved from the error handler.
	 * <p>
	 * The error handler can be tuned to collect the errors or to throw an exception on error. The
	 * default behavior is to collect the errors, if you need the other case you need to call the
	 * method {@link ErrorHandler#setValidating(boolean)} with <code>false</code>.
	 * <p>
	 * This method can be used and to add a custom logic to the error handler to perform a data
	 * extraction from the validated xml for optimizing the data processing with a single xml
	 * parsing.
	 * 
	 * @param xmlStream
	 *            is the xml stream to use.
	 * @param handler
	 *            is the error handler to use.
	 * @param schemaStream
	 *            is the schema stream to use.
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 * @throws XmlValidatorError
	 */
	public static boolean validate(InputStream xmlStream, ErrorHandler handler,
			InputStream... schemaStream) throws XmlValidatorError {
		return validate(new InputSource(xmlStream), handler, schemaStream);
	}

	/**
	 * Validates the given xml against the given schema using the given {@link ErrorHandler}. The
	 * list of error can be retrieved from the error handler.
	 * <p>
	 * The error handler can be tuned to collect the errors or to throw an exception on error. The
	 * default behavior is to collect the errors, if you need the other case you need to call the
	 * method {@link ErrorHandler#setValidating(boolean)} with <code>false</code>.
	 * <p>
	 * This method can be used and to add a custom logic to the error handler to perform a data
	 * extraction from the validated xml for optimizing the data processing with a single xml
	 * parsing.
	 * 
	 * @param xmlSource
	 *            is the xml source to use.
	 * @param handler
	 *            is the error handler to use.
	 * @param schemaStream
	 *            is the schema stream to use.
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 * @throws XmlValidatorError
	 */
	public static boolean validate(InputSource xmlSource, ErrorHandler handler,
			InputStream... schemaStream) throws XmlValidatorError {
		Schema schema = getSchema(buildSchema(schemaStream));
		SAXParser saxParser = getParser(schema);

		// handler.setValidating(true);
		try {
			saxParser.parse(xmlSource, handler);
		} catch (SAXException | IOException e) {
			throw new XmlValidatorError(e);
		}
		return handler.hasErrors();
	}

	/**
	 * Validates the given xml against the given schema using the given {@link ErrorHandler}. The
	 * list of error can be retrieved from the error handler.
	 * <p>
	 * The error handler can be tuned to collect the errors or to throw an exception on error. The
	 * default behavior is to collect the errors, if you need the other case you need to call the
	 * method {@link ErrorHandler#setValidating(boolean)} with <code>false</code>.
	 * <p>
	 * This method can be used and to add a custom logic to the error handler to perform a data
	 * extraction from the validated xml for optimizing the data processing with a single xml
	 * parsing.
	 * 
	 * @param xmlSource
	 *            is the xml source to use.
	 * @param handler
	 *            is the error handler to use.
	 * @param schema
	 *            is the schema to use.
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 * @throws XmlValidatorError
	 */
	public static boolean validate(InputSource xmlSource, ErrorHandler handler, Schema schema)
			throws XmlValidatorError {
		if (schema == null) {
			throw new XmlValidatorError("Schema must be specified.");
		}
		SAXParser saxParser = getParser(schema);

		// handler.setValidating(true);
		try {
			saxParser.parse(xmlSource, handler);
		} catch (SAXException | IOException e) {
			throw new XmlValidatorError(e);
		}
		return handler.hasErrors();
	}

	/**
	 * Validates all given xml sources against the given schema using the given {@link ErrorHandler}
	 * . The list of error can be retrieved from the error handler.
	 * <p>
	 * The error handler can be tuned to collect the errors or to throw an exception on error. The
	 * default behavior is to collect the errors, if you need the other case you need to call the
	 * method {@link ErrorHandler#setValidating(boolean)} with <code>false</code>.
	 * <p>
	 * This method can be used and to add a custom logic to the error handler to perform a data
	 * extraction from the validated xml for optimizing the data processing with a single xml
	 * parsing.
	 * 
	 * @param xmls
	 *            is the list of xml sources to validate.
	 * @param handler
	 *            is the error handler to use.
	 * @param schemaStream
	 *            is the schema stream to use.
	 * @return <code>true</code> if the xml is valid and <code>false</code> if errors are found.
	 * @throws XmlValidatorError
	 */
	public static boolean batchValidate(List<InputSource> xmls, ErrorHandler handler,
			InputStream... schemaStream) throws XmlValidatorError {
		Schema schema = getSchema(buildSchema(schemaStream));
		SAXParser saxParser = getParser(schema);

		for (InputSource inputSource : xmls) {
			try {
				saxParser.parse(inputSource, handler);
			} catch (SAXException | IOException e) {
				throw new XmlValidatorError(e);
			}
		}
		return handler.hasErrors();
	}

	/**
	 * Parses the given xml file against the given schema and returns the list with found errors.
	 * 
	 * @param xmlPath
	 *            is the path to the xml from the file system.
	 * @param schemaPath
	 *            is the path to the schema file from the file system to use.
	 * @return the list with found errors or empty list if nothing found.
	 * @throws XmlValidatorError
	 */
	public static List<XmlError> resolveErrors(String xmlPath, String... schemaPath)
			throws XmlValidatorError {
		try {
			File xml = getAsFile(xmlPath, schemaPath);
			InputStream[] streams = getStreamsFromPaths(schemaPath);

			return resolveErrors(new FileInputStream(xml), streams);
		} catch (FileNotFoundException e) {
			throw new XmlValidatorError("Cannot read the input files.");
		}
	}

	/**
	 * Checks the xml validation preconditions and turns the xml path to a {@link File} instance.
	 * 
	 * @param xmlPath
	 *            path to get as file.
	 * @param schemaPath
	 *            schema paths to verify.
	 * @return File instance.
	 */
	private static File getAsFile(String xmlPath, String... schemaPath) {
		File xml = new File(xmlPath);
		checkFile(xml, "Cannot read the xml file: ");
		if (schemaPath == null) {
			throw new XmlValidatorError("At least one schema file must be specified.");
		}
		return xml;
	}

	/**
	 * Parses the given xml file against the given schema and returns the list with found errors.
	 * 
	 * @param xmlStream
	 *            is the xml input stream.
	 * @param schemaStream
	 *            is the schema to use.
	 * @return the list with found errors or empty list if nothing found.
	 * @throws XmlValidatorError
	 */
	public static List<XmlError> resolveErrors(InputStream xmlStream, InputStream... schemaStream)
			throws XmlValidatorError {
		return resolveErrors(new InputSource(xmlStream), schemaStream);
	}

	/**
	 * Parses the given xml file against the given schema and returns the list with found errors.
	 * 
	 * @param xmlSource
	 *            is the xml input source.
	 * @param schemaStream
	 *            is the schema to use.
	 * @return the list with found errors or empty list if nothing found.
	 * @throws XmlValidatorError
	 */
	public static List<XmlError> resolveErrors(InputSource xmlSource, InputStream... schemaStream)
			throws XmlValidatorError {
		ErrorHandler handler = new ErrorHandler();
		validate(xmlSource, handler, schemaStream);

		return handler.getErrorsList();
	}

	/**
	 * Creates a {@link Schema} instance object.
	 * 
	 * @param source
	 *            is the schema source.
	 * @return the created schema object.
	 * @throws XmlValidatorError
	 *             when any error occur while creating schema.
	 */
	private static Schema getSchema(Source... source) throws XmlValidatorError {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
		try {
			return schemaFactory.newSchema(source);
		} catch (SAXException e) {
			throw new XmlValidatorError("Invalid schema.", e);
		}
	}

	/**
	 * Builds a schema source list from the given input streams.
	 * 
	 * @param schemaStream
	 *            is the list of schema streams
	 * @return the list of sources
	 * @throws XmlValidatorError
	 */
	private static Source[] buildSchema(InputStream... schemaStream) throws XmlValidatorError {
		if (schemaStream == null) {
			throw new XmlValidatorError("At least one schema file must be specified.");
		}
		Source[] sources = new Source[schemaStream.length];
		for (int i = 0; i < sources.length; i++) {
			sources[i] = new StreamSource(schemaStream[i]);
		}
		return sources;
	}

	/**
	 * Creates and returns a validating {@link SAXParser}.
	 * 
	 * @param schema
	 *            is the validating schema.
	 * @return the created parser.
	 * @throws XmlValidatorError
	 *             when any error occur while creating parser.
	 */
	protected static SAXParser getParser(Schema schema) throws XmlValidatorError {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setSchema(schema);

		factory.setNamespaceAware(true);
		factory.setValidating(true);
		try {
			factory.setFeature("http://xml.org/sax/features/validation", true);
			return factory.newSAXParser();
		} catch (SAXException | ParserConfigurationException e) {
			throw new XmlValidatorError(e);
		}
	}

	/**
	 * Checks if the given file exists and is a file.
	 * 
	 * @param file
	 *            is the file to check
	 * @param msg
	 *            is the message to throw with an exception.
	 * @throws XmlValidatorError
	 *             when the file does not exists.
	 */
	private static void checkFile(File file, String msg) throws XmlValidatorError {
		if (!(file.exists() && file.isFile())) {
			throw new XmlValidatorError(msg + file.getPath());
		}
	}

	/**
	 * Creates FileInputStream instances from provided set of paths.
	 * 
	 * @param schemaPath
	 *            paths to the schemas.
	 * @return array with the streams.
	 * @throws FileNotFoundException
	 *             when any of the files cannot be found.
	 */
	private static InputStream[] getStreamsFromPaths(String... schemaPath)
			throws FileNotFoundException {
		InputStream[] streams = new InputStream[schemaPath.length];
		for (int i = 0; i < schemaPath.length; i++) {
			String string = schemaPath[i];
			File schema = new File(string);
			checkFile(schema, "Cannot read the xml schema file: ");
			streams[i] = new FileInputStream(schema);
		}
		return streams;
	}

}
