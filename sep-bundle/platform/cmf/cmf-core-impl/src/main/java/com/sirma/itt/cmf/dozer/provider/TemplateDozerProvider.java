package com.sirma.itt.cmf.dozer.provider;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.dozer.provider.DozerMapperMappingProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Provider for template dozer mappings
 * 
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 5)
public class TemplateDozerProvider implements DozerMapperMappingProvider {

	/** The Constant DOZER_TEMPLATE_MAPPING_XML. */
	public static final String DOZER_TEMPLATE_MAPPING_XML = "com/sirma/itt/cmf/dozer/dozerTemplateBeanMapping.xml";

	/** The Constant MAPPINGS. */
	private static final List<String> MAPPINGS = Arrays.asList(DOZER_TEMPLATE_MAPPING_XML);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getMappingUries() {
		return MAPPINGS;
	}

}
