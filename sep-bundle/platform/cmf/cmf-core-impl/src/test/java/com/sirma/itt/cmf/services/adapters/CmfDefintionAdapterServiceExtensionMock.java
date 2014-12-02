package com.sirma.itt.cmf.services.adapters;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * Mock for DMSDefintionAdapterServiceExtension that returs internal uri to definitions location
 *
 * @author bbanchev
 */
@Extension(target = DMSDefintionAdapterServiceExtension.TARGET_NAME, order = 10, priority = 1)
public class CmfDefintionAdapterServiceExtensionMock implements DMSDefintionAdapterServiceExtension {

	/** The Constant mapping. */
	private static final Map<Class<?>, String> MAPPING = CollectionUtils.createHashMap(5);
	static {
		MAPPING.put(CaseDefinition.class, "");
		MAPPING.put(DocumentDefinitionTemplate.class, "");
		MAPPING.put(WorkflowDefinition.class, "");
		MAPPING.put(TaskDefinitionTemplate.class, "");
		MAPPING.put(TaskDefinition.class, "");
		MAPPING.put(TemplateDefinition.class, "");
		MAPPING.put(GenericDefinition.class, "");
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
		URL resource = CmfDefintionAdapterServiceExtensionMock.class.getResource("dummy");
		return resource.toString();
	}

}
