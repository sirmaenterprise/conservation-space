package com.sirma.sep.content.idoc.extract;

import com.sirma.itt.seip.content.event.InstanceViewEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.testutil.CustomMatcher;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;

/**
 * @author Boyan Tonchev.
 */
public class ViewContentExtractorTest {

	private static final String EMF_ID = "emf-id";

	@Mock
	private SchedulerService schedulerService;

	@InjectMocks
	private ViewContentExtractor viewContentExtractor;

	/**
	 * Runs before each method and setup mockito.
	 */
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests method onInstanceViewEvent.
	 */
	@Test
	public void onInstanceViewEventTest() {
		Instance owner = Mockito.mock(Instance.class);
		Mockito.when(owner.getId()).thenReturn(EMF_ID);
		InstanceViewEvent event = Mockito.mock(InstanceViewEvent.class);
		Mockito.when(event.getOwner()).thenReturn(owner);

		viewContentExtractor.onInstanceViewEvent(event);

		Mockito.verify(schedulerService)
				.schedule(Matchers.eq(ScheduleViewContentExtraction.NAME), argThat(configurationMatcher()),
						  argThat(contextMatcher()));

	}

	/**
	 * Tests method onInstanceViewEvent scenario version of instance.
	 */
	@Test
	public void onInstanceViewEventWithVersionInstanceTest() {
		Instance owner = Mockito.mock(Instance.class);
		Mockito.when(owner.getId()).thenReturn("emf-id-v1.0");
		InstanceViewEvent event = Mockito.mock(InstanceViewEvent.class);
		Mockito.when(event.getOwner()).thenReturn(owner);

		viewContentExtractor.onInstanceViewEvent(event);

		Mockito.verify(schedulerService, never())
				.schedule(Matchers.eq(ScheduleViewContentExtraction.NAME), Matchers.any(SchedulerConfiguration.class),
						  Matchers.any(SchedulerContext.class));

	}

	/**
	 * Tests method onInstanceViewEvent scenario with non Entity owner.
	 */
	@Test
	public void onInstanceViewEventNonEntityOwnerTest() {
		InstanceViewEvent event = Mockito.mock(InstanceViewEvent.class);
		Mockito.when(event.getOwner()).thenReturn("not entity object");

		viewContentExtractor.onInstanceViewEvent(event);

		Mockito.verify(schedulerService, never())
				.schedule(Matchers.eq(ScheduleViewContentExtraction.NAME), Matchers.any(SchedulerConfiguration.class),
						  Matchers.any(SchedulerContext.class));

	}

	private static CustomMatcher<SchedulerConfiguration> configurationMatcher() {
		return CustomMatcher.of((SchedulerConfiguration configuration) -> {
			assertFalse(configuration.isSynchronous());
			assertTrue(configuration.isRemoveOnSuccess());
			assertTrue(configuration.isPersistent());
			assertEquals(TransactionMode.NOT_SUPPORTED, configuration.getTransactionMode());
			assertEquals(5, configuration.getMaxRetryCount());
			assertTrue(configuration.isIncrementalDelay());
			assertEquals(5, configuration.getMaxActivePerGroup());
			assertEquals(SchedulerEntryType.TIMED, configuration.getType());
		});
	}

	private static CustomMatcher<SchedulerContext> contextMatcher() {
		return CustomMatcher.of((SchedulerContext context) -> {
			assertEquals(EMF_ID, context.get(ScheduleViewContentExtraction.INSTANCE_ID));
		});
	}
}
