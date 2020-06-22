/**
 *
 */
package com.sirma.itt.seip.resources.favorites;

import java.util.Collection;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Concrete implementation of the InstanceLoadDecorator. The implementation updates the favourite status in the instance
 * headers of the each passed instance. This extension is collected and called in the concrete implementation of
 * InstanceLoadDecorator - ChaningInstanceLoadDecorator.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceLoadDecorator.INSTANCE_DECORATOR, enabled = false, order = 20)
public class FavouritesServiceInstanceLoadDecorator implements InstanceLoadDecorator {

	@Inject
	private FavouritesService favouritesService;

	@Override
	public <I extends Instance> void decorateInstance(I instance) {
		favouritesService.updateFavoriteStateForInstance(instance);
	}

	@Override
	public <I extends Instance> void decorateResult(Collection<I> collection) {
		favouritesService.updateFavouriteStateForInstances(collection);
	}

}
