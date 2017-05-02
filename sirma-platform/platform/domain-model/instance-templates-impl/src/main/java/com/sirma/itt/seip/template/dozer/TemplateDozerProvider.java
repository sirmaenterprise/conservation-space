package com.sirma.itt.seip.template.dozer;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.mapping.dozer.DozerMapperMappingProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provider for template dozer mappings
 *
 * @author BBonev
 */
@Extension(target = DozerMapperMappingProvider.TARGET_NAME, order = 5)
public class TemplateDozerProvider implements DozerMapperMappingProvider {

	public static final String DOZER_TEMPLATE_MAPPING_XML = "com/sirma/itt/seip/template/dozer/dozerTemplateBeanMapping.xml";
	private static final List<String> MAPPINGS = Arrays.asList(DOZER_TEMPLATE_MAPPING_XML);

	@Override
	public List<String> getMappingUries() {
		return MAPPINGS;
	}

}
