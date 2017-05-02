package com.sirma.itt.seip.instance.version.revert;

import javax.inject.Inject;

import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.version.CreateInstanceVersionStep;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Handles the saving of the reverted instance. This step should be executed after the system properties are reverted
 * and the additional content handling in order to ensure correct results after the revert operation.
 *
 * @author A. Kunchev
 */
@Extension(target = RevertStep.EXTENSION_NAME, enabled = true, order = 50)
public class SaveRevertedInstanceStep implements RevertStep {

	private static final String SAVE_CONTEXT_KEY = "$saveContext$";

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private InstanceVersionService instanceVersionService;

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public String getName() {
		return "saveRevertedInstance";
	}

	@Override
	public void invoke(RevertContext context) {
		InstanceSaveContext saveContext = InstanceSaveContext.create(context.getRevertResultInstance(),
				context.getOperation());
		domainInstanceService.save(saveContext);
		context.put(SAVE_CONTEXT_KEY, saveContext);
	}

	@Override
	public void rollback(RevertContext context) {
		// TODO need to expose save rollback mechanism
		InstanceSaveContext saveContext = context.getIfSameType(SAVE_CONTEXT_KEY, InstanceSaveContext.class);
		if (saveContext == null) {
			return;
		}

		// delete the created version and the generated view, the other things are rollbacked, when the transaction is
		instanceVersionService
				.deleteVersion(saveContext.getIfSameType(CreateInstanceVersionStep.VERSION_INSTANCE_ID, String.class));
		saveContext.getViewId().ifPresent(id -> instanceContentService.deleteContent(id, null));
	}

}
