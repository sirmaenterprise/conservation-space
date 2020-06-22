package com.sirma.itt.seip.annotations.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.model.DataType;

/**
 * Test for {@link AnnotationDefinitionManagementExtension}
 *
 * @author BBonev
 */
public class AnnotationDefinitionManagementExtensionTest {

	@Test
	public void getTypeDefinition() throws Exception {
		AnnotationDefinitionManagementExtension extension = new AnnotationDefinitionManagementExtension();
		List<FileDescriptor> definitions = extension.getDefinitions(DataTypeDefinition.class);
		assertNotNull(definitions);
		assertFalse(definitions.isEmpty());

		FileDescriptor descriptor = definitions.get(0);
		assertNotNull(descriptor);
		assertNotNull(descriptor.asString());

		definitions = extension.getDefinitions(GenericDefinition.class);
		assertNotNull(definitions);
		assertTrue(definitions.isEmpty());
	}

	@Test
	public void supportedTypes() throws Exception {
		AnnotationDefinitionManagementExtension extension = new AnnotationDefinitionManagementExtension();
		List<Class> list = extension.getSupportedObjects();
		assertNotNull(list);

		assertTrue(list.contains(DataTypeDefinition.class));
		assertTrue(list.contains(DataType.class));
	}
}
