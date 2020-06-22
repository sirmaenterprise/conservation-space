package com.sirma.itt.emf.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.descriptor.ResourceFileDescriptor;

/**
 * Default extension point for EMF module
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DefinitionManagementServiceExtension.TARGET_NAME, order = 10)
public class EmfDefinitionManagementServiceExtension implements DefinitionManagementServiceExtension {

	private static final List<FileDescriptor> TYPES_XML = Collections.singletonList(
			(FileDescriptor) new ResourceFileDescriptor("types.xml", EmfDefinitionManagementServiceExtension.class));

	@SuppressWarnings("rawtypes")
	private static final List<Class> SUPPORTED_OBJECTS = Arrays.asList(DataTypeDefinition.class, DataType.class);

	@Override
	@SuppressWarnings("rawtypes")
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		if (DataTypeDefinition.class.isAssignableFrom(definitionClass)) {
			return TYPES_XML;
		}
		return Collections.emptyList();
	}

}
