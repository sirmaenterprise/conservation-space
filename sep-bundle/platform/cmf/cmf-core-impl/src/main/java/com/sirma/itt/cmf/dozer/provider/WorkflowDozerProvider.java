package com.sirma.itt.cmf.dozer.provider;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.dozer.provider.DozerMapperMappingProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Provider class for workflow dozer mappings
 * 
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 3)
public class WorkflowDozerProvider implements DozerMapperMappingProvider {
	/** The Constant DOZER_WORKFLOW_ENTITY_MAPPING_XML. */
	public static final String DOZER_WORKFLOW_ENTITY_MAPPING_XML = "com/sirma/itt/cmf/dozer/dozerWorkflowEntityBeanMapping.xml";

	/** The Constant DOZER_WORKFLOW_DEFINITION_MAPPING_XML. */
	public static final String DOZER_WORKFLOW_DEFINITION_MAPPING_XML = "com/sirma/itt/cmf/dozer/dozerWorkflowDefinitionBeanMapping.xml";

	private static final List<String> MAPPINGS = Arrays.asList(DOZER_WORKFLOW_ENTITY_MAPPING_XML,
			DOZER_WORKFLOW_DEFINITION_MAPPING_XML);
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMappingUries() {
		return MAPPINGS;
	}

}
