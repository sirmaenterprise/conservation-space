package com.sirma.sep.bpm.camunda.bpmn.execution.listeners;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirmaenterprise.sep.bpm.camunda.schedules.BPMMailScheduler;

/**
 * Tests for {@link SendWorkflowMail}.
 *
 * @author simeon iliev
 */
public class SendWorkflowMailTest {

	@Mock
	private SchedulerService schedulerService;
	@InjectMocks
	private SendWorkflowMail workflowMail;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateParameters() throws Exception {
		workflowMail.setMailTemplate(mock(Expression.class));
		workflowMail.setSource(mock(Expression.class));
		workflowMail.setSubject(mock(FixedValue.class));
		workflowMail.setUsers(mock(FixedValue.class));
		workflowMail.validateParameters();
	}

	@Test(expected = NullPointerException.class)
	public void testValidateParametersNullPointer() throws Exception {
		workflowMail.setMailTemplate(null);
		workflowMail.setSource(null);
		workflowMail.validateParameters();
	}

	@Test
	public void testExecute() throws Exception {
		ExecutionEntity entity = mock(ExecutionEntity.class);
		String busineskeyValue = "123456789";
		when(entity.getBusinessKey()).thenReturn(busineskeyValue);
		DelegateExecution execution = mock(DelegateExecution.class);
		Expression mailExpresion = mock(Expression.class);
		Expression sourceExpresion = mock(Expression.class);
		when(sourceExpresion.getValue(eq(execution))).thenReturn(entity);
		FixedValue subjectFixedValue = mock(FixedValue.class);
		when(subjectFixedValue.getExpressionText()).thenReturn("The subject");
		FixedValue usersFixedValue = mock(FixedValue.class);
		when(usersFixedValue.getExpressionText()).thenReturn("emf:userID,emf:GroupID,property");

		SchedulerConfiguration schedulerConfiguration = mock(SchedulerConfiguration.class);
		when(schedulerService.buildEmptyConfiguration(any())).thenReturn(schedulerConfiguration);
		workflowMail.setMailTemplate(mailExpresion);
		workflowMail.setSource(sourceExpresion);
		workflowMail.setSubject(subjectFixedValue);
		workflowMail.setUsers(usersFixedValue);
		workflowMail.execute(execution, workflowMail);
		Mockito.verify(schedulerService).schedule(eq(BPMMailScheduler.BEAN_ID), eq(schedulerConfiguration), any());
	}

}
