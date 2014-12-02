package com.sirma.itt.emf.dozer.provider;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.plugin.Extension;

/**
 * Provider class for forum dozer mappings.
 * 
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 4)
public class ForumDozerProvider implements DozerMapperMappingProvider {

	/** The Constant DOZER_FORUM_ENTITY_MAPPING_XML. */
	public static final String DOZER_FORUM_ENTITY_MAPPING_XML = "com/sirma/itt/emf/dozer/dozerForumEntityMapping.xml";

	/** The Constant MAPPINGS. */
	private static final List<String> MAPPINGS = Arrays.asList(DOZER_FORUM_ENTITY_MAPPING_XML);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMappingUries() {
		return MAPPINGS;
	}

}
