package com.sirma.itt.objects.dozer;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.mapping.dozer.DozerMapperMappingProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension class to add Objects module configuration for Dozer subsystem.
 *
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 30)
public class ObjectsDozerMappingProvider implements DozerMapperMappingProvider {

	public static final String DOZER_OBJECTS_ENTITY_MAPPING_XML = "com/sirma/itt/objects/dozer/dozerObjectsEntityMapping.xml";
	public static final String DOZER_ARCHIVED_OBJECTS_MAPPING_XML = "com/sirma/itt/objects/dozer/dozerArchivedInstanceMappings.xml";

	private static final List<String> OBJECTS_MAPPINGS = Arrays.asList(DOZER_OBJECTS_ENTITY_MAPPING_XML,
			DOZER_ARCHIVED_OBJECTS_MAPPING_XML);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMappingUries() {
		return OBJECTS_MAPPINGS;
	}
}
