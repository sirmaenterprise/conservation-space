package com.sirma.itt.seip.instance.dozer;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.mapping.dozer.DozerMapperMappingProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provider class for common dozer mappings
 *
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 1)
public class CommonDozerProvider implements DozerMapperMappingProvider {

	public static final String DOZER_COMMON_MAPPING_XML = "com/sirma/itt/seip/instance/dozer/dozerCommonMappings.xml";
	public static final String DOZER_DELETED_INSTANCE_MAPPING_XML = "com/sirma/itt/seip/instance/dozer/dozerDeletedEntityMapping.xml";

	private static List<String> MAPPINGS = Arrays.asList(DOZER_COMMON_MAPPING_XML, DOZER_DELETED_INSTANCE_MAPPING_XML);

	@Override
	public List<String> getMappingUries() {
		return MAPPINGS;
	}

}
