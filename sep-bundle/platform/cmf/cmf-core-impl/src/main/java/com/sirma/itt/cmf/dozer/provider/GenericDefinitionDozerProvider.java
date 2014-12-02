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
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 6)
public class GenericDefinitionDozerProvider implements DozerMapperMappingProvider {
	/** The Constant DOZER_CASE_DEFINITION_MAPPING_XML. */
	public static final String DOZER_GENERIC_DEFINITION_MAPPING_XML = "com/sirma/itt/cmf/dozer/dozerGenericDefinitionBeanMapping.xml";

	/** The Constant MAPPINGS. */
	private static final List<String> MAPPINGS = Arrays
			.asList(DOZER_GENERIC_DEFINITION_MAPPING_XML);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMappingUries() {
		return MAPPINGS;
	}

}
