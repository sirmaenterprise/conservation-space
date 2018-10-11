/**
 *
 */
package com.sirma.sep.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
	 * Parses the given xml and validates it against the schema provided by the given {@link XmlSchemaProvider} and
	 * returns a list with found errors.
	 *
	 * @param xmlStream
	 *            stream to the xml to validate
	 * @param schemaProvider
	 *            provides information that will be used to validate the given XML against
	 * @return the list with found errors or empty list if nothing found.
	 * @throws IOException
	 */
	public static List<String> resolveErrors(InputStream xmlStream, XmlSchemaProvider schemaProvider) {
		Schema schema = SchemaBuilder.getSchema(schemaProvider);
		Validator validator = schema.newValidator();
		Source xmlFile = new StreamSource(xmlStream);

		XsdErrorHandler errorHandler = new XsdErrorHandler();
		validator.setErrorHandler(errorHandler);

		try {
			validator.validate(xmlFile);
		} catch (IOException e) {
			throw new XmlValidatorError(e);
		} catch (SAXException e) {
			LOGGER.trace("Error while validating xml file", e);
		}

		return errorHandler.getErrorsList();
	}
}
