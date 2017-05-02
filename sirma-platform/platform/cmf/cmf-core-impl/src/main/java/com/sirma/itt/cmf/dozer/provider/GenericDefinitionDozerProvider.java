package com.sirma.itt.cmf.dozer.provider;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.mapping.dozer.DozerMapperMappingProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provider for case dozer mappings
 *
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 6)
public class GenericDefinitionDozerProvider implements DozerMapperMappingProvider {
	public static final String DOZER_GENERIC_DEFINITION_MAPPING_XML = "com/sirma/itt/cmf/dozer/dozerGenericDefinitionBeanMapping.xml";
	private static final List<String> MAPPINGS = Arrays.asList(DOZER_GENERIC_DEFINITION_MAPPING_XML);

	@Override
	public List<String> getMappingUries() {
		return MAPPINGS;
	}

}
