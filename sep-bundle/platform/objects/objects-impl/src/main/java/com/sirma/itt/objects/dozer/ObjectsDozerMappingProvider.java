package com.sirma.itt.objects.dozer;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.dozer.provider.DozerMapperMappingProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Extension class to add Objects module configuration for Dozer subsystem.
 * 
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 30)
public class ObjectsDozerMappingProvider implements DozerMapperMappingProvider {

	/** The Constant DOZER_OBJECTS_DEFINITION_MAPPING_XML. */
	public static final String DOZER_OBJECTS_DEFINITION_MAPPING_XML = "com/sirma/itt/objects/dozer/dozerObjectsDefinitionMapping.xml";

	/** The Constant DOZER_OBJECTS_ENTITY_MAPPING_XML. */
	public static final String DOZER_OBJECTS_ENTITY_MAPPING_XML = "com/sirma/itt/objects/dozer/dozerObjectsEntityMapping.xml";

	/** The Constant OBJECTS_MAPPINGS. */
	private static final List<String> OBJECTS_MAPPINGS = Arrays.asList(
			DOZER_OBJECTS_DEFINITION_MAPPING_XML, DOZER_OBJECTS_ENTITY_MAPPING_XML);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMappingUries() {
		return OBJECTS_MAPPINGS;
	}
}
