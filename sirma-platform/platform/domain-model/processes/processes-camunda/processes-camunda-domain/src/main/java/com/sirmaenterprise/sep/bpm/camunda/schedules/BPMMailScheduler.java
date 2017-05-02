package com.sirmaenterprise.sep.bpm.camunda.schedules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailNotificationService;
import com.sirma.itt.seip.resources.mails.UsersMailExtractor;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.sep.bpm.camunda.bpmn.email.WorkflowNotificationContext;
import com.sirmaenterprise.sep.bpm.camunda.util.BPMInstanceUtil;

/**
 * Schedules sending mail for BPM.
 * 
 * @author simeon
 */
@Singleton
@Named(BPMMailScheduler.BEAN_ID)
public class BPMMailScheduler extends SchedulerActionAdapter {

	protected static final String TEMPLATE_ID = "templateId";
	protected static final String USERS_MAIL_LIST = "usersMailList";
	protected static final String SUBJECT_ID = "subjectId";
	protected static final String BUSINESS_ID = "businessId";
	public static final String BEAN_ID = "BPMMailScheduler";

	@Inject
	private InstanceTypeResolver instanceTypeResolver;
	@Inject
	private UsersMailExtractor usersMailExtractor;
	@Inject
	private javax.enterprise.inject.Instance<MailNotificationService> mailNotificationService;

	@Override
	public void execute(SchedulerContext context) throws Exception {
		String template = context.getIfSameType(TEMPLATE_ID, String.class);
		String mailSubject = context.getIfSameType(SUBJECT_ID, String.class);
		String businessId = context.getIfSameType(BUSINESS_ID, String.class);
		List<String> usersMailList = context.getIfSameType(USERS_MAIL_LIST, ArrayList.class);
		Instance sourceInstance = BPMInstanceUtil.resolveInstance(businessId, instanceTypeResolver);
		Collection<String> mailRecipients = usersMailExtractor.extractMails(usersMailList, sourceInstance);
		Collection<MailNotificationContext> mailContexts = mailRecipients.stream()
				.map(mail -> new WorkflowNotificationContext(sourceInstance, mail, mailSubject, template))
				.collect(Collectors.toList());

		for (MailNotificationContext mailContext : mailContexts) {
			for (MailNotificationService service : mailNotificationService) {
				service.sendEmail(mailContext);
			}
		}
	}

	/**
	 * Creates a {@link SchedulerContext} for sending mails in BPM.
	 * 
	 * @param businessId
	 *            the id of the process instance.
	 * @param subjectId
	 *            the subject of the mail.
	 * @param usersMailList
	 *            the list of properties or users that need to be notified.
	 * @param templateId
	 *            the id of the template.
	 * @return {@link SchedulerContext} for {@link BPMMailScheduler}.
	 */
	public static SchedulerContext createExecutorContext(String businessId, String subjectId,
			List<String> usersMailList, String templateId) {
		SchedulerContext context = new SchedulerContext();
		context.put(BUSINESS_ID, businessId);
		context.put(SUBJECT_ID, subjectId);
		context.put(USERS_MAIL_LIST, new ArrayList<>(usersMailList));
		context.put(TEMPLATE_ID, templateId);
		return context;
	}
}
