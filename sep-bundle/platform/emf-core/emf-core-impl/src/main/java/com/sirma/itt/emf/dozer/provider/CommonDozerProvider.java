package com.sirma.itt.emf.dozer.provider;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.plugin.Extension;

/**
 * Provider class for common dozer mappings
 *
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 1)
public class CommonDozerProvider implements DozerMapperMappingProvider {

	/** The Constant DOZER_COMMON_MAPPING_XML. */
	public static final String DOZER_COMMON_MAPPING_XML = "com/sirma/itt/emf/dozer/dozerCommonMappings.xml";
	public static final String DOZER_COMMON_DEFINITION_MAPPING_XML = "com/sirma/itt/emf/dozer/dozerCommonDefinitionMappings.xml";

	/** The mappings. */
	private static List<String> MAPPINGS = Arrays.asList(DOZER_COMMON_MAPPING_XML,
			DOZER_COMMON_DEFINITION_MAPPING_XML);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMappingUries() {
		return MAPPINGS;
	}

}
