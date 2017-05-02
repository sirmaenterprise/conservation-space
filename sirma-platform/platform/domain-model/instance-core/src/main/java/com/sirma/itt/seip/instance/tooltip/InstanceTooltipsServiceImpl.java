package com.sirma.itt.seip.instance.tooltip;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.HeadersService;

/**
 * Default implementation of the {@link InstanceTooltipsService}.
 *
 * @author nvelkov
 */
public class InstanceTooltipsServiceImpl implements InstanceTooltipsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private HeadersService headersService;

	@Override
	public String getTooltip(String instanceId) {
		Instance instance = domainInstanceService.loadInstance(instanceId);

		// generate the header for the instance
		String header = headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_TOOLTIP);
		// if no tooltip header is found
		if (header == null) {
			LOGGER.warn("Not found tooltip header for instance[{}] with id[{}]!", instance.getClass().getSimpleName(),
					instance.getId());
			return null;
		}

		return header;
	}

}
