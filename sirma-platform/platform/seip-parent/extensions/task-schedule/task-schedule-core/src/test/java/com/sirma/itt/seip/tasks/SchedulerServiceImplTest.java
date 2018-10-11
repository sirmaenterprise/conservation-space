package com.sirma.itt.seip.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
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

	@Test
	@SuppressWarnings({ "boxing", "rawtypes", "unchecked" })
	public void should_be_valid_if_conversion_successful() {
		SchedulerEntity entity = Mockito.mock(SchedulerEntity.class);
		SchedulerEntry entry = new SchedulerEntry();
		Mockito.when(entity.toSchedulerEntry(Matchers.any(SerializationHelper.class))).thenReturn(entry);
		entry.setId(1L);
		Mockito.when(entity.getActionName()).thenReturn("mockAction");

		Mockito.when(beanManager.getReference(Matchers.any(Bean.class), Matchers.any(Type.class),
				Matchers.any(CreationalContext.class))).thenReturn(Mockito.mock(SchedulerAction.class));

		Set beans = new HashSet<>(Arrays.asList(Mockito.mock(Bean.class)));
		Mockito.when(beanManager.getBeans("mockAction")).thenReturn(beans);
		Mockito.doReturn(SchedulerAction.class).when(kryoHelper).getRegisteredClass(Matchers.anyInt());

		Mockito.when(schedulerStore.findById(Matchers.anyLong())).thenReturn(entity);
		Assert.assertEquals(true, service.validate(entry));
	}

	@Test
	public void should_be_invalid_if_conversion_failed() {
		SchedulerEntity entity = Mockito.mock(SchedulerEntity.class);
		SchedulerEntry entry = new SchedulerEntry();
		Mockito.when(entity.toSchedulerEntry(Matchers.any(SerializationHelper.class)))
				.thenThrow(new RuntimeException());

		Assert.assertEquals(false, service.validate(entry));
	}

	@Test
	public void should_loadbyStatus() {
		SchedulerEntity entity = Mockito.mock(SchedulerEntity.class);
		SchedulerEntry entry = new SchedulerEntry();
		Mockito.when(entity.toSchedulerEntry(Matchers.any(SerializationHelper.class))).thenReturn(entry);
		Mockito.when(entity.getActionName()).thenReturn("mockAction");

		Mockito.when(schedulerStore.findByStatus(SchedulerEntryStatus.CANCELED)).thenReturn(Arrays.asList(entity));
		List<SchedulerEntry> entries = service.loadByStatus(SchedulerEntryStatus.CANCELED);

		Assert.assertEquals(entry, entries.get(0));
	}
}
