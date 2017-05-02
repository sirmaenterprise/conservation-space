package com.sirma.itt.seip.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.spi.BeanManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.serialization.SerializationHelper;
import com.sirma.itt.seip.serialization.kryo.KryoHelper;
import com.sirma.itt.seip.tasks.entity.SchedulerEntity;

/**
 * Test for {@link SchedulerServiceImpl}
 *
 * @author BBonev
 */
public class SchedulerServiceImplTest {

	@InjectMocks
	private SchedulerServiceImpl service;

	@Mock
	private SchedulerExecuter schedulerExecuter;
	@Mock
	private BeanManager beanManager;
	@Mock
	private EventService eventService;
	@Mock
	private SecurityValidator securityValidator;
	@Mock
	private KryoHelper kryoHelper;
	@Mock
	private SerializationHelper serializationHelper;
	@Mock
	private SchedulerEntryStore schedulerStore;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(schedulerStore.getOrCreateEntityForIdentifier(any())).then(a -> {
			SchedulerEntity entity = new SchedulerEntity();
			entity.setIdentifier(a.getArgumentAt(0, String.class));
			return entity;
		});
		when(schedulerStore.persist(any())).then(a -> a.getArgumentAt(0, SchedulerEntity.class));
	}

	@Test
	public void buildConfigurationShouldHaveTheSameType() throws Exception {
		SchedulerConfiguration configuration = service.buildEmptyConfiguration(SchedulerEntryType.TIMED);
		assertNotNull(configuration);
		assertEquals(SchedulerEntryType.TIMED, configuration.getType());
	}

	@Test
	public void scheduleImmediateTaskShouldSetScheduleTimeIfNotPresent() throws Exception {
		SchedulerConfiguration configuration = service.buildEmptyConfiguration(SchedulerEntryType.IMMEDIATE);
		configuration.setSynchronous(true).setPersistent(false);

		when(schedulerExecuter.executeImmediate(any())).thenReturn(Boolean.TRUE);

		service.schedule("action", configuration);

		assertNotNull(configuration.getScheduleTime());
	}

	@Test
	public void scheduleTimedTaskShouldSetScheduleTimeIfNotPresent() throws Exception {
		SchedulerConfiguration configuration = service.buildEmptyConfiguration(SchedulerEntryType.TIMED);
		configuration.setSynchronous(true).setPersistent(false);

		when(schedulerExecuter.executeImmediate(any())).thenReturn(Boolean.TRUE);

		service.schedule("action", configuration);

		assertNotNull(configuration.getScheduleTime());
	}
}
