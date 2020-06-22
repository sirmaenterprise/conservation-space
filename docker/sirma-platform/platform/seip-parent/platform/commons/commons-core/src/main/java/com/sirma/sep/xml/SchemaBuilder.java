package com.sirma.sep.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

/**
 * Builds XML schemas and keeps a static cache of them for reuse.
 *
 * @author BBonev
 */
public class SchemaBuilder {

	/** The cached compiled schemas. */
	private static final Map<String, Schema> CACHEDS_SCHEMAS = new LinkedHashMap<>();

	/**
	 * <p>
	 * W3C XML Schema Namespace URI.
	 * </p>
	 * <p>
	 * Defined to be "<code>http://www.w3.org/2001/XMLSchema</code>".
	 *
	 * @see <a href= "http://www.w3.org/TR/xmlschema-1/#Instance_Document_Constructions"> XML Schema Part 1: Structures,
	 *      2.6 Schema-Related Markup in Documents Being Validated</a>
	 */
	public static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

	/**
	 * Instantiates a new schema builder.
	 */
	private SchemaBuilder() {
		// utility class
	}

	/**
	 * Gets a cached schema already build via the method {@link #getSchema(SchemaInstance, SchemaInstance...)}
	 *
	 * @param name
	 *            the name
	 * @return the cached schema
	 */
	public static Schema getCachedSchema(String name) {
		return CACHEDS_SCHEMAS.get(name);
	}

	/**
	 * Creates a schema for validation. The schemas are loaded using the given base loader class. First the method will
	 * check the internal cache for already build schema instance and if not found will try to build new one from the
	 * provided arguments. If the schema has been build already then the loader class is not required otherwise the
	 * method will throw an {@link XmlValidatorError}.
	 *
	 * @param mainSchemaPath
	 *            Path to the main schema. It's used for internal cache key also
	 * @param additional
	 *            the additional schema files needed to build the schema. All provided files will be merged into single
	 *            file internally
	 * @return Schema representation of the arguments
	 * @throws XmlValidatorError
	 *             If an error occurs. When baseLoaderClass is <code>null</code> when trying to build new schema, if any
	 *             of the given paths does not point to a valid file location relative to baseLoaderClass or if the
	 *             schema is not valid in structure
	 */
	public static Schema getSchema(SchemaInstance mainSchemaPath, SchemaInstance... additional) {
		if (mainSchemaPath.getName() == null) {
			return buildSchema(mainSchemaPath, additional);
		}
		return CACHEDS_SCHEMAS.computeIfAbsent(mainSchemaPath.getName(),
				key -> buildSchema(mainSchemaPath, additional));
	}

	private static Schema buildSchema(SchemaInstance mainSchemaPath, SchemaInstance[] additional) {
		Source[] schemas = loadSchemas(mainSchemaPath, additional);

		SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
		try {
			return schemaFactory.newSchema(schemas);
		} catch (SAXException e) {
			throw new XmlValidatorError("Invalid schema.", e);
		}
	}

	private static Source[] loadSchemas(SchemaInstance mainSchemaPath, SchemaInstance... additional) {
		Source[] schemas = new Source[1];
		if (additional != null && additional.length > 0) {
			String[] files = new String[additional.length + 1];
			try {
				// load all files in memory
				files[0] = IOUtils.toString(mainSchemaPath.getSchema(), StandardCharsets.UTF_8.toString());
				for (int i = 0; i < additional.length; i++) {
					InputStream uri = additional[i].getSchema();
					if (uri != null) {
						try (InputStream inputStream = uri) {
							files[i + 1] = IOUtils.toString(inputStream, StandardCharsets.UTF_8.toString());
						}
					}
				}
				// merge files to a single XSD file
				String mergedXSD = XmlTools.mergeXSD(files);
				schemas[0] = new StreamSource(new StringReader(mergedXSD));
			} catch (IOException e) {
				throw new XmlValidatorError("Failed to load schemas.", e);
			}
		} else {
			schemas[0] = new StreamSource(mainSchemaPath.getSchema());
		}
		return schemas;
	}

	/**
	 * Creates a schema for from the given provider.
	 *
	 * @param provider
	 *            the XSD provider
	 * @return the created schema
	 */
	public static Schema getSchema(XmlSchemaProvider provider) {
		if (provider == null) {
			throw new IllegalArgumentException("The type cannot be NULL");
		}

		SchemaInstance mainSchemaPath = getBaseSchemaName(provider);

		return getSchema(mainSchemaPath, provider.getAdditionalSchemaLocations());
	}

	/**
	 * Returns the base schema from the provider.
	 *
	 * @param provider
	 *            the XSD provider
	 * @return is the schema location
	 */
	public static SchemaInstance getBaseSchemaName(XmlSchemaProvider provider) {
		if (provider == null || provider.getIdentifier() == null || provider.getMainSchemaFileLocation() == null) {
			throw new XmlValidatorError("Invalid schema provider: " + provider);
		}
		return provider.getMainSchemaFileLocation();
	}
}
