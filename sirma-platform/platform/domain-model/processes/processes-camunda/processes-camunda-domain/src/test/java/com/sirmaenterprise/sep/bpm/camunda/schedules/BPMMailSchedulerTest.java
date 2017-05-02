package com.sirmaenterprise.sep.bpm.camunda.schedules;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.mail.MailNotificationService;
import com.sirma.itt.seip.resources.mails.UsersMailExtractor;
import com.sirma.itt.seip.tasks.SchedulerContext;

/**
 * Tests for {@link SchedulerContext}.
 *
 * @author simeon
 */
public class BPMMailSchedulerTest {

	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private UsersMailExtractor usersMailExtractor;
	@Mock
	private javax.enterprise.inject.Instance<MailNotificationService> mailNotificationService;
	@InjectMocks
	private BPMMailScheduler bpmMailScheduler;
	
	private SchedulerContext context;
	
	@Before
	public void setUp() throws Exception {
		context = new SchedulerContext();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_execute_scheduled_operation() throws Exception {
		Instance instance = Mockito.mock(Instance.class);
		InstanceReference reference = Mockito.mock(InstanceReference.class);
		Mockito.when(reference.toInstance()).thenReturn(instance);
		Optional<InstanceReference> option = Optional.of(reference);
		Mockito.when(instanceTypeResolver.resolveReference(Matchers.any())).thenReturn(option);
		
		ArrayList<String> emails = new ArrayList<>(Arrays.asList("asd@asd.asd", "hello@java.com"));
		when(usersMailExtractor.extractMails(anyList(), eq(instance))).thenReturn(emails);
		MailNotificationService service = mock(MailNotificationService.class);
		Iterator<MailNotificationService> iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
		when(iterator.next()).thenReturn(service, service);
		when(mailNotificationService.iterator()).thenReturn(iterator);
		
		String templateId = "email_template";
		String businessId = "emf:123456";
		String subjectId = "Test subject";

		context.put(BPMMailScheduler.BUSINESS_ID, businessId);
		context.put(BPMMailScheduler.SUBJECT_ID, subjectId);
		context.put(BPMMailScheduler.USERS_MAIL_LIST, emails);
		context.put(BPMMailScheduler.TEMPLATE_ID, templateId);

		bpmMailScheduler.execute(context);
		
		verify(service, times(2)).sendEmail(any());
	}

	@Test
	public void test_create_executor_context() throws Exception {
		String templateId = "email_template";
		String businessId = "emf:123456";
		String subjectId = "Test subject";
		ArrayList<String> mailList = new ArrayList<>(1);
		context.put(BPMMailScheduler.BUSINESS_ID, businessId);
		context.put(BPMMailScheduler.SUBJECT_ID, subjectId);
		context.put(BPMMailScheduler.USERS_MAIL_LIST, mailList);
		context.put(BPMMailScheduler.TEMPLATE_ID, templateId);

		SchedulerContext createExecutorContext = BPMMailScheduler.createExecutorContext(businessId, subjectId, mailList,
				templateId);
		assertEquals(createExecutorContext, context);
	}

}
