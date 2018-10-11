package com.sirma.itt.objects.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.objects.xml.schema.ObjectsSchemaBuilder;
import com.sirma.itt.seip.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.descriptor.ResourceFileDescriptor;

/**
 * Adds definitions loading for project specific definitions.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DefinitionManagementServiceExtension.TARGET_NAME, order = 40)
public class ObjectsDefinitionManagementServiceExtension implements DefinitionManagementServiceExtension {

	private static final List<FileDescriptor> TYPES_XML = Collections
			.singletonList(new ResourceFileDescriptor("../types.xml", ObjectsSchemaBuilder.class));

	@SuppressWarnings("rawtypes")
	private static final List<Class> SUPPORTED_OBJECTS = new ArrayList<>(
			Arrays.asList(DataTypeDefinition.class, DataType.class));

	@Override
	@SuppressWarnings("rawtypes")
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		if (DataTypeDefinition.class.isAssignableFrom(definitionClass)) {
			return getTypeDefinitions();
		}
		return Collections.emptyList();
	}

	private static List<FileDescriptor> getTypeDefinitions() {
		return TYPES_XML;
	}
}
