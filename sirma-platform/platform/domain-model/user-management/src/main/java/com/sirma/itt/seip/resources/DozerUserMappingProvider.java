/**
 *
 */
package com.sirma.itt.seip.resources;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.mapping.dozer.DozerMapperMappingProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Mapping provider for user and group mappings
 *
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 50)
public class DozerUserMappingProvider implements DozerMapperMappingProvider {
	public static final String DOZER_USER_MAPPING_XML = "com/sirma/itt/seip/resources/dozer/dozerUserMappings.xml";

	@Override
	public List<String> getMappingUries() {
		return Collections.singletonList(DOZER_USER_MAPPING_XML);
	}

}
