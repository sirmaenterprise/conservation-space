package com.sirma.itt.cmf.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.TemplateDefinition;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.CaseDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionTemplateImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TemplateDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.WorkflowDefinitionImpl;
import com.sirma.itt.cmf.xml.schema.CmfSchemaBuilder;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.definition.DefinitionManagementServiceExtension;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.io.descriptor.ResourceFileDescriptor;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Adds CMF specific definitions support loading.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DefinitionManagementServiceExtension.TARGET_NAME, order = 20)
public class CmfDefinitionManagementServiceImpl implements DefinitionManagementServiceExtension {

	/** The adapter service. */
	@Inject
	private DMSDefintionAdapterService adapterService;

	private static final List<FileDescriptor> TYPES_XML = Collections
			.<FileDescriptor> singletonList(new ResourceFileDescriptor("../types.xml",
					CmfSchemaBuilder.class));

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CmfDefinitionManagementServiceImpl.class);

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(Arrays.asList(
			DataTypeDefinition.class, DataType.class, CaseDefinition.class,
			CaseDefinitionImpl.class, DocumentDefinitionTemplate.class,
			DocumentDefinitionImpl.class, TaskDefinitionTemplate.class,
			TaskDefinitionTemplateImpl.class, WorkflowDefinition.class,
			WorkflowDefinitionImpl.class, TaskDefinition.class, TaskDefinitionImpl.class,
			TemplateDefinitionImpl.class, TemplateDefinition.class, GenericDefinition.class,
			GenericDefinitionImpl.class));

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
			return getTypeDefinitions();
		} else if (CaseDefinition.class.isAssignableFrom(definitionClass)) {
			return getDefinitionInternal(CaseDefinition.class);
		} else if (DocumentDefinitionTemplate.class.isAssignableFrom(definitionClass)) {
			return getDefinitionInternal(DocumentDefinitionTemplate.class);
		} else if (TaskDefinitionTemplate.class.isAssignableFrom(definitionClass)
				|| TaskDefinition.class.isAssignableFrom(definitionClass)) {
			return getDefinitionInternal(TaskDefinitionTemplate.class);
		} else if (WorkflowDefinition.class.isAssignableFrom(definitionClass)) {
			return getDefinitionInternal(WorkflowDefinition.class);
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
		} catch (DMSException e) {
			LOGGER.warn("Failed to retrieve " + definitionClass.getSimpleName()
					+ " definitions from DMS", e);
		}
		return Collections.emptyList();
	}

	/**
	 * Gets the type definitions.
	 * 
	 * @return the type definitions
	 */
	private List<FileDescriptor> getTypeDefinitions() {
		return TYPES_XML;
	}

}
