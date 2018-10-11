package com.sirma.itt.seip.instance.version;

import java.util.Collection;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.plugin.Extension;

/**
 * This implementation of {@link InstanceLoadDecorator} is used to set initial version on instances that don't have one.
 * It is done this way, because the initial version is configurable.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceLoadDecorator.TARGET_NAME, order = 60)
public class VersionInstanceLoadDecorator implements InstanceLoadDecorator {

	@Inject
	private InstanceVersionService instanceVersionService;

	@Override
	public <I extends Instance> void decorateInstance(I instance) {
		instanceVersionService.populateVersion(instance);
	}

	@Override
	public <I extends Instance> void decorateResult(Collection<I> collection) {
		collection.forEach(instanceVersionService::populateVersion);
	}

}
