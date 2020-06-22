package com.sirma.sep.instance.template.schema;

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
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.TemplateDefinition;
import com.sirma.itt.seip.template.TemplateDefinitionImpl;
import com.sirma.sep.content.descriptor.ResourceFileDescriptor;

/**
 * Adds templates specific definitions support loading.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("rawtypes")
@Extension(target = DefinitionManagementServiceExtension.TARGET_NAME, order = 25)
public class TemplateDefinitionManagementService implements DefinitionManagementServiceExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final List<Class> SUPPORTED_OBJECTS = new ArrayList<>(
			Arrays.asList(TemplateDefinitionImpl.class, TemplateDefinition.class));

	private static final List<FileDescriptor> TYPES_XML = Collections
			.singletonList(new ResourceFileDescriptor("./types.xml", TemplateDefinitionManagementService.class));

	@Inject
	private DefintionAdapterService adapterService;

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		if (TemplateDefinition.class.isAssignableFrom(definitionClass)) {
			return getDefinitionInternal(TemplateDefinition.class);
		}

		return Collections.emptyList();
	}

	private List<FileDescriptor> getDefinitionInternal(Class<?> clazz) {
		try {
			return adapterService.getDefinitions(clazz);
		} catch (Exception e) {
			LOGGER.warn("Failed to retrieve {} definitions from DMS", clazz.getSimpleName(), e);
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