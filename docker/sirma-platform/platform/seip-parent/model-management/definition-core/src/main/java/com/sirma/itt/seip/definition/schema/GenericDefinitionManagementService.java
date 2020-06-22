package com.sirma.itt.seip.definition.schema;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.seip.definition.DefintionAdapterService;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.descriptor.ResourceFileDescriptor;

/**
 * Adds generic specific definitions support loading.
 *
 * @author BBonev
 */
@SuppressWarnings("rawtypes")
@Extension(target = DefinitionManagementServiceExtension.TARGET_NAME, order = 20)
public class GenericDefinitionManagementService implements DefinitionManagementServiceExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final List<Class> SUPPORTED_OBJECTS = new ArrayList<>(Arrays.asList(DataTypeDefinition.class,
			DataType.class, GenericDefinition.class, GenericDefinitionImpl.class));

	private static final List<FileDescriptor> TYPES_XML = Collections
			.singletonList(new ResourceFileDescriptor("./types.xml", GenericDefinitionSchemaBuilder.class));

	@Inject
	private DefintionAdapterService adapterService;

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		if (DataTypeDefinition.class.isAssignableFrom(definitionClass)) {
			return getTypeDefinitions();
		} else if (GenericDefinition.class.isAssignableFrom(definitionClass)) {
			return getDefinitionInternal(GenericDefinition.class);
		}
		return Collections.emptyList();
	}

	private List<FileDescriptor> getDefinitionInternal(Class<?> definitionClass) {
		try {
			return adapterService.getDefinitions(definitionClass);
		} catch (Exception e) {
			LOGGER.warn("Failed to retrieve {} definitions from DMS.", definitionClass.getSimpleName(), e);
		}
		return Collections.emptyList();
	}

	/**
	 * Gets the type definitions.
	 *
	 * @return the type definitions
	 */
	protected List<FileDescriptor> getTypeDefinitions() {
		return TYPES_XML;
	}
}