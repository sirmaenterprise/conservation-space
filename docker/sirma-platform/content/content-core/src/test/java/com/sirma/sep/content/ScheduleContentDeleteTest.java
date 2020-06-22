package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.ScheduleContentDelete;

/**
 * Test for {@link ScheduleContentDelete}.
 *
 * @author A. Kunchev
 */
public class ScheduleContentDeleteTest {

	@InjectMocks
	private ScheduleContentDelete schedule;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private SchedulerService schedulerService;

	@Before
	public void setup() {
		schedule = new ScheduleContentDelete();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void execute_successful_deleteContentCalled() throws Exception {
		SchedulerContext context = new SchedulerContext();
		InstanceReference instance = new InstanceReferenceMock();
		context.put("target", instance);
		context.put("purpose", "export");
		schedule.execute(context);
		verify(instanceContentService).deleteContent(instance, "export");
	}

	@Test(expected = IllegalArgumentException.class)
	public void createContext_nullId() {
		ScheduleContentDelete.createContext(null, "purpose");
	}

	@Test(expected = IllegalArgumentException.class)
	public void createContext_nullPurpose() {
		ScheduleContentDelete.createContext("id", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createContext_emptyPurpose() {
		ScheduleContentDelete.createContext("id", "");
	}

	@Test
	public void createContext_successful() {
		SchedulerContext context = ScheduleContentDelete.createContext("id", "purpose");
		assertEquals("id", context.getIfSameType("target", Serializable.class));
		assertEquals("purpose", context.getIfSameType("purpose", String.class));
	}

	@Test
	public void buildConfiguration() {
		SchedulerConfiguration configuration = ScheduleContentDelete.buildConfiguration(1, TimeUnit.HOURS);
		assertThat(configuration, configurationMatcher());
	}

	private static CustomMatcher<SchedulerConfiguration> configurationMatcher() {
		return CustomMatcher.of((SchedulerConfiguration configuration) -> {
			assertTrue(configuration.isRemoveOnSuccess());
			assertTrue(configuration.isPersistent());
			assertEquals(TransactionMode.REQUIRED, configuration.getTransactionMode());
			assertEquals(2, configuration.getMaxRetryCount());
			assertNotNull(configuration.getNextScheduleTime());
		});
	}

	@Test
	public void validateInput() {
		List<Pair<String, Class<?>>> validateInput = schedule.validateInput();
		assertFalse(validateInput.isEmpty());
		assertEquals(2, validateInput.size());
	}

}
