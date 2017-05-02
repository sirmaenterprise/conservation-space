/**
 *
 */
package com.sirma.itt.seip.definition.schema;

import com.sirma.itt.seip.domain.xml.SchemaInstance;
import com.sirma.itt.seip.domain.xml.XmlSchemaProvider;

/**
 * Default schema instances
 *
 * @author BBonev
 */
public class BaseSchemas {

	/** The common schema definitions. */
	public static final SchemaInstance COMMON = SchemaInstance.fromResource(BaseSchemas.class, "common.xsd");
	public static final SchemaInstance TYPES = SchemaInstance.fromResource(BaseSchemas.class, "types.xsd");
	public static final SchemaInstance GENERIC = SchemaInstance.fromResource(BaseSchemas.class, "generic.xsd");

	/** The types definition. */
	public static final XmlSchemaProvider TYPES_DEFINITION = XmlSchemaProvider.create("types", TYPES);
	/** The generic definition. */
	public static final XmlSchemaProvider GENERIC_DEFINITION = XmlSchemaProvider.create("generic", GENERIC,
			BaseSchemas.COMMON);
}
