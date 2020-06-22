package com.sirma.itt.seip.instance.headers;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.event.InstanceChangeEvent;

/**
 * Observer that listens for changes in instances and updates their static header value in order to be persisted in the
 * database.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/11/2017
 */
@Singleton
class InstanceHeaderUpdateObserver {

	@Inject
	private InstanceHeaderService headerService;
	@Inject
	private InstancePropertyNameResolver fieldConverter;

	/**
	 * Generate the static instance header and assign it to the instance
	 *
	 * @param event the event that carry the modified instance
	 */
	void onInstanceChange(@Observes InstanceChangeEvent<?> event) {
		Instance instance = event.getInstance();
		String header = headerService.evaluateHeader(instance).orElse(null);
		// if null the header should be removed from the database
		instance.add(DefaultProperties.HEADER_LABEL, header, fieldConverter);
	}
}
