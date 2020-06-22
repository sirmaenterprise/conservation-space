package com.sirma.itt.seip.template.schedule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.template.TemplateService;

/**
 * Template activation scheduler used after reloading of existing dms templates to activate them.
 *
 * @author hlungov
 */
@Singleton
@Named(TemplateActivateScheduler.BEAN_ID)
public class TemplateActivateScheduler extends SchedulerActionAdapter {

	public static final String BEAN_ID = "TemplateActivateScheduler";

	protected static final String CORRESPONDING_INSTANCE_ID = "correspondingInstanceId";

	@Inject
	private TemplateService templateService;

	@Override
	public void execute(SchedulerContext context) throws Exception {
		String correspondingInstanceId = context.getIfSameType(CORRESPONDING_INSTANCE_ID, String.class);
		templateService.activate(correspondingInstanceId, false);
	}

	/**
	 * Creates the executor context.
	 *
	 * @param correspondingInstanceId
	 * 		the template corresponding instance ids which to add them to new {@link SchedulerContext}
	 * @return the {@link SchedulerContext} filled with all the mandatory data for schedule
	 */
	public static SchedulerContext createExecutorContext(String correspondingInstanceId) {
		if (StringUtils.isBlank(correspondingInstanceId)) {
			throw new EmfRuntimeException(
					"Can not create ScheduleContext, because of missing " + CORRESPONDING_INSTANCE_ID);
		}
		SchedulerContext context = new SchedulerContext();
		context.put(CORRESPONDING_INSTANCE_ID, correspondingInstanceId);
		return context;
	}
}
