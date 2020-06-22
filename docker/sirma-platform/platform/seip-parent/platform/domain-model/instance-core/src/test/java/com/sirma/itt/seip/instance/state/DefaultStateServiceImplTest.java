package com.sirma.itt.seip.instance.state;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Test for {@link DefaultStateServiceImpl}
 *
 * @author BBonev
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultStateServiceImplTest {
	private static final String COMPLETE = "Complete";
	private static final String INIT = "Init";

	@InjectMocks
	private DefaultStateServiceImpl stateService;

	@Mock
	private EventService eventService;

	private List<StateServiceExtension> extensions = new ArrayList<>();
	@Spy
	private Plugins<StateServiceExtension> services = new Plugins<>("", extensions);
	@Mock
	private StateServiceExtension extension;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(extension.canHandle(any())).thenReturn(true);
		when(extension.getPrimaryState(any())).thenReturn(INIT, COMPLETE);
		when(extension.isInActiveState(any())).thenReturn(true);
		extensions.clear();
		extensions.add(extension);
	}

	@Test
	public void testChangeState() throws Exception {
		Instance instance = new EmfInstance("");
		instance.setType(InstanceType.create(""));
		Operation operation = mock(Operation.class);
		when(extension.changeState(instance, operation)).thenReturn(true);
		stateService.changeState(instance, operation);
		verify(eventService, only()).fire(any());
	}

	@Test
	public void testIsInActiveState() throws Exception {
		Instance instance = new EmfInstance("");
		instance.setType(InstanceType.create(""));
		Assert.assertTrue(stateService.isInActiveState(instance));
	}

	@Test
	public void handleOperation_shouldFireActivationEvent_IfInstanceTransitionsInactiveToActive() throws Exception {
		EmfInstance instance = new EmfInstance("emf:instance");
		instance.setType(InstanceType.create("case"));
		Operation operation = new Operation("create");

		when(extension.isInActiveState(instance)).thenReturn(Boolean.FALSE, Boolean.TRUE);
		when(extension.changeState(instance, operation)).thenReturn(true);

		stateService.changeState(instance, operation);

		verify(eventService).fire(argThat(CustomMatcher.ofPredicate(InstanceActivatedEvent.class::isInstance)));
	}

	@Test
	public void handleOperation_shouldFireDeactivationEvent_IfInstanceTransitionsActiveToInactive() throws Exception {
		EmfInstance instance = new EmfInstance("emf:instance");
		instance.setType(InstanceType.create("case"));
		Operation operation = new Operation("deactivate");

		when(extension.isInActiveState(instance)).thenReturn(Boolean.TRUE, Boolean.FALSE);
		when(extension.changeState(instance, operation)).thenReturn(true);

		stateService.changeState(instance, operation);

		verify(eventService).fire(argThat(CustomMatcher.ofPredicate(InstanceDeactivatedEvent.class::isInstance)));
	}

	@Test
	public void handleOperation_shouldNotFireAnyEvent_IfInstanceDoesNotTransitionsStates() throws Exception {

		EmfInstance instance = new EmfInstance("emf:instance");
		instance.setType(InstanceType.create("case"));
		Operation operation = new Operation("editDetails");

		when(extension.isInActiveState(instance)).thenReturn(Boolean.TRUE, Boolean.TRUE);
		when(extension.changeState(instance, operation)).thenReturn(true);

		stateService.changeState(instance, operation);

		verify(eventService, only()).fire(any());
	}
}
