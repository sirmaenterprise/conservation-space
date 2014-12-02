package com.sirma.itt.emf.audit.schedule;

import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.processor.AuditProcessor;
import com.sirma.itt.emf.scheduler.SchedulerActionAdapter;
import com.sirma.itt.emf.scheduler.SchedulerContext;

/**
 * Action that receives a context containing audit activity information stored in
 * {@link AuditActivity} and passes it to an {@link AuditProcessor}. This is a named bean so it can
 * be found by the schedule service.
 * 
 * @author Mihail Radkov
 */
@Named(AuditSchedulerAction.ACTION_NAME)
public class AuditSchedulerAction extends SchedulerActionAdapter {

	public static final String ACTION_NAME = "AuditSchedulerAction";

	@Inject
	private AuditProcessor processor;

	@Override
	public void execute(SchedulerContext context) throws Exception {
		AuditActivity activity = context.getIfSameType("payload", AuditActivity.class);
		processor.processActivity(activity);
	}

}
