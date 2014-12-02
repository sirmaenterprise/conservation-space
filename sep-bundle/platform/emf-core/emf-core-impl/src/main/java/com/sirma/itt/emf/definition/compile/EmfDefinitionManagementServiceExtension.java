package com.sirma.itt.emf.definition.compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.io.descriptor.ResourceFileDescriptor;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.xml.schema.SchemaBuilder;

/**
 * Default extension point for EMF module
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DefinitionManagementServiceExtension.TARGET_NAME, order = 10)
public class EmfDefinitionManagementServiceExtension implements
		DefinitionManagementServiceExtension {

	private static final List<FileDescriptor> TYPES_XML = Collections
			.singletonList((FileDescriptor) new ResourceFileDescriptor("../types.xml",
					SchemaBuilder.class));

	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(Arrays.asList(
			DataTypeDefinition.class, DataType.class));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		if (DataTypeDefinition.class.isAssignableFrom(definitionClass)) {
			return TYPES_XML;
		}
		return Collections.emptyList();
	}

}
