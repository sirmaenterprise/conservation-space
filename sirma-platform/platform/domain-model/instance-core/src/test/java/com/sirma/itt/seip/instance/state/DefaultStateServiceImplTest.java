package com.sirma.itt.seip.instance.state;

import static org.mockito.Mockito.*;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;

@RunWith(MockitoJUnitRunner.class)
public class DefaultStateServiceImplTest {
	private static final String COMPLETE = "Complete";
	private static final String INIT = "Init";
	@Mock
	private EventService eventService;
	@Mock
	private Iterable<StateServiceExtension<Instance>> services;
	@InjectMocks
	private DefaultStateServiceImpl defaultStateServiceImpl;

	private StateServiceExtension<Instance> extension;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		Iterator<StateServiceExtension<Instance>> iterator = mock(Iterator.class);
		extension = mock(StateServiceExtension.class);
		when(extension.canHandle(Mockito.any())).thenReturn(true, true);
		when(extension.getPrimaryState(any())).thenReturn(INIT, COMPLETE);
		when(extension.isInActiveState(any())).thenReturn(true);
		when(services.iterator()).thenReturn(iterator, iterator, iterator);
		when(iterator.hasNext()).thenReturn(true, true, true);
		when(iterator.next()).thenReturn(extension, extension, extension);
	}

	@Test
	public void testChangeState() throws Exception {
		Instance instance = mock(Instance.class);
		Operation operation = mock(Operation.class);
		when(extension.changeState(instance, operation)).thenReturn(true);
		defaultStateServiceImpl.changeState(instance, operation);
		verify(eventService , only()).fire(any());
	}	

	@Test
	public void testIsInActiveState() throws Exception {
		Instance instance = mock(Instance.class);
		Assert.assertTrue(defaultStateServiceImpl.isInActiveState(instance));
	}

}
