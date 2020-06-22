package com.sirma.itt.seip.instance.template.observers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.save.event.AfterInstanceSaveEvent;
import com.sirma.itt.seip.instance.version.InstanceVersionService;

/**
 * Intercepts {@link AfterInstanceSaveEvent} and checks for changes in {@link LinkConstants#HAS_TEMPLATE}. If the
 * template of the saved instance is changed, the operation will be logged in the audit.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class InstanceTemplateChangedObserver {

	private static final String CHANGE_TEMPLATE = "changeTemplate";

	@Inject
	private InstanceVersionService instanceVersionService;

	@Inject
	private EventService eventService;

	@Inject
	private InstancePropertyNameResolver nameResolver;

	/**
	 * Checks, if there is a change in {@link LinkConstants#HAS_TEMPLATE} instance property, when specific instance is
	 * successfully saved. Intercepts {@link AfterInstanceSaveEvent}, which is fired at the end of the save process. The
	 * method will compare the old value, before the actual save and the value after the save. If the values are
	 * different, <code>changeTemplate</code> operation will be logged in the audit. The operation will be logged, if
	 * the instance is not new (just created, which is checked by it version number).
	 *
	 * @param event
	 *            carries the instances, which are used to compare the property. This event is fired at the end of the
	 *            save process, which should guarantees that there are no more changes to the saved instance
	 */
	public void onInstanceSave(@Observes AfterInstanceSaveEvent event) {
		Instance instanceToSave = event.getInstanceToSave();
		if (instanceVersionService.hasInitialVersion(instanceToSave)) {
			return;
		}

		Instance currentInstance = event.getCurrentInstance();
		if (currentInstance == null) {
			return;
		}

		String previousTemplate = currentInstance.getString(LinkConstants.HAS_TEMPLATE, "", nameResolver);
		String newTemplate = instanceToSave.getString(LinkConstants.HAS_TEMPLATE, "", nameResolver);
		if (!newTemplate.equals(previousTemplate)) {
			eventService.fire(new AuditableEvent(instanceToSave, CHANGE_TEMPLATE));
		}
	}

}
