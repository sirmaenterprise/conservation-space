package com.sirma.itt.cmf.alfresco4.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.TemplateDefinition;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterServiceExtension;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * CMF module's adapter that provides the means of loading the definitions for the module.
 *
 * @author BBonev
 */
@Extension(target = DMSDefintionAdapterServiceExtension.TARGET_NAME, order = 10)
public class CmfDefintionAdapterServiceExtension implements DMSDefintionAdapterServiceExtension {

	/** The Constant mapping. */
	private static final Map<Class<?>, String> MAPPING = CollectionUtils.createHashMap(5);
	static {
		MAPPING.put(CaseDefinition.class, ServiceURIRegistry.CMF_CASE_DEFINITIONS_SEARCH);
		MAPPING.put(DocumentDefinitionTemplate.class, ServiceURIRegistry.CMF_DOC_DEFINITIONS_SEARCH);
		MAPPING.put(WorkflowDefinition.class, ServiceURIRegistry.CMF_WORKFLOW_DEFINITIONS_SEARCH);
		MAPPING.put(TaskDefinitionTemplate.class, ServiceURIRegistry.CMF_TASK_DEFINITIONS_SEARCH);
		MAPPING.put(TaskDefinition.class, ServiceURIRegistry.CMF_TASK_DEFINITIONS_SEARCH);
		MAPPING.put(TemplateDefinition.class, ServiceURIRegistry.CMF_TEMPLATE_DEFINITIONS_SEARCH);
		MAPPING.put(GenericDefinition.class, ServiceURIRegistry.CMF_GENERIC_DEFINITIONS_SEARCH);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return new ArrayList<Class<?>>(MAPPING.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSearchPath(Class<?> definitionClass) {
		return MAPPING.get(definitionClass);
	}

}
