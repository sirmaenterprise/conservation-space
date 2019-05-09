package com.sirma.itt.seip.instance.dao;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;

import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Resolve instance type for instances that does not have an instance type present
 *
 * @author BBonev
 */
@Extension(target = InstanceLoadDecorator.INSTANCE_DECORATOR, order = 50)
public class InstanceTypeLoadDecorator implements InstanceLoadDecorator {

	@Inject
	private InstanceTypes instanceTypes;

	@Override
	public <I extends Instance> void decorateInstance(I instance) {
		if (instance.type() == null) {
			Optional<InstanceType> type = instanceTypes.from(instance);
			if (type.isPresent()) {
				instance.setType(type.get());
				instance.getOrCreateProperties().computeIfAbsent(SEMANTIC_TYPE, key -> type.get().getId());
			}
		}
	}

	@Override
	public <I extends Instance> void decorateResult(Collection<I> collection) {
		instanceTypes.resolveTypes(collection);
	}

	@Override
	public boolean allowParallelProcessing() {
		// this should be run after parallel loading is completed and the semantic type is resolved otherwise this may
		// cause multiple solr queries for resolving the type
		return false;
	}
}