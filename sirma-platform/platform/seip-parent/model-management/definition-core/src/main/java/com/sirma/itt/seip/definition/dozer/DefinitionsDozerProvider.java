package com.sirma.itt.seip.definition.dozer;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.mapping.dozer.DozerMapperMappingProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provider class for common definition dozer mappings
 *
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 7)
public class DefinitionsDozerProvider implements DozerMapperMappingProvider {

	public static final String DOZER_COMMON_MAPPING_XML = "com/sirma/itt/seip/definition/dozer/dozerCommonDefinitionMappings.xml";
	public static final String DOZER_GENERIC_DEFINITION_MAPPING_XML = "com/sirma/itt/seip/definition/dozer/dozerGenericDefinitionBeanMapping.xml";

	private static final List<String> MAPPINGS = Arrays.asList(DOZER_COMMON_MAPPING_XML,
			DOZER_GENERIC_DEFINITION_MAPPING_XML);

	@Override
	public List<String> getMappingUries() {
		return MAPPINGS;
	}

}
