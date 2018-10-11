package com.sirma.itt.seip.instance;

import java.util.Collection;

import com.sirma.itt.seip.Trackable;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Instance decorator that enables tracking changes for all loaded instances
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/05/2018
 */
@Extension(target = InstanceLoadDecorator.TARGET_NAME, order = InstanceLoadDecorator.MAX_ORDER)
public class EnableTrackingInstanceChanges implements InstanceLoadDecorator{

	@Override
	public <I extends Instance> void decorateInstance(I instance) {
		Trackable.enableTracking(instance);
	}

	@Override
	public <I extends Instance> void decorateResult(Collection<I> collection) {
		collection.forEach(Trackable::enableTracking);
	}
}
