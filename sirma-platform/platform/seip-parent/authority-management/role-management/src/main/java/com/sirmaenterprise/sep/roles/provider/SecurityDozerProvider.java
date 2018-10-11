package com.sirmaenterprise.sep.roles.provider;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.mapping.dozer.DozerMapperMappingProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Dozer mapping provider for security classes.
 *
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 100)
public class SecurityDozerProvider implements DozerMapperMappingProvider {

	public static final String DOZER_SECURITY_MAPPING_XML = "com/sirmaenterprise/sep/roles/provider/dozer/dozerPermissionsEntityMapping.xml";
	private static List<String> MAPPINGS = Arrays.asList(DOZER_SECURITY_MAPPING_XML);

	@Override
	public List<String> getMappingUries() {
		return MAPPINGS;
	}

}
