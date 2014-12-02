package com.sirma.itt.pm.dozer;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.dozer.provider.DozerMapperMappingProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Extension class to add PM modules configuration for Dozer subsystem
 *
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 10)
public class PmDozerMappingProvider implements DozerMapperMappingProvider {

	/** The Constant DOZER_PROJECT_MAPPING_XML. */
	public static final String DOZER_PROJECT_DEFINITION_MAPPING_XML = "com/sirma/itt/pm/dozer/dozerProjectDefinitionMapping.xml";

	/** The Constant DOZER_PROJECT_ENTITY_MAPPING_XML. */
	public static final String DOZER_PROJECT_ENTITY_MAPPING_XML = "com/sirma/itt/pm/dozer/dozerProjectEntityMapping.xml";

	/** The Constant PM_MAPPINGS. */
	private static final List<String> PM_MAPPINGS = Arrays.asList(
			DOZER_PROJECT_DEFINITION_MAPPING_XML, DOZER_PROJECT_ENTITY_MAPPING_XML);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMappingUries() {
		return PM_MAPPINGS;
	}
}
