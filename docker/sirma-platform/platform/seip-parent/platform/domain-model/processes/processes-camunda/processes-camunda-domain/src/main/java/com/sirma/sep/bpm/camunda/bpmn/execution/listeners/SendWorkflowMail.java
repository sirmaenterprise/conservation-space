package com.sirma.sep.bpm.camunda.bpmn.execution.listeners;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.FixedValue;

import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.ListenerExpressionUtil;
import com.sirmaenterprise.sep.bpm.camunda.listeners.delegate.CDIJavaDelegate;
import com.sirmaenterprise.sep.bpm.camunda.listeners.delegate.CDITaskListener;
import com.sirmaenterprise.sep.bpm.camunda.schedules.BPMMailScheduler;

/**
 * Sends emails from BPM transition, BPM-MUT7
 * 
 * @author simeon iliev
 */
@Singleton
public class SendWorkflowMail implements CDIJavaDelegate<SendWorkflowMail>, CDITaskListener<SendWorkflowMail> {

	private Expression source;
	private Expression mailTemplate;
	private FixedValue users;
	private FixedValue subject;

	@Inject
	private SchedulerService schedulerService;

	@Override
	public void validateParameters() {
		Objects.requireNonNull(mailTemplate, "Mail template is a required field !(mailTemplate)");
		Objects.requireNonNull(source, "Source is a required argument ! (source)");
		Objects.requireNonNull(users, "Users is a required parameter ! (users)");
		Objects.requireNonNull(subject, "The subject of the mail is a required value ! (subject)");
	}

	@Override
	public void execute(DelegateExecution delegateExecution, SendWorkflowMail sourceListener) {
		scheduleMail(delegateExecution, sourceListener);
	}

	@Override
	public void execute(DelegateTask delegateTask, SendWorkflowMail sourceListener) {
		scheduleMail(delegateTask, sourceListener);
	}

	private void scheduleMail(VariableScope scope, SendWorkflowMail sourceListener) {
		String businessId = ListenerExpressionUtil.extractBusinessIdBySourceExpression(scope, sourceListener.source);
		List<String> usersMailList = Arrays
				.asList(sourceListener.users.getExpressionText().replaceAll("\\s+", "").split(","));

		String templateId = sourceListener.mailTemplate.getExpressionText();
		String mailSubject = sourceListener.subject.getExpressionText();
		SchedulerContext schedulerContext = BPMMailScheduler.createExecutorContext(businessId, mailSubject,
																				   usersMailList, templateId);

		SchedulerConfiguration schedulerConfiguration = schedulerService
				.buildEmptyConfiguration(SchedulerEntryType.TIMED);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 1);
		Date time = calendar.getTime();
		schedulerConfiguration.setScheduleTime(time);
		schedulerConfiguration.setRemoveOnSuccess(true);
		schedulerConfiguration.setMaxRetryCount(2);
		schedulerConfiguration.setRetryDelay(Long.valueOf(60));
		schedulerService.schedule(BPMMailScheduler.BEAN_ID, schedulerConfiguration, schedulerContext);
	}

	public Expression getSource() {
		return source;
	}

	public void setSource(Expression source) {
		this.source = source;
	}

	public Expression getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(Expression mailTemplate) {
		this.mailTemplate = mailTemplate;
	}

	public FixedValue getUsers() {
		return users;
	}

	public void setUsers(FixedValue users) {
		this.users = users;
	}

	public FixedValue getSubject() {
		return subject;
	}

	public void setSubject(FixedValue subject) {
		this.subject = subject;
	}
}
