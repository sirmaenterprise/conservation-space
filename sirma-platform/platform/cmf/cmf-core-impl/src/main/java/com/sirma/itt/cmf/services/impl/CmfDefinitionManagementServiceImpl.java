package com.sirma.itt.cmf.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.cmf.xml.schema.CmfSchemaBuilder;
import com.sirma.itt.seip.content.descriptor.ResourceFileDescriptor;
import com.sirma.itt.seip.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.seip.definition.DefintionAdapterService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.TemplateDefinition;
import com.sirma.itt.seip.template.TemplateDefinitionImpl;

/**
 * Adds CMF specific definitions support loading.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DefinitionManagementServiceExtension.TARGET_NAME, order = 20)
public class CmfDefinitionManagementServiceImpl implements DefinitionManagementServiceExtension {

	@Inject
	private DefintionAdapterService adapterService;

	private static final List<FileDescriptor> TYPES_XML = Collections
			.<FileDescriptor> singletonList(new ResourceFileDescriptor("../types.xml", CmfSchemaBuilder.class));

	private static final Logger LOGGER = LoggerFactory.getLogger(CmfDefinitionManagementServiceImpl.class);

	/** The Constant SUPPORTED_OBJECTS. */
	@SuppressWarnings("rawtypes")
	private static final List<Class> SUPPORTED_OBJECTS = new ArrayList<>(
			Arrays.asList(DataTypeDefinition.class, DataType.class, TemplateDefinitionImpl.class,
					TemplateDefinition.class, GenericDefinition.class, GenericDefinitionImpl.class));

	@Override
	@SuppressWarnings("rawtypes")
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass) {
		if (DataTypeDefinition.class.isAssignableFrom(definitionClass)) {
			return getTypeDefinitions();
		} else if (TemplateDefinition.class.isAssignableFrom(definitionClass)) {
			return getDefinitionInternal(TemplateDefinition.class);
		} else if (GenericDefinition.class.isAssignableFrom(definitionClass)) {
			return getDefinitionInternal(GenericDefinition.class);
		}
		return Collections.emptyList();
	}

	/**
	 * Gets the definition internal.
	 *
	 * @param definitionClass
	 *            the definition class
	 * @return the definition internal
	 */
	private List<FileDescriptor> getDefinitionInternal(Class<?> definitionClass) {
		try {
			return adapterService.getDefinitions(definitionClass);
		} catch (Exception e) {
			LOGGER.warn("Failed to retrieve " + definitionClass.getSimpleName() + " definitions from DMS", e);
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