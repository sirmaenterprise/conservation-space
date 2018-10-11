/**
 *
 */
package com.sirma.itt.seip.definition.schema;

import com.sirma.sep.xml.SchemaInstance;
import com.sirma.sep.xml.XmlSchemaProvider;

/**
 * Default schema instances
 *
 * @author BBonev
 */
public class BaseSchemas {

	private BaseSchemas() {

	}

	public static final SchemaInstance COMMON = SchemaInstance.fromResource(BaseSchemas.class, "common.xsd");
	public static final SchemaInstance TYPES = SchemaInstance.fromResource(BaseSchemas.class, "types.xsd");
	public static final SchemaInstance GENERIC = SchemaInstance.fromResource(BaseSchemas.class, "generic.xsd");

	public static final XmlSchemaProvider TYPES_DEFINITION = XmlSchemaProvider.create("types", TYPES);
	public static final XmlSchemaProvider GENERIC_DEFINITION = XmlSchemaProvider.create("generic", GENERIC,
			BaseSchemas.COMMON);
}
