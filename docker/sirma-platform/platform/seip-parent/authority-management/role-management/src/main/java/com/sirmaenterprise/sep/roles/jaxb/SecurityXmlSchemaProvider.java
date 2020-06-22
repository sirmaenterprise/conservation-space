package com.sirmaenterprise.sep.roles.jaxb;

import com.sirma.sep.xml.SchemaInstance;
import com.sirma.sep.xml.XmlSchemaProvider;

/**
 * Xml schema provider for security schemas
 *
 * @author BBonev
 */
public class SecurityXmlSchemaProvider {

	private static final SchemaInstance ROLE_MAPPING = SchemaInstance.fromResource(SecurityXmlSchemaProvider.class,
			"roleMapping.xsd");

	/** The roles schema information for loading security mappings */
	public static final XmlSchemaProvider ROLES = XmlSchemaProvider.create("roles", ROLE_MAPPING);

	private SecurityXmlSchemaProvider() {
		// util class
	}
}
