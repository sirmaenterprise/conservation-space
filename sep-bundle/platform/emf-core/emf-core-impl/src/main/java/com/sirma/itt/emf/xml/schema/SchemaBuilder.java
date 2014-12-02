package com.sirma.itt.emf.xml.schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.xml.XmlSchemaProvider;
import com.sirma.itt.emf.xml.XmlTools;
import com.sirma.itt.emf.xml.XmlValidatorError;

/**
 * Builds XML schemas and keeps a static cache of them for reuse.
 * 
 * @author BBonev
 */
public class SchemaBuilder {

	private static final Logger LOGGER = Logger.getLogger(SchemaBuilder.class);

	/** The cached compiled schemas. */
	private final static Map<String, Schema> cachedSchemas = new LinkedHashMap<>();

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
	 * Creates a schema for validation. The schemas are loaded using the given base loader class.
	 * First the method will check the internal cache for already build schema instance and if not
	 * found will try to build new one from the provided arguments. If the schema has been build
	 * already then the loader class is not required otherwise the method will throw an
	 * {@link XmlValidatorError}.
	 * 
	 * @param baseLoaderClass
	 *            the class used to fetch the resources that are relative to him. Not required IF
	 *            schema has been build already
	 * @param mainSchemaPath
	 *            Path to the main schema. It's used for internal cache key also
	 * @param additional
	 *            the additional schema files needed to build the schema. All provided files will be
	 *            merged into single file internally
	 * @return Schema representation of the arguments
	 * @throws XmlValidatorError
	 *             If an error occurs. When baseLoaderClass is <code>null</code> when trying to
	 *             build new schema, if any of the given paths does not point to a valid file
	 *             location relative to baseLoaderClass or if the schema is not valid in structure
	 */
	public static Schema getSchema(Class<?> baseLoaderClass, String mainSchemaPath,
			String... additional) throws XmlValidatorError {
		if (cachedSchemas.containsKey(mainSchemaPath)) {
			return cachedSchemas.get(mainSchemaPath);
		}
		if (baseLoaderClass == null) {
			throw new XmlValidatorError("Schema '" + mainSchemaPath
					+ "' not found and missing loader class to build new one!");
		}
		Source[] schemas = new Source[1];
		if ((additional != null) && (additional.length > 0)) {
			String[] files = new String[additional.length + 1];
			try {
				// load all files in memory
				files[0] = IOUtils.toString(baseLoaderClass.getResourceAsStream(mainSchemaPath),
						"UTF-8");
				for (int i = 0; i < additional.length; i++) {
					String uri = additional[i];
					if (StringUtils.isNotNullOrEmpty(uri)) {
						InputStream inputStream = baseLoaderClass.getResourceAsStream(uri);
						files[i + 1] = IOUtils.toString(inputStream, "UTF-8");
					}
				}
				// merge files to a single XSD file
				String mergedXSD = XmlTools.mergeXSD(files);
				schemas[0] = new StreamSource(new StringReader(mergedXSD));
			} catch (IOException e) {
				throw new XmlValidatorError("Failed to load schemas.", e);
			}
		} else {
			schemas[0] = new StreamSource(baseLoaderClass.getResourceAsStream(mainSchemaPath));
		}

		SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = schemaFactory.newSchema(schemas);
			cachedSchemas.put(mainSchemaPath, schema);
			return schema;
		} catch (SAXException e) {
			throw new XmlValidatorError("Invalid schema.", e);
		}
	}

	/**
	 * Creates a schema for from the given provider.
	 * 
	 * @param provider
	 *            the XSD provider
	 * @return the created schema
	 * @throws XmlValidatorError
	 */
	public static Schema getSchema(XmlSchemaProvider provider) throws XmlValidatorError {
		if (provider == null) {
			throw new IllegalArgumentException("The type cannot be NULL");
		}
		LOGGER.debug("Loading schema for XML with type: " + provider.getIdentifier());
		String mainSchemaPath = getBaseSchemaName(provider);

		return getSchema(provider.baseLoaderClass(), mainSchemaPath,
				provider.getAdditionalSchemaLocations());
	}

	/**
	 * Returns the base schema from the provider.
	 * 
	 * @param provider
	 *            the XSD provider
	 * @return is the schema location
	 */
	public static String getBaseSchemaName(XmlSchemaProvider provider) {
		if ((provider == null) || (provider.getIdentifier() == null)
				|| (provider.getMainSchemaFileLocation() == null)) {
			throw new XmlValidatorError("Invalid schema provider: " + provider);
		}
		String mainSchemaPath = provider.getMainSchemaFileLocation();
		return mainSchemaPath;
	}
}
