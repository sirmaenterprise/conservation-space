package com.sirma.itt.seip.annotations.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.descriptor.ResourceFileDescriptor;

/**
 * Definition provider to for the annotations model and type definition.
 *
 * @author BBonev
 */
@Extension(target = DefinitionManagementServiceExtension.TARGET_NAME, order = 343)
public class AnnotationDefinitionManagementExtension implements DefinitionManagementServiceExtension {

	private static final List<FileDescriptor> TYPES_XML = Collections
			.singletonList(new ResourceFileDescriptor("./types.xml", AnnotationDefinitionManagementExtension.class));

	@Override
	public List<Class> getSupportedObjects() {
		return Arrays.asList(DataTypeDefinition.class, DataType.class);
	}

	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		if (DataTypeDefinition.class.isAssignableFrom(definitionClass)) {
			return TYPES_XML;
		}
		return Collections.emptyList();
	}

}
