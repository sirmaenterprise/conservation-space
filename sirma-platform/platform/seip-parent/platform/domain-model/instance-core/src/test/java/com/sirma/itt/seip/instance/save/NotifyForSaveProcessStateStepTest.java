package com.sirma.itt.seip.instance.save;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.save.event.BeforeInstanceSaveEvent;
import com.sirma.itt.seip.instance.save.event.BeforeInstanceSaveRollbackEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link NotifyForSaveProcessStateStep}.
 *
 * @author A. Kunchev
 */
public class NotifyForSaveProcessStateStepTest {

	@InjectMocks
	private NotifyForSaveProcessStateStep step;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private EventService eventService;

	@Before
	public void setup() {
		step = new NotifyForSaveProcessStateStep();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void beforeSave_eventFired_contextPopulated_noCurrentInstance() {
		InstanceSaveContext context = beforeSaveInternal(Optional.empty());
		assertNull(context.getSaveEvent().getCurrentInstance());
	}

	@Test
	public void beforeSave_eventFired_contextPopulated_withCurrentInstance() {
		InstanceSaveContext context = beforeSaveInternal(
				Optional.of(InstanceReferenceMock.createGeneric("instance-id")));
		assertNotNull(context.getSaveEvent().getCurrentInstance());
	}

	private InstanceSaveContext beforeSaveInternal(Optional<InstanceReference> optionalToReturn) {
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(optionalToReturn);
		Instance target = new EmfInstance();
		target.setId("instance-id");
		InstanceSaveContext context = InstanceSaveContext.create(target, new Operation());
		step.beforeSave(context);
		verify(eventService).fire(any(BeforeInstanceSaveEvent.class));
		assertTrue(context.getSaveEvent() instanceof BeforeInstanceSaveEvent);
		return context;
	}

	@Test
	public void afterSave_nextPhaseFired() {
		InstanceSaveContext context = InstanceSaveContext
				.create(new EmfInstance(), new Operation())
				.setSaveEvent(new BeforeInstanceSaveEvent(new EmfInstance(), null, Operation.NO_OPERATION));
		step.afterSave(context);
		verify(eventService).fireNextPhase(any(BeforeInstanceSaveEvent.class));
	}

	@Test
	public void rollbackBeforeSave_eventFired_contextPopulatedWithNewEvent() {
		InstanceSaveContext context = InstanceSaveContext
				.create(new EmfInstance(), new Operation())
				.setSaveEvent(new BeforeInstanceSaveEvent(new EmfInstance(), null, Operation.NO_OPERATION));
		step.rollbackBeforeSave(context);
		verify(eventService).fire(any(BeforeInstanceSaveRollbackEvent.class));
		assertTrue(context.getSaveEvent() instanceof BeforeInstanceSaveRollbackEvent);
	}

	@Test
	public void rollbackAfterSave_nextPhaseFired() {
		InstanceSaveContext context = InstanceSaveContext.create(new EmfInstance(), new Operation()).setSaveEvent(
				new BeforeInstanceSaveRollbackEvent(new EmfInstance(), null, Operation.NO_OPERATION));
		step.rollbackAfterSave(context);
		verify(eventService).fireNextPhase(any(BeforeInstanceSaveRollbackEvent.class));
	}

	@Test
	public void getName() {
		assertEquals("notifyForSaveProcessState", step.getName());
	}

}
