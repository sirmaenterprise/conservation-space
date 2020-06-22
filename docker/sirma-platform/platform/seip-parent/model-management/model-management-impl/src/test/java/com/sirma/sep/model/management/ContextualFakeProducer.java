package com.sirma.sep.model.management;

import javax.enterprise.inject.Produces;

import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;

/**
 * Produces fakes of {@link Contextual} and related implementations.
 *
 * @author Mihail Radkov
 */
public class ContextualFakeProducer {

	@Produces
	public <T> Contextual<T> produceReference() {
		return ContextualReference.create();
	}

	@Produces
	public <K, V> ContextualConcurrentMap<K, V> produceConcurrentMap() {
		return ContextualConcurrentMap.create();
	}

}
