/**
 *
 */
package com.sirma.itt.seip.template.jaxb;

import com.sirma.itt.seip.definition.schema.BaseSchemas;
import com.sirma.itt.seip.domain.xml.SchemaInstance;
import com.sirma.itt.seip.domain.xml.XmlSchemaProvider;

/**
 * Schema provider for templates schema
 * 
 * @author BBonev
 */
public class TemplateSchemaProvider {

	private static final SchemaInstance TEMPLATE_SCHEMA = SchemaInstance.fromResource(TemplateSchemaProvider.class,
			"templates.xsd");

	public static final XmlSchemaProvider TEMPLATE_PROVIDER = XmlSchemaProvider.create("template", TEMPLATE_SCHEMA,
			BaseSchemas.COMMON);

}
