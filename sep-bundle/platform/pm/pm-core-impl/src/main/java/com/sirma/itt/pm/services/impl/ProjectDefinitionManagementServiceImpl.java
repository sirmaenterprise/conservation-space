package com.sirma.itt.pm.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.DMSDefintionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.io.descriptor.ResourceFileDescriptor;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.definitions.impl.ProjectDefinitionImpl;
import com.sirma.itt.pm.xml.schema.PmSchemaBuilder;

/**
 * Adds definitions loading for project specific definitions.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DefinitionManagementServiceExtension.TARGET_NAME, order = 30)
public class ProjectDefinitionManagementServiceImpl implements DefinitionManagementServiceExtension {

	List<FileDescriptor> TYPES_XML = Collections
			.<FileDescriptor> singletonList(new ResourceFileDescriptor("../types.xml",
					PmSchemaBuilder.class));
	/** The adapter service. */
	@Inject
	private DMSDefintionAdapterService adapterService;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ProjectDefinitionManagementServiceImpl.class);

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(Arrays.asList(
			DataTypeDefinition.class, DataType.class, ProjectDefinition.class,
			ProjectDefinitionImpl.class));

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
		if (ProjectDefinition.class.isAssignableFrom(definitionClass)) {
			return getProjectDefinitions();
		} else if (DataTypeDefinition.class.isAssignableFrom(definitionClass)) {
			return getTypeDefinitions();
		}
		return Collections.emptyList();
	}

	/**
	 * Gets the type definitions.
	 * 
	 * @return the type definitions
	 */
	public List<FileDescriptor> getTypeDefinitions() {
		return TYPES_XML;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<FileDescriptor> getProjectDefinitions() {
		try {
			return adapterService.getDefinitions(ProjectDefinition.class);
		} catch (DMSException e) {
			LOGGER.warn("Failed to retrieve project definitions from DMS", e);
		}
		return Collections.emptyList();
	}

}
