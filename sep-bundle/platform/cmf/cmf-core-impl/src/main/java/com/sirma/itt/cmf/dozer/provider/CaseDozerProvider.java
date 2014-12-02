package com.sirma.itt.cmf.dozer.provider;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.dozer.provider.DozerMapperMappingProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Provider for case dozer mappings
 * 
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 2)
public class CaseDozerProvider implements DozerMapperMappingProvider {
	/** The Constant DOZER_CASE_DEFINITION_MAPPING_XML. */
	public static final String DOZER_CASE_DEFINITION_MAPPING_XML = "com/sirma/itt/cmf/dozer/dozerCasedefinitionBeanMapping.xml";

	/** The Constant DOZER_CASE_ENTITY_MAPPING_XML. */
	public static final String DOZER_CASE_ENTITY_MAPPING_XML = "com/sirma/itt/cmf/dozer/dozerCaseEntityBeanMapping.xml";

	/** The Constant MAPPINGS. */
	private static final List<String> MAPPINGS = Arrays.asList(DOZER_CASE_DEFINITION_MAPPING_XML,
			DOZER_CASE_ENTITY_MAPPING_XML);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMappingUries() {
		return MAPPINGS;
	}

}
