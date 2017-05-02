package com.sirma.itt.seip.template.schedule;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Template activation scheduler used after reloading of existing dms templates to activate them.
 *
 * @author hlungov
 */
@Singleton
@Named(TemplateActivateScheduler.BEAN_ID)
public class TemplateActivateScheduler extends SchedulerActionAdapter {

	public static final String BEAN_ID = "TemplateActivateScheduler";

	protected static final String CORRESPONDING_INSTANCE_IDS = "correspondingInstanceIds";

	@Inject
	private TemplateService templateService;

	@Inject
	private TransactionSupport transactionSupport;

	@Override
	public void execute(SchedulerContext context) throws Exception {
		List<String> correspondingInstanceIds = context.getIfSameType(CORRESPONDING_INSTANCE_IDS, List.class);
		// added separate transaction because when activate instances the check for previous is not correct
		correspondingInstanceIds.stream().forEach(id -> {
			transactionSupport.invokeInNewTx(() -> templateService.activate(id));
		});
	}

	/**
	 * Creates the executor context.
	 *
	 * @param correspondingInstanceIds
	 * 		the template corresponding instance ids which to add them to new {@link SchedulerContext}
	 * @return the {@link SchedulerContext} filled with all the mandatory data for schedule
	 */
	public static SchedulerContext createExecutorContext(List<String> correspondingInstanceIds) {
		if (correspondingInstanceIds == null || correspondingInstanceIds.isEmpty()) {
			throw new EmfRuntimeException(
					"Can not create ScheduleContext, because of missing " + CORRESPONDING_INSTANCE_IDS);
		}
		SchedulerContext context = new SchedulerContext();
		context.put(CORRESPONDING_INSTANCE_IDS, (Serializable) correspondingInstanceIds);
		return context;
	}
}
