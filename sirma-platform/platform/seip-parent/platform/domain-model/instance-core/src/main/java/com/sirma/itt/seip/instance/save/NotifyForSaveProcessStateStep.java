package com.sirma.itt.seip.instance.save;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.save.event.BeforeInstanceSaveEvent;
import com.sirma.itt.seip.instance.save.event.BeforeInstanceSaveRollbackEvent;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Contains logic that fires specific events during the save process. To this events could be added additional logic by
 * intercepting them and process the information that they carry. There are four events overall, fired by this step, one
 * for each phase that is executed by the steps. <br>
 * This step is executed and should be executed last. This way could be guaranteed that every before or after process is
 * invoked, before the specific events are fired.<br>
 * The step also fires events for rollback, so that they could be used to revert any changes done in different
 * transaction then the one used for the save process.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = Double.MAX_VALUE)
public class NotifyForSaveProcessStateStep implements InstanceSaveStep {

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private EventService eventService;

	/**
	 * Creates before instance save event, populates context with it, so that it could be used for the next phase and
	 * then fires it.
	 */
	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		BeforeInstanceSaveEvent beforeInstanceSaveEvent = createBeforeSaveEvent(saveContext);
		saveContext.setSaveEvent(beforeInstanceSaveEvent);
		eventService.fire(beforeInstanceSaveEvent);
	}

	private BeforeInstanceSaveEvent createBeforeSaveEvent(InstanceSaveContext saveContext) {
		return new BeforeInstanceSaveEvent(saveContext.getInstance(), loadCurrentInstance(saveContext.getInstanceId()),
				saveContext.getOperation());
	}

	/**
	 * Loads the current instance. It could be used to find the changes that will be applied after successful save
	 * process.
	 */
	private Instance loadCurrentInstance(String instanceId) {
		return instanceTypeResolver.resolveReference(instanceId).map(InstanceReference::toInstance).orElse(null);
	}

	/**
	 * Fires the next phase of before instance save event.
	 */
	@Override
	public void afterSave(InstanceSaveContext saveContext) {
		eventService.fireNextPhase(saveContext.getSaveEvent());
	}

	/**
	 * Creates before instance save rollback event, replaces before instance save event in the context, so that it could
	 * be used when firing the next phase of the rollback and fires it.
	 */
	@Override
	public void rollbackBeforeSave(InstanceSaveContext saveContext) {
		BeforeInstanceSaveRollbackEvent event = createBeforeSaveRollbackEvent(saveContext);
		saveContext.setSaveEvent(event);
		eventService.fire(event);
	}

	/**
	 * Reusing the current instance stored in the before instance save event to create the rollback event.
	 */
	private static BeforeInstanceSaveRollbackEvent createBeforeSaveRollbackEvent(InstanceSaveContext saveContext) {
		return new BeforeInstanceSaveRollbackEvent(saveContext.getInstance(),
				saveContext.getSaveEvent().getCurrentInstance(), saveContext.getOperation());
	}

	/**
	 * Fires the next phase of before instance save rollback event.
	 */
	@Override
	public void rollbackAfterSave(InstanceSaveContext saveContext) {
		eventService.fireNextPhase(saveContext.getSaveEvent());
	}

	@Override
	public String getName() {
		return "notifyForSaveProcessState";
	}

}
