package com.sirma.itt.seip.instance.save;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.event.ParentChangedEvent;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Step that persist the current bind context <strong>after</strong> persisting the instance itself. If there is an
 * error during context persisting the error will be rethtrown. To disable automatic saving
 * {@link Options#DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS} could be set.
 *
 * @author bbanchev
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 30)
public class AssignContextStep implements InstanceSaveStep {

	@Inject
	private InstanceContextService instanceContextService;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private EventService eventService;

	@Inject
	private InstancePropertyNameResolver fieldConverter;

	@Override
	public String getName() {
		return "assignContext";
	}

	@Override
	public void afterSave(InstanceSaveContext saveContext) {
		// maybe should be configuration in saveContext
		if (Options.DISABLE_AUTOMATIC_CONTEXT_CHILD_LINKS.isEnabled()) {
			return;
		}
		Instance instance = saveContext.getInstance();
		if (instanceContextService.isContextChanged(instance)) {
			Serializable newParentId = instance.get(InstanceContextService.HAS_PARENT, fieldConverter);
			instanceContextService.bindContext(instance, newParentId);
			eventService.fire(new ParentChangedEvent(instance, getOldParent(instance), getNewParent(newParentId)));
		}
	}

	private Instance getOldParent(Instance instance) {
		return instanceContextService.getContext(instance.getId()).map(InstanceReference::toInstance).orElse(null);
	}

	private Instance getNewParent(Serializable newParentId) {
		return newParentId != null ? domainInstanceService.loadInstance((String) newParentId) : null;
	}
}
