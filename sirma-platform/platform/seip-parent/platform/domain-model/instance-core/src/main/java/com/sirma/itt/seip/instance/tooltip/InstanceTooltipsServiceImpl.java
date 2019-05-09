package com.sirma.itt.seip.instance.tooltip;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.sep.content.rendition.RenditionService;

/**
 * Default implementation of the {@link InstanceTooltipsService}.
 *
 * @author nvelkov
 */
public class InstanceTooltipsServiceImpl implements InstanceTooltipsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String NOT_FOUND_TOOLTIP = "Not found tooltip header for instance[{}] with id[{}]!";

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private HeadersService headersService;
	
	@Inject
	private RenditionService renditionService;

	@Override
	public String getTooltip(String instanceId) {
		Instance instance = domainInstanceService.loadInstance(instanceId);
		// loads the thumbnail in the instance as DefaultProperties.THUMBNAIL_IMAGE
		instance.getOrCreateProperties()
				.computeIfAbsent(DefaultProperties.THUMBNAIL_IMAGE, k -> renditionService.getThumbnail(instanceId));
		String header = headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_TOOLTIP);
		if (header == null) {
			LOGGER.warn(NOT_FOUND_TOOLTIP, instance.getClass().getSimpleName(), instance.getId());
			return null;
		}
		return header;
	}
}