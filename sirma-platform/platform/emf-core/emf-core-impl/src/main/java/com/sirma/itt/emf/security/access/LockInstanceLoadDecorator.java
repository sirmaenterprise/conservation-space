/**
 *
 */
package com.sirma.itt.emf.security.access;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_INFO;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.Lockable;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Instance load decorator that fetches the instance lock status.
 *
 * @author BBonev
 */
@Extension(target = InstanceLoadDecorator.TARGET_NAME, order = 15)
public class LockInstanceLoadDecorator implements InstanceLoadDecorator {

	@Inject
	private LockService lockService;

	@Override
	public <I extends Instance> void decorateInstance(I instance) {
		if (instance instanceof Lockable) {
			LockInfo lockStatus = lockService.lockStatus(instance.toReference());
			instance.addIfNotNull(DefaultProperties.LOCKED_BY, lockStatus.getLockedBy());
			instance.add(LOCKED_INFO, lockStatus);
		}
	}

	@Override
	public <I extends Instance> void decorateResult(Collection<I> collection) {
		List<InstanceReference> references = collection
				.stream()
					.filter(Lockable.class::isInstance)
					.map(Instance::toReference)
					.collect(Collectors.toCollection(LinkedList::new));

		if (references.isEmpty()) {
			return;
		}

		Map<InstanceReference, LockInfo> lockStatus = lockService.lockStatus(references);

		collection.stream().filter(Lockable.class::isInstance).forEach(inst -> {
			LockInfo lockInfo = lockStatus.get(inst.toReference());
			inst.addIfNotNull(DefaultProperties.LOCKED_BY, lockInfo.getLockedBy());
			inst.add(LOCKED_INFO, lockInfo);
		});
	}

}
