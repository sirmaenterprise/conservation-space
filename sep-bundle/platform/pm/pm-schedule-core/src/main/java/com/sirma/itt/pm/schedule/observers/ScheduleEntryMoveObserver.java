/*
 *
 */
package com.sirma.itt.pm.schedule.observers;

import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.instance.GenericMovedEvent;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.pm.schedule.event.ScheduleEntryMovedEvent;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;

/**
 * Observer handler that update the parent - child relations when moving schedule entries from one
 * parent to other.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ScheduleEntryMoveObserver {

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The task service. */
	@Inject
	private TaskService taskService;
	@Inject
	private EventService eventService;

	/**
	 * Listens for move event to update the parent child relations
	 *
	 * @param event
	 *            the event
	 */
	public void onEntryMoved(@Observes ScheduleEntryMovedEvent event) {
		ScheduleEntry scheduleEntry = event.getInstance();
		Instance newParent = null;
		Instance oldParent = null;

		if ((scheduleEntry == null) || (event.getNewParent() == null)
				|| (event.getOldParent() == null)) {
			return;
		}

		Instance actualInstance = scheduleEntry.getActualInstance();
		if ((actualInstance != null) && (event.getNewParent() instanceof ScheduleEntry)
				&& (event.getOldParent() instanceof ScheduleEntry)) {

			ScheduleEntry old = (ScheduleEntry) event.getOldParent();
			oldParent = old.getActualInstance();
			Instance oldContext = null;
			if (oldParent != null) {
				unlink(oldParent.toReference(), actualInstance.toReference());

				oldContext = InstanceUtil.getParentContext(actualInstance, true);
			}

			ScheduleEntry newP = (ScheduleEntry) event.getNewParent();
			newParent = newP.getActualInstance();

			if (newParent != null) {
				// update the direct parent reference
				if (actualInstance instanceof OwnedModel) {
					((OwnedModel) actualInstance).setOwningInstance(newParent);
					((OwnedModel) actualInstance).setOwningReference(newParent.toReference());
				}

				link(newParent.toReference(), actualInstance.toReference(), true);

				Instance newContext = InstanceUtil.getParentContext(actualInstance, true);

				if ((oldContext != null) && (newContext != null)) {
					if (!EqualsHelper.nullSafeEquals(oldContext.getId(), newContext.getId())) {
						if (oldParent != null) {
							if (!EqualsHelper.nullSafeEquals(oldContext.getId(), oldParent.getId())) {
								unlink(oldContext.toReference(), actualInstance.toReference());
							}
						}
						unlinkFromContext(oldContext, actualInstance);
						link(newContext.toReference(), actualInstance.toReference(), false);
						linkToContext(newContext, actualInstance);
					}
					// at least we update the instance that they have new parents
					contextChanged(actualInstance, oldContext, newContext);
				}
			}
		}
		eventService.fire(new GenericMovedEvent(scheduleEntry.getActualInstance(), oldParent,
				newParent));
	}

	/**
	 * Updates the instance that has a context changed.
	 *
	 * @param actualInstance
	 *            the actual instance
	 * @param oldContext
	 *            the old context
	 * @param newContext
	 *            the new context
	 */
	private void contextChanged(Instance actualInstance, Instance oldContext, Instance newContext) {
		if (actualInstance instanceof StandaloneTaskInstance) {
			((StandaloneTaskInstance) actualInstance).setParentContextId(((DMSInstance) newContext)
					.getDmsId());
			taskService.save((AbstractTaskInstance) actualInstance, new Operation(
					ActionTypeConstants.EDIT_DETAILS));
		}
	}

	/**
	 * Links the given instance to it's new context
	 *
	 * @param newContext
	 *            the new context
	 * @param actualInstance
	 *            the actual instance
	 */
	private void linkToContext(Instance newContext, Instance actualInstance) {
		if (actualInstance instanceof AbstractTaskInstance) {
			taskService.attachTaskToInstance(newContext,
					Arrays.asList((AbstractTaskInstance) actualInstance),
					InstanceUtil.getDirectParent(actualInstance, true), true);
		}
	}

	/**
	 * Updates given context that the given instance has been removed from it.
	 *
	 * @param oldContext
	 *            the old context
	 * @param actualInstance
	 *            the actual instance
	 */
	private void unlinkFromContext(Instance oldContext, Instance actualInstance) {
		if (actualInstance instanceof AbstractTaskInstance) {
			taskService.dettachTaskFromInstance(oldContext,
					Arrays.asList((AbstractTaskInstance) actualInstance));
		}
	}

	/**
	 * Links the given two instances via parent-child relation. If the argument
	 * {@code directParent==true} then simple semantic relation will be created also between the 2
	 * instances.
	 *
	 * @param parent
	 *            the parent
	 * @param child
	 *            the child
	 * @param directParent
	 *            the direct parent
	 */
	private void link(InstanceReference parent, InstanceReference child, boolean directParent) {
		linkService.link(parent, child, LinkConstants.PARENT_TO_CHILD,
				LinkConstants.CHILD_TO_PARENT, LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
		if (directParent) {
			linkService.linkSimple(child, parent, LinkConstants.PART_OF_URI);
		}
	}

	/**
	 * Unlinks the given parent and child
	 *
	 * @param parent
	 *            the parent
	 * @param child
	 *            the child
	 */
	private void unlink(InstanceReference parent, InstanceReference child) {
		linkService.unlink(parent, child, LinkConstants.PARENT_TO_CHILD,
				LinkConstants.CHILD_TO_PARENT);
		linkService.unlinkSimple(child, parent, LinkConstants.PART_OF_URI);
	}

}
